package com.ivianuu.compose.sample

import androidx.compose.Ambient
import androidx.compose.State
import androidx.compose.state
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

fun Route(content: ViewComposition.() -> Unit) = object : Route {
    override fun ViewComposition.content() {
        content.invoke(this)
    }
}

class Navigator(
    private val state: State<List<Route>>,
    private val onExit: () -> Unit
) {

    fun push(route: Route) {
        state.value = state.value.toMutableList().apply { add(route) }
    }

    fun pop() {
        if (state.value.size > 1) {
            state.value = state.value.toMutableList().apply { removeAt(lastIndex) }
        } else {
            onExit()
        }
    }

    fun ViewComposition.content() {
        state.value.lastOrNull()?._content(this)
    }

    fun _content(viewComposition: ViewComposition) {
        with(viewComposition) {
            content()
        }
    }

    companion object {
        fun createInitialState(startRoute: Route) = listOf(startRoute)
    }

}

fun ViewComposition.Navigator(startRoute: Route, onExit: () -> Unit) {
    val state = +state { Navigator.createInitialState(startRoute) }
    val navigator = Navigator(state, onExit)
    NavigatorAmbient.Provider(navigator) { navigator._content(this) }
}