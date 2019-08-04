package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.transition.DefaultViewTransition
import com.ivianuu.compose.transition.ViewTransition

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
    private val runningTransitions = mutableMapOf<View, ViewTransition>()

    fun rebind(views: List<View>) {
        println("${container.component?.key} rebind")

        this.views.clear()
        this.views += views

        views
            .forEach {
                performTransition(
                    null,
                    it,
                    true,
                    null
                )
            }
    }

    fun setViews(newViews: List<View>, isPush: Boolean) {
        println("${container.component?.key} set views")

        if (newViews == views) return

        val oldViews = views.toList()
        val removedViews = oldViews.filter { it !in newViews }
        val addedViews = newViews.filter { it !in oldViews }

        views.clear()
        views += newViews

        val oldTopView = oldViews.lastOrNull()
        val newTopView = newViews.lastOrNull()

        // check if we should animate the top views
        val replacingTopViews = newTopView != null && (oldTopView == null
                || (oldTopView != newTopView))

        // Remove all views which are not present anymore from top to bottom
        removedViews
            .dropLast(if (replacingTopViews) 1 else 0)
            .reversed()
            .forEach { view ->
                println("${container.component?.key} remove view ${view.component?.key}")
                cancelTransition(view)
                performTransition(
                    from = view,
                    to = null,
                    isPush = isPush,
                    transition = view.component?.outTransition
                )
            }

        // Add any new views to the backStack from bottom to top
        addedViews
            .dropLast(if (replacingTopViews) 1 else 0)
            .forEachIndexed { i, view ->
                println("${container.component?.key} add view ${view.component?.key}")
                performTransition(
                    from = addedViews.getOrNull(i - 1),
                    to = view,
                    isPush = true,
                    transition = view.component?.inTransition
                )
            }

        // Replace the old visible top with the new one
        if (replacingTopViews) {
            val transition = if (isPush) newTopView?.component?.inTransition
            else oldTopView?.component?.outTransition

            println("${container.component?.key} replace top new ${newTopView?.component?.key} old ${oldTopView?.component?.key}")

            performTransition(
                from = oldTopView,
                to = newTopView,
                isPush = isPush,
                transition = transition
            )
        }
    }

    private fun cancelTransition(view: View) {
        runningTransitions.remove(view)?.cancel()
    }

    private fun performTransition(
        from: View?,
        to: View?,
        isPush: Boolean,
        transition: ViewTransition?
    ) {
        val transitionToUse = when {
            transition == null -> DefaultViewTransition()
            transition.hasBeenUsed -> transition.copy()
            else -> transition
        }
        transitionToUse.hasBeenUsed = true

        println("perform transition from ${from?.component?.key} to ${to?.component?.key} is push $isPush transition $transitionToUse")

        from?.let { cancelTransition(it) }
        to?.let { runningTransitions[it] = transitionToUse }

        transitionToUse.execute(
            container,
            from,
            to,
            isPush
        ) {
            if (to != null) runningTransitions -= to
        }
    }

}