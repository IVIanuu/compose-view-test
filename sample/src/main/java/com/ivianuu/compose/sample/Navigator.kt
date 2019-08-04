package com.ivianuu.compose.sample

import androidx.compose.Ambient
import androidx.compose.Recompose
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.util.sourceLocation

val NavigatorAmbient = Ambient.of<Navigator>()

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

inline fun Route(
    isFloating: Boolean = false,
    noinline compose: ViewComposition.() -> Unit
) = Route(sourceLocation(), isFloating, compose)

fun Route(
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

    init {
        backStack.add(startRoute)
    }

    fun push(route: Route) {
        backStack.add(route)
        recompose()
    }

    fun pop() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
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
            .forEach {
                viewComposition.group(it.key) {
                    it._compose(viewComposition)
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