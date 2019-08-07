package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.Ambient

val InChangeHandlerAmbient = Ambient.of<ComponentChangeHandler?>("InTransition")
val OutChangeHandlerAmbient = Ambient.of<ComponentChangeHandler?>("OutTransition")
val TransitionHintsAmbient = Ambient.of("TransitionHints") { true }

fun ViewComposition.Transitions(
    changeHandler: ComponentChangeHandler?,
    children: ViewComposition.() -> Unit
) {
    Transitions(
        inChangeHandler = changeHandler,
        outChangeHandler = changeHandler,
        children = children
    )
}

fun ViewComposition.Transitions(
    inChangeHandler: ComponentChangeHandler? = null,
    outChangeHandler: ComponentChangeHandler? = null,
    children: ViewComposition.() -> Unit
) {
    InChangeHandlerAmbient.Provider(inChangeHandler) {
        OutChangeHandlerAmbient.Provider(outChangeHandler) {
            children()
        }
    }
}

abstract class ComponentChangeHandler {

    internal var hasBeenUsed = false

    abstract fun execute(changeData: ChangeData)

    abstract fun cancel()

    abstract fun copy(): ComponentChangeHandler

    data class ChangeData(
        val container: ViewGroup,
        val from: View?,
        val to: View?,
        val isPush: Boolean,
        val onComplete: () -> Unit
    ) {
        val addedToView = to != null && to.parent == null
    }

}

class DefaultChangeHandler : ComponentChangeHandler() {

    override fun execute(changeData: ChangeData) {
        with(changeData) {
            if (from != null) container.removeView(from)
            if (to != null && to.parent == null) container.addView(to)
            onComplete()
        }
    }

    override fun copy(): ComponentChangeHandler =
        DefaultChangeHandler()

    override fun cancel() {
    }
}