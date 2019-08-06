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
    private val runningTransitions = mutableMapOf<View, ViewChangeHandler>()

    fun rebind(views: List<View>) {
        println("${container.component?.key} rebind")

        this.views.clear()
        this.views += views

        views
            .forEach {
                performChange(
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
        val replacingTopViews = newTopView != null && oldTopView != null && newTopView != oldTopView

        // Remove all views which are not present anymore from top to bottom
        removedViews
            .dropLast(if (replacingTopViews) 1 else 0)
            .reversed()
            .forEach { view ->
                println("${container.component?.key} remove view ${view.component?.key}")
                cancelTransition(view)
                performChange(
                    from = view,
                    to = null,
                    isPush = isPush,
                    changeHandler = view.component?.outChangeHandler
                )
            }

        // Add any new views to the backStack from bottom to top
        addedViews
            .dropLast(if (replacingTopViews) 1 else 0)
            .forEachIndexed { i, view ->
                println("${container.component?.key} add view ${view.component?.key}")
                performChange(
                    from = addedViews.getOrNull(i - 1),
                    to = view,
                    isPush = true,
                    changeHandler = view.component?.inChangeHandler
                )
            }

        // Replace the old visible top with the new one
        if (replacingTopViews) {
            val transition = if (isPush) newTopView?.component?.inChangeHandler
            else oldTopView?.component?.outChangeHandler

            println("${container.component?.key} replace top new ${newTopView?.component?.key} old ${oldTopView?.component?.key}")

            performChange(
                from = oldTopView,
                to = newTopView,
                isPush = isPush,
                changeHandler = transition
            )
        }
    }

    private fun cancelTransition(view: View) {
        runningTransitions.remove(view)?.cancel()
    }

    private fun performChange(
        from: View?,
        to: View?,
        isPush: Boolean,
        changeHandler: ViewChangeHandler?
    ) {
        val handlerToUse = when {
            changeHandler == null -> DefaultViewChangeHandler()
            changeHandler.hasBeenUsed -> changeHandler.copy()
            else -> changeHandler
        }
        handlerToUse.hasBeenUsed = true

        println("perform changeHandler from ${from?.component?.key} to ${to?.component?.key} is push $isPush changeHandler $handlerToUse")

        from?.let { cancelTransition(it) }
        to?.let { runningTransitions[it] = handlerToUse }

        val changeData = ViewChangeHandler.ChangeData(
            container = container,
            from = from,
            to = to,
            isPush = isPush,
            onComplete = { if (to != null) runningTransitions -= to }
        )

        handlerToUse.execute(changeData)
    }

}