package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

private val viewManagerKey = tagKey("viewManager")

internal fun ViewGroup.getViewManager(): ViewManager {
    var viewManager = getTag(viewManagerKey) as? ViewManager
    if (viewManager == null) {
        viewManager = ViewManager(this)
        setTag(viewManagerKey, viewManager)
    }

    return viewManager

}

internal class ViewManager(val container: ViewGroup) {

    val views = mutableListOf<View>()

    fun setViews(newViews: List<View>) {
        if (newViews == views) return

        println("$container set views $newViews")
        val oldViews = views.toList()

        val removedViews = oldViews.filter { it !in newViews }
        val addedViews = newViews.filter { it !in oldViews }

        removedViews.forEach { container.removeView(it) }
        addedViews.forEach { container.addView(it) }

        views.clear()
        views += newViews
    }

}

/*
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
 */