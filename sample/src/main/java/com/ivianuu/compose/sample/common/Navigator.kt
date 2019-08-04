package com.ivianuu.compose.sample.common

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
    private val startRoute: Route,
    private val onExit: () -> Unit,
    unit: Unit
) {

    private val backStack = mutableListOf<Route>()
    lateinit var recompose: () -> Unit

    private var wasPush = true

    init {
        backStack.add(startRoute)
    }

    fun push(route: Route) {
        backStack.add(route)
        wasPush = true
        recompose()
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            wasPush = false
            recompose()
        } else {
            onExit()
        }
    }

    fun content(viewComposition: ViewComposition) = Recompose { recompose ->
        this@Navigator.recompose = recompose

        val visibleRoutes = mutableListOf<Route>()

        for (route in backStack.reversed()) {
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

fun ViewComposition.Navigator(
    startRoute: Route,
    onExit: () -> Unit
) {
    val navigator = Navigator(startRoute, onExit, Unit)
    NavigatorAmbient.Provider(navigator) {
        navigator.content(this)
    }
}