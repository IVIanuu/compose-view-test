/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose.common

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.Ambient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ivianuu.compose.ActivityAmbient
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.Hidden
import com.ivianuu.compose.TransitionHints
import com.ivianuu.compose.ambient
import com.ivianuu.compose.internal.sourceLocation
import com.ivianuu.compose.invalidate
import com.ivianuu.compose.key
import com.ivianuu.compose.memo
import com.ivianuu.compose.onActive
import com.ivianuu.compose.onDispose
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun ComponentComposition.Navigator(startRoute: ComponentComposition.() -> Route) {
    val activity = ambient(ActivityAmbient)
    val backPressedDispatcher = (activity as OnBackPressedDispatcherOwner).onBackPressedDispatcher
    val navigator = memo { Navigator(startRoute()) }

    val onBackPressedCallback = memo {
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                navigator.pop()
            }
        }
    }

    onActive {
        backPressedDispatcher.addCallback(onBackPressedCallback)
        onDispose { onBackPressedCallback.remove() }
    }

    navigator.backStackChangeObserver = {
        onBackPressedCallback.isEnabled = it.size > 1
    }

    onDispose { navigator.backStackChangeObserver = null }
    activity.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                navigator.backStackChangeObserver = null
            }
        }
    })

    NavigatorAmbient.Provider(navigator) {
        navigator.compose(this)
    }
}

class Navigator(private val startRoute: Route) {

    internal var backStackChangeObserver: ((List<Route>) -> Unit)? = null
        set(value) {
            field = value
            value?.invoke(_backStack)
        }

    val backStack: List<Route> get() = _backStack
    private val _backStack = mutableListOf<Route>()
    lateinit var invalidate: () -> Unit

    private var wasPush = true

    private val resultsByRoute = mutableMapOf<Route, CompletableDeferred<Any?>>()

    init {
        _backStack += startRoute
    }

    fun push(route: Route) {
        GlobalScope.launch(Dispatchers.Main) { push<Any?>(route) }
    }

    suspend fun <T> push(route: Route): T? {
        _backStack += route
        wasPush = true
        backStackChangeObserver?.invoke(_backStack)
        invalidate()
        val deferredResult = CompletableDeferred<Any?>()
        resultsByRoute[route] = deferredResult
        return deferredResult.await() as? T
    }

    fun pop(result: Any? = null) {
        if (_backStack.size > 1) {
            val route = _backStack.removeAt(_backStack.lastIndex)
            val deferredResult = resultsByRoute.remove(route)
            deferredResult?.complete(result)
            wasPush = false
            backStackChangeObserver?.invoke(_backStack)
            invalidate()
        }
    }

    fun popToRoot(result: Any? = null) {
        val top = _backStack.last()
        val deferredResult = resultsByRoute.remove(top)
        deferredResult?.complete(result)
        val root = _backStack.first()
        _backStack.clear()
        _backStack += root
        wasPush = false
        backStackChangeObserver?.invoke(_backStack)
        invalidate()
    }

    fun compose(componentComposition: ComponentComposition) {
        this@Navigator.invalidate = componentComposition.invalidate

        backStack
            .filter { it.keepState || it.isVisible() }
            .forEach {
                componentComposition.key(it.key) {
                    TransitionHints(wasPush) {
                        Hidden(!it.isVisible()) {
                            it._compose(componentComposition)
                        }
                    }
                }
            }
    }

    // todo improve
    private fun Route.isVisible(): Boolean {
        val visibleRoutes = mutableListOf<Route>()

        for (route in _backStack.reversed()) {
            visibleRoutes += route
            if (!route.isFloating) break
        }

        return this in visibleRoutes
    }
}

private val NavigatorAmbient = Ambient.of<Navigator>()
val ComponentComposition.navigator: Navigator get() = ambient(NavigatorAmbient)

interface Route {

    val key: Any

    val isFloating: Boolean
    val keepState: Boolean

    fun ComponentComposition.compose()

    fun _compose(componentComposition: ComponentComposition) {
        with(componentComposition) {
            compose()
        }
    }

}

inline fun Route(
    isFloating: Boolean = false,
    keepState: Boolean = false,
    noinline compose: ComponentComposition.() -> Unit
) = Route(sourceLocation(), isFloating, keepState, compose)

fun Route(
    key: Any,
    isFloating: Boolean = false,
    keepState: Boolean = false,
    content: ComponentComposition.() -> Unit
) = object : Route {

    override val key: Any
        get() = key

    override val isFloating: Boolean
        get() = isFloating
    override val keepState: Boolean
        get() = keepState

    override fun ComponentComposition.compose() {
        content.invoke(this)
    }
}