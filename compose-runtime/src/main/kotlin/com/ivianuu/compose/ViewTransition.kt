package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.Ambient

val InTransitionAmbient = Ambient.of<ViewTransition> { DefaultViewTransition() }
val OutTransitionAmbient = Ambient.of<ViewTransition> { DefaultViewTransition() }

fun ViewComposition.Transitions(
    transition: ViewTransition,
    children: ViewComposition.() -> Unit
) {
    Transitions(inTransition = transition, outTransition = transition, children = children)
}

fun ViewComposition.Transitions(
    inTransition: ViewTransition? = null,
    outTransition: ViewTransition? = null,
    children: ViewComposition.() -> Unit
) {
    InTransitionAmbient.Provider(inTransition ?: DefaultViewTransition()) {
        OutTransitionAmbient.Provider(outTransition ?: DefaultViewTransition()) {
            children()
        }
    }
}

private fun tagKey(key: String): Int {
    return (3 shl 24) or key.hashCode()
}

internal val animationControllerKey = tagKey("animationController")

internal fun View.getAnimationController(): ViewAnimationController {
    var controller = getTag(animationControllerKey) as? ViewAnimationController
    if (controller == null) {
        controller = ViewAnimationController(this)
        setTag(animationControllerKey, controller)
    }

    return controller
}

private val inTransitionKey = tagKey("inTransition")
@PublishedApi
internal var View.inTransition: ViewTransition?
    get() = getTag(inTransitionKey) as? ViewTransition
    set(value) {
        setTag(inTransitionKey, value)
    }

private val outTransitionKey = tagKey("inTransition")
@PublishedApi
internal var View.outTransition: ViewTransition?
    get() = getTag(outTransitionKey) as? ViewTransition
    set(value) {
        setTag(outTransitionKey, value)
    }

internal class ViewAnimationController(val view: View) {

    private var current: ViewTransition? = null

    fun add(container: ViewGroup, index: Int) {
        performTransition(container, ViewTransition.Direction.In, index)
    }

    fun remove(container: ViewGroup) {
        performTransition(container, ViewTransition.Direction.Out, null)
    }

    fun cancelCurrent() {
        current?.cancel()
        current = null
    }

    private fun performTransition(
        container: ViewGroup,
        direction: ViewTransition.Direction,
        index: Int?
    ) {
        cancelCurrent()
        val transition = getTransition(direction)
        current = transition
        transition.execute(container, view, direction, index) { current = null }
    }

    private fun getTransition(direction: ViewTransition.Direction): ViewTransition {
        var transition = when(direction) {
            ViewTransition.Direction.In -> view.inTransition
            ViewTransition.Direction.Out -> view.outTransition
        } ?: DefaultViewTransition()
        if (transition.hasBeenUsed) {
            transition = transition.copy()
        }

        transition.hasBeenUsed = true

        return transition
    }
}

abstract class ViewTransition {

    internal var hasBeenUsed = false

    abstract fun execute(
        container: ViewGroup,
        view: View,
        direction: Direction,
        index: Int?,
        onComplete: () -> Unit
    )

    abstract fun cancel()

    abstract fun copy(): ViewTransition

    data class ChangeData(
        val container: ViewGroup,
        val view: View,
        val direction: Direction,
        val index: Int?,
        val onComplete: () -> Unit
    )

    enum class Direction {
        In, Out
    }
}

class DefaultViewTransition : ViewTransition() {

    override fun execute(
        container: ViewGroup,
        view: View,
        direction: Direction,
        index: Int?,
        onComplete: () -> Unit
    ) {
        when(direction) {
            Direction.In -> container.addView(view, index!!)
            Direction.Out -> container.removeView(view)
        }
        onComplete()
    }

    override fun copy(): ViewTransition = DefaultViewTransition()

    override fun cancel() {
    }
}