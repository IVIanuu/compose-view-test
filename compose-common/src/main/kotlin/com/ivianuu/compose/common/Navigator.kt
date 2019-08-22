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
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.ComponentChangeHandler
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.Hidden
import com.ivianuu.compose.ShareViews
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

    fun compose(composition: ComponentComposition) = with(composition) {
        backStack
            .filter { it.keepState || it.isVisible() }
            .forEach { route ->
                key(key = route.key) {
                    ChangeHandlers(inHandler = route.inHandler, outHandler = route.outHandler) {
                        TransitionHints(isPush = wasPush) {
                            Hidden(value = !route.isVisible()) {
                                ShareViews(value = false) {
                                    RouteAmbient.Provider(value = route) {
                                        with(route) {
                                            compose()
                                        }
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

    val inHandler: ComponentChangeHandler?
    val outHandler: ComponentChangeHandler?

    fun ComponentComposition.compose()

    fun withHandlers(
        inHandler: ComponentChangeHandler? = null,
        outHandler: ComponentChangeHandler? = null
    ): Route

}

fun Route.withHandlers(handler: ComponentChangeHandler): Route =
    withHandlers(inHandler = handler, outHandler = handler)

inline fun Route(
    isFloating: Boolean = false,
    keepState: Boolean = false,
    inHandler: ComponentChangeHandler? = null,
    outHandler: ComponentChangeHandler? = null,
    noinline compose: ComponentComposition.() -> Unit
) = Route(sourceLocation(), isFloating, keepState, inHandler, outHandler, compose)

fun Route(
    key: Any,
    isFloating: Boolean = false,
    keepState: Boolean = false,
    inHandler: ComponentChangeHandler? = null,
    outHandler: ComponentChangeHandler? = null,
    compose: ComponentComposition.() -> Unit
) = SimpleRoute(key, isFloating, keepState, inHandler, outHandler, compose)

inline fun Route(
    isFloating: Boolean = false,
    keepState: Boolean = false,
    handler: ComponentChangeHandler,
    noinline compose: ComponentComposition.() -> Unit
): Route = Route(sourceLocation(), isFloating, keepState, handler, handler, compose)

fun Route(
    key: Any,
    isFloating: Boolean = false,
    keepState: Boolean = false,
    handler: ComponentChangeHandler,
    compose: ComponentComposition.() -> Unit
): Route = Route(key, isFloating, keepState, handler, handler, compose)

class SimpleRoute(
    override val key: Any,
    override val isFloating: Boolean = false,
    override val keepState: Boolean = false,
    override val inHandler: ComponentChangeHandler? = null,
    override val outHandler: ComponentChangeHandler? = null,
    private val content: ComponentComposition.() -> Unit
) : Route {

    override fun ComponentComposition.compose() {
        content()
    }

    override fun withHandlers(
        inHandler: ComponentChangeHandler?,
        outHandler: ComponentChangeHandler?
    ): Route = SimpleRoute(key, isFloating, keepState, inHandler, outHandler, content)

}

val RouteAmbient = Ambient.of<Route?>("CurrentRoute") { null }