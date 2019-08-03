package com.ivianuu.compose.sample

import androidx.compose.Ambient
import androidx.compose.Recompose
import com.ivianuu.compose.ViewComposition

val NavigatorAmbient = Ambient.of<Navigator>()

interface Route {

    fun ViewComposition.content()

    fun _content(viewComposition: ViewComposition) {
        with(viewComposition) {
            content()
        }
    }

}

fun Route(
    content: ViewComposition.() -> Unit
) = object : Route {
    override fun ViewComposition.content() {
        content.invoke(this)
    }
}

class Navigator(
    private val startRoute: Route,
    private val onExit: () -> Unit,
    private val unit: Unit
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
        backStack.lastOrNull()?._content(viewComposition)
    }

}

fun ViewComposition.Navigator(startRoute: Route, onExit: () -> Unit) {
    val navigator = Navigator(startRoute, onExit, Unit)
    NavigatorAmbient.Provider(navigator) {
        navigator.content(this)
    }
}