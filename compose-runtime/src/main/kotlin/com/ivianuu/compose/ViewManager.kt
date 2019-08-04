package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.transition.DefaultViewTransition
import com.ivianuu.compose.transition.ViewTransition
import com.ivianuu.compose.transition.inTransition
import com.ivianuu.compose.transition.outTransition

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
                    transition = view.outTransition
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
                    transition = view.inTransition
                )
            }

        // Replace the old visible top with the new one
        if (replacingTopViews) {
            val transition = if (isPush) newTopView?.inTransition
            else oldTopView?.outTransition

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