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

import androidx.compose.Ambient
import androidx.compose.Recompose
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.Hidden
import com.ivianuu.compose.TransitionHints
import com.ivianuu.compose.internal.sourceLocation
import com.ivianuu.compose.key
import com.ivianuu.compose.memo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun ComponentComposition.Navigator(
    handleBack: Boolean = true,
    startRoute: ComponentComposition.() -> Route
) {
    Recompose { recompose ->
        val navigator = memo { Navigator(startRoute()) }
        navigator.recompose = recompose

        if (handleBack && navigator.backStack.size > 1) {
            key(navigator.backStack.size) {
                handleBack { navigator.pop() }
            }
        }

        NavigatorAmbient.Provider(navigator) {
            navigator.compose(this)
        }
    }
}

class Navigator internal constructor(private val startRoute: Route) {

    lateinit var recompose: () -> Unit

    val backStack: List<Route> get() = _backStack
    private val _backStack = mutableListOf<Route>()

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
            recompose()
        }
    }

    fun compose(componentComposition: ComponentComposition) {
        backStack
            .filter { it.keepState || it.isVisible() }
            .forEach { route ->
                componentComposition.key(key = route.key) {
                    TransitionHints(isPush = wasPush) {
                        Hidden(value = !route.isVisible()) {
                            RouteAmbient.Provider(value = route) {
                                with(route) {
                                    with(componentComposition) {
                                        compose()
                                    }
                                }
                            }
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

val NavigatorAmbient = Ambient.of<Navigator>()

interface Route {

    val key: Any

    val isFloating: Boolean
    val keepState: Boolean

    fun ComponentComposition.compose()

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
        content()
    }
}

val RouteAmbient = Ambient.of<Route?>("CurrentRoute") { null }