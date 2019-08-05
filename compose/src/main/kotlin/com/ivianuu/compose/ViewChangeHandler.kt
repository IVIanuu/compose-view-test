package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.Ambient

val InChangeHandlerAmbient = Ambient.of<ViewChangeHandler?>("InTransition")
val OutChangeHandlerAmbient = Ambient.of<ViewChangeHandler?>("OutTransition")
val TransitionHintsAmbient = Ambient.of("TransitionHints") { true }

fun ViewComposition.Transitions(
    changeHandler: ViewChangeHandler?,
    children: ViewComposition.() -> Unit
) {
    Transitions(
        inChangeHandler = changeHandler,
        outChangeHandler = changeHandler,
        children = children
    )
}

fun ViewComposition.Transitions(
    inChangeHandler: ViewChangeHandler? = null,
    outChangeHandler: ViewChangeHandler? = null,
    children: ViewComposition.() -> Unit
) {
    InChangeHandlerAmbient.Provider(inChangeHandler) {
        OutChangeHandlerAmbient.Provider(outChangeHandler) {
            children()
        }
    }
}

abstract class ViewChangeHandler {

    internal var hasBeenUsed = false

    abstract fun execute(changeData: ChangeData)

    abstract fun cancel()

    abstract fun copy(): ViewChangeHandler

    data class ChangeData(
        val container: ViewGroup,
        val from: View?,
        val to: View?,
        val isPush: Boolean,
        val onComplete: () -> Unit
    )

}

class DefaultViewChangeHandler : ViewChangeHandler() {

    override fun execute(changeData: ChangeData) {
        with(changeData) {
            if (from != null) container.removeView(from)
            if (to != null && to.parent == null) container.addView(to)
            onComplete()
        }
    }

    override fun copy(): ViewChangeHandler =
        DefaultViewChangeHandler()

    override fun cancel() {
    }
}