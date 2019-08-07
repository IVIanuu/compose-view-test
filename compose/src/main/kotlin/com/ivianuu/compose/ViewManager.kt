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

    val children = mutableListOf<Component<*>>()
    private val runningTransitions = mutableMapOf<Component<*>, ComponentChangeHandler>()

    private val viewsByChild = mutableMapOf<Component<*>, View>()

    fun init(children: List<Component<*>>) {
        println("${container.component?.key} init")

        this.children.clear()
        this.children += children

        children
            .forEach {
                performChange(
                    null,
                    it,
                    true,
                    null
                )
            }
    }

    fun clear() {
        children.forEach {
            performChange(
                it,
                null,
                false,
                null
            )
        }
    }

    fun update(newChildren: List<Component<*>>, isPush: Boolean) {
        println("${container.component?.key} set components")

        if (children == newChildren) return

        val oldChildren = children.toList()
        val removedChildren = oldChildren.filter { it !in newChildren }
        val addedChildren = newChildren.filter { it !in oldChildren }

        children.clear()
        children += newChildren

        val oldTopChild = oldChildren.lastOrNull()
        val newTopChild = newChildren.lastOrNull()

        // check if we should animate the top views
        val replacingTopChildren =
            newTopChild != null && oldTopChild != null && newTopChild != oldTopChild

        // Remove all views which are not present anymore from top to bottom
        removedChildren
            .dropLast(if (replacingTopChildren) 1 else 0)
            .reversed()
            .forEach { child ->
                println("${container.component?.key} remove view ${child.key}")
                cancelTransition(child)
                performChange(
                    from = child,
                    to = null,
                    isPush = isPush,
                    changeHandler = child.outChangeHandler
                )
            }

        // Add any new views to the backStack from bottom to top
        addedChildren
            .dropLast(if (replacingTopChildren) 1 else 0)
            .forEachIndexed { i, child ->
                println("${container.component?.key} add view ${child.key}")
                performChange(
                    from = addedChildren.getOrNull(i - 1),
                    to = child,
                    isPush = true,
                    changeHandler = child.inChangeHandler
                )
            }

        // Replace the old visible top with the new one
        if (replacingTopChildren) {
            val transition = if (isPush) newTopChild?.inChangeHandler
            else oldTopChild?.outChangeHandler

            println("${container.component?.key} replace top new ${newTopChild?.key} old ${oldTopChild?.key}")

            performChange(
                from = oldTopChild,
                to = newTopChild,
                isPush = isPush,
                changeHandler = transition
            )
        }
    }

    private fun cancelTransition(component: Component<*>) {
        runningTransitions.remove(component)?.cancel()
    }

    private fun performChange(
        from: Component<*>?,
        to: Component<*>?,
        isPush: Boolean,
        changeHandler: ComponentChangeHandler?
    ) {
        val handlerToUse = when {
            changeHandler == null -> DefaultChangeHandler()
            changeHandler.hasBeenUsed -> changeHandler.copy()
            else -> changeHandler
        }
        handlerToUse.hasBeenUsed = true

        println("${container.component?.key} perform change from ${from?.key} to ${to?.key} is push $isPush changeHandler $handlerToUse")

        from?.let { cancelTransition(it) }
        to?.let { runningTransitions[it] = handlerToUse }

        val fromView = viewsByChild[from]

        val toView = if (to != null) {
            viewsByChild.getOrPut(to) {
                to.performCreateView(container)
            }
        } else {
            null
        }

        (to as? Component<View>)?.bindView(toView!!)

        val changeData = ComponentChangeHandler.ChangeData(
            container = container,
            from = fromView,
            to = toView,
            isPush = isPush,
            onComplete = {
                if (to != null) runningTransitions -= to
                if (from != null) {
                    (from as Component<View>).unbindView(fromView!!)
                    viewsByChild.remove(from)
                }
            }
        )

        handlerToUse.execute(changeData)
    }

}