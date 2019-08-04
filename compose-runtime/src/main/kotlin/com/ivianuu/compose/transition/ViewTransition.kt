package com.ivianuu.compose.transition

import android.view.View
import android.view.ViewGroup
import androidx.compose.Ambient
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.tagKey

val InTransitionAmbient = Ambient.of<ViewTransition?>()
val OutTransitionAmbient = Ambient.of<ViewTransition?>()

private val inTransitionKey = tagKey("inTransition")
var View.inTransition: ViewTransition?
    get() = getTag(inTransitionKey) as? ViewTransition
    set(value) {
        setTag(inTransitionKey, value)
    }

private val outTransitionKey = tagKey("inTransition")
var View.outTransition: ViewTransition?
    get() = getTag(outTransitionKey) as? ViewTransition
    set(value) {
        setTag(outTransitionKey, value)
    }

fun ViewComposition.Transitions(
    transition: ViewTransition?,
    children: ViewComposition.() -> Unit
) {
    Transitions(inTransition = transition, outTransition = transition, children = children)
}

fun ViewComposition.Transitions(
    inTransition: ViewTransition? = null,
    outTransition: ViewTransition? = null,
    children: ViewComposition.() -> Unit
) {
    InTransitionAmbient.Provider(inTransition) {
        OutTransitionAmbient.Provider(outTransition) {
            children()
        }
    }
}

abstract class ViewTransition {

    internal var hasBeenUsed = false

    abstract fun execute(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onComplete: () -> Unit
    )

    abstract fun cancel()

    abstract fun copy(): ViewTransition

    data class ChangeData(
        val container: ViewGroup,
        val from: View?,
        val to: View?,
        val isPush: Boolean,
        val onComplete: () -> Unit
    )

}

class DefaultViewTransition : ViewTransition() {

    override fun execute(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onComplete: () -> Unit
    ) {
        if (from != null) container.removeView(from)
        if (to != null && to.parent == null) {
            container.addView(to)
        }
        onComplete()
    }

    override fun copy(): ViewTransition =
        DefaultViewTransition()

    override fun cancel() {
    }
}