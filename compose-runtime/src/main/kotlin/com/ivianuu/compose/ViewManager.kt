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
    private val runningTransitions = mutableMapOf<View, ViewTransition>()

    fun setViews(newViews: List<View>, isPush: Boolean) {
        println("set views $newViews is push $isPush")
        if (newViews == views) return

        val oldViews = views.toList()

        views.clear()
        views += newViews

        val oldTopView = oldViews.lastOrNull()
        val newTopView = newViews.lastOrNull()

        // check if we should animate the top views
        val replacingTopViews = newTopView != null && (oldTopView == null
                || oldTopView != newTopView)

        // Remove all views which are not present anymore from top to bottom
        oldViews
            .dropLast(if (replacingTopViews) 1 else 0)
            .reversed()
            .filterNot { it in newViews }
            .forEach { view ->
                cancelTransition(view)

                performTransition(
                    from = view,
                    to = null,
                    isPush = isPush,
                    transition = view.outTransition
                )
            }

        // Add any new views to the backStack from bottom to top
        newViews
            .dropLast(if (replacingTopViews) 1 else 0)
            .filterNot { it in oldViews }
            .forEachIndexed { i, view ->
                performTransition(
                    from = newViews.getOrNull(i - 1),
                    to = view,
                    isPush = true,
                    transition = view.inTransition
                )
            }

        // Replace the old visible top with the new one
        if (replacingTopViews) {
            val transition = if (isPush) newTopView?.inTransition
            else oldTopView?.outTransition

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
        val handlerToUse = when {
            transition == null -> DefaultViewTransition()
            transition.hasBeenUsed -> transition.copy()
            else -> transition
        }
        handlerToUse.hasBeenUsed = true

        from?.let { cancelTransition(it) }
        to?.let { runningTransitions[it] = handlerToUse }



        handlerToUse.execute(
            container,
            from,
            to,
            isPush
        ) {
            if (to != null) runningTransitions -= to
        }
    }

}