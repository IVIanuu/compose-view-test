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

package com.ivianuu.compose.sample.common

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.Ambient
import androidx.compose.Recompose
import androidx.compose.ambient
import com.ivianuu.compose.TransitionHintsAmbient
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sourceLocation

val NavigatorAmbient = Ambient.of<Navigator>()

fun ViewComposition.navigator() = +ambient(NavigatorAmbient)

interface Route {

    val key: Any

    val isFloating: Boolean

    fun ViewComposition.compose()

    fun _compose(viewComposition: ViewComposition) {
        with(viewComposition) {
            compose()
        }
    }

}

inline fun ViewComposition.Route(
    isFloating: Boolean = false,
    noinline compose: ViewComposition.() -> Unit
) = Route(sourceLocation(), isFloating, compose)

fun ViewComposition.Route(
    key: Any,
    isFloating: Boolean = false,
    content: ViewComposition.() -> Unit
) = object : Route {

    override val key: Any
        get() = key

    override val isFloating: Boolean
        get() = isFloating

    override fun ViewComposition.compose() {
        content.invoke(this)
    }
}

class Navigator(
    private val activity: Ref<ComponentActivity?>,
    private val startRoute: Route
) {

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            pop()
        }
    }

    val backStack: List<Route> get() = _backStack
    private val _backStack = mutableListOf<Route>()
    lateinit var recompose: () -> Unit

    private var wasPush = true

    init {
        activity.onUpdate { _, newValue ->
            newValue?.onBackPressedDispatcher?.addCallback(backPressedCallback)
        }
        _backStack.add(startRoute)
    }

    fun push(route: Route) {
        _backStack.add(route)
        wasPush = true
        recompose()
    }

    fun pop() {
        if (_backStack.size > 1) {
            _backStack.removeAt(_backStack.lastIndex)
            wasPush = false
            recompose()
        } else {
            activity()?.finish()
        }
    }

    fun popToRoot() {
        val root = _backStack.first()
        _backStack.clear()
        _backStack.add(root)
        wasPush = false
        recompose()
    }

    fun content(viewComposition: ViewComposition) = Recompose { recompose ->
        this@Navigator.recompose = recompose

        val visibleRoutes = mutableListOf<Route>()

        for (route in _backStack.reversed()) {
            visibleRoutes.add(route)
            if (!route.isFloating) break
        }

        visibleRoutes.reversed()
            .also { println("compose routes ${it.map { it.key }}") }
            .forEach {
                viewComposition.group(it.key) {
                    TransitionHintsAmbient.Provider(wasPush) {
                        it._compose(viewComposition)
                    }
                }
            }
    }

}

fun ViewComposition.Navigator(startRoute: ViewComposition.() -> Route) {
    val activity = +ambient(ActivityRefAmbient)
    val navigator = Navigator(activity as Ref<ComponentActivity?>, startRoute())
    NavigatorAmbient.Provider(navigator) {
        navigator.content(this)
    }
}