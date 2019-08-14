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

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.Ambient
import androidx.compose.Recompose
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ivianuu.compose.ActivityAmbient
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.TransitionHintsAmbient
import com.ivianuu.compose.ambient
import com.ivianuu.compose.memo
import com.ivianuu.compose.sourceLocation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val NavigatorAmbient = Ambient.of<Navigator>()
val ComponentComposition.navigator: Navigator get() = ambient(NavigatorAmbient)

interface Route {

    val key: Any

    val isFloating: Boolean

    fun ComponentComposition.compose()

    fun _compose(componentComposition: ComponentComposition) {
        with(componentComposition) {
            compose()
        }
    }

}

inline fun Route(
    isFloating: Boolean = false,
    noinline compose: ComponentComposition.() -> Unit
) = Route(sourceLocation(), isFloating, compose)

fun Route(
    key: Any,
    isFloating: Boolean = false,
    content: ComponentComposition.() -> Unit
) = object : Route {

    override val key: Any
        get() = key

    override val isFloating: Boolean
        get() = isFloating

    override fun ComponentComposition.compose() {
        content.invoke(this)
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
    lateinit var recompose: () -> Unit

    private var wasPush = true

    private val resultsByRoute = mutableMapOf<Route, CompletableDeferred<Any?>>()

    init {
        _backStack.add(startRoute)
    }

    fun push(route: Route) {
        GlobalScope.launch(Dispatchers.Main) { push<Any?>(route) }
    }

    suspend fun <T> push(route: Route): T? {
        _backStack.add(route)
        wasPush = true
        backStackChangeObserver?.invoke(_backStack)
        recompose()
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
            recompose()
        }
    }

    fun popToRoot(result: Any? = null) {
        val top = _backStack.last()
        val deferredResult = resultsByRoute.remove(top)
        deferredResult?.complete(result)
        val root = _backStack.first()
        _backStack.clear()
        _backStack.add(root)
        wasPush = false
        backStackChangeObserver?.invoke(_backStack)
        recompose()
    }

    fun compose(componentComposition: ComponentComposition) = Recompose { recompose ->
        this@Navigator.recompose = recompose

        val visibleRoutes = mutableListOf<Route>()

        for (route in _backStack.reversed()) {
            visibleRoutes.add(route)
            if (!route.isFloating) break
        }

        visibleRoutes.reversed()
            .also { println("compose routes ${it.map { it.key }}") }
            .forEach {
                componentComposition.group(it.key) {
                    TransitionHintsAmbient.Provider(wasPush) {
                        it._compose(componentComposition)
                    }
                }
            }
    }

}

fun ComponentComposition.Navigator(startRoute: ComponentComposition.() -> Route) {
    val activity = ambient(ActivityAmbient)
    val navigator = memo { Navigator(startRoute()) }

    val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            navigator.pop()
        }
    }
    (activity as ComponentActivity).onBackPressedDispatcher.addCallback(
        activity,
        onBackPressedCallback
    )

    navigator.backStackChangeObserver = { onBackPressedCallback.isEnabled = it.size > 1 }

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