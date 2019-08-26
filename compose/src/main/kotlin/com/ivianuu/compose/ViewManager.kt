/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.internal.log
import com.ivianuu.compose.internal.tagKey

class ViewManager(val key: Any, val container: ViewGroup) {

    private data class ViewKeyWithSlot(
        val viewKey: Any,
        val slot: Any
    )

    private val children = mutableListOf<Component<*>>()
    private val slotsByChild = mutableMapOf<Component<*>, Any>()

    private val runningTransitions = mutableMapOf<Component<*>, ComponentChangeHandler>()
    private val views = mutableMapOf<ViewKeyWithSlot, View>()

    fun update(newChildren: List<Component<*>>, isPush: Boolean, animate: Boolean) {
        log { "view manager: $key -> update new ${newChildren.map { it.key }} old ${children.map { it.key }}" }

        if (children == newChildren) return

        val oldChildren = children.toList()
        val removedChildren = oldChildren.filter { it !in newChildren }
        val addedChildren = newChildren.filter { it !in oldChildren }

        val childrenToAdd = addedChildren
            .filter { newChild ->
                removedChildren.none { oldChild ->
                    oldChild.viewKey == newChild.viewKey && slotsByChild.getValue(oldChild) == newChild.slot
                            && oldChild.shareViews && newChild.shareViews
                }
            }
        val childrenToRemove = removedChildren
            .filter { oldChild ->
                newChildren.none { newChild ->
                    oldChild.viewKey == newChild.viewKey && slotsByChild.getValue(oldChild) == newChild.slot
                            && oldChild.shareViews && newChild.shareViews
                }
            }
        val childrenToReplace = addedChildren
            .filter { it !in childrenToAdd }
            .map { newChild ->
                newChild to removedChildren.first { oldChild ->
                    newChild.viewKey == oldChild.viewKey && newChild.slot == slotsByChild.getValue(
                        oldChild
                    )
                }
            }

        log { "view manager $key to add ${childrenToAdd.map { it.key }} to remove ${childrenToRemove.map { it.key }} to replace ${childrenToReplace.map { it.first.key.toString() + "=:=" + it.second.key }}" }

        children.clear()
        children += newChildren
        slotsByChild.clear()
        children.forEach {
            slotsByChild[it] = it.slot!!
        }

        val oldTopChild = childrenToRemove.lastOrNull()
        val newTopChild = childrenToAdd.lastOrNull()

        // check if we should animate the top views
        val changeTopChildren =
            newTopChild != null && oldTopChild != null && newTopChild != oldTopChild
                    && oldTopChild !in newChildren
                    && newTopChild !in oldChildren

        // Remove all views which are not present anymore from top to bottom
        childrenToRemove
            .dropLast(if (changeTopChildren) 1 else 0)
            .reversed()
            .forEach { child ->
                log { "view manager: $key -> remove old ${child.key}" }

                cancelTransition(child)
                performChange(
                    from = child,
                    to = null,
                    isPush = false,
                    changeHandler = if (!animate) DefaultChangeHandler() else child.outChangeHandler
                )
            }

        // Add any new views to the backStack from bottom to top
        childrenToAdd
            .dropLast(if (changeTopChildren) 1 else 0)
            .forEach { child ->
                log { "view manager: $key -> add new ${child.key}" }

                performChange(
                    from = null,
                    to = child,
                    isPush = true,
                    changeHandler = if (!animate) DefaultChangeHandler() else child.inChangeHandler
                )
            }

        // Replace the old visible top with the new one
        if (changeTopChildren) {
            log { "view manager: $key -> replace top new ${newTopChild?.key} old ${oldTopChild?.key}" }
            val transition = when {
                !animate -> DefaultChangeHandler()
                isPush -> newTopChild?.inChangeHandler
                else -> oldTopChild?.outChangeHandler
            }

            performChange(
                from = oldTopChild,
                to = newTopChild,
                isPush = isPush,
                changeHandler = transition
            )
        }

        childrenToReplace.forEach { (newChild, oldChild) ->
            log { "view manager: $key -> rebind new ${newChild.key} old ${oldChild.key}" }

            cancelTransition(oldChild)

            val view = views.getValue(ViewKeyWithSlot(newChild.viewKey, newChild.slot!!))
            oldChild as Component<View>
            oldChild.unbindView(view, false)
            newChild as Component<View>
            newChild.bindView(view, false)
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

        log { "view manager: $key -> perform change from ${from?.key} to ${to?.key} is push $isPush changeHandler $handlerToUse" }

        from?.let { cancelTransition(it) }
        to?.let { runningTransitions[it] = handlerToUse }

        val fromView = from?.let { views[ViewKeyWithSlot(it.viewKey, it.slot!!)] }

        val toView = if (to != null) {
            if (from?.viewKey != to.viewKey) {
                views.getOrPut(ViewKeyWithSlot(to.viewKey, to.slot!!)) {
                    val view = to.createView(container)
                    to as Component<View>
                    to.bindView(view, true)
                    view
                }
            } else {
                val view = to.createView(container)
                to as Component<View>
                to.bindView(view, true)
                views[ViewKeyWithSlot(to.viewKey, to.slot!!)] = view
                view
            }
        } else {
            null
        }

        val changeData = ComponentChangeHandler.ChangeData(
            container = container,
            from = fromView,
            to = toView,
            isPush = isPush,
            callback = object : ComponentChangeHandler.Callback {
                override fun addToView() {
                    if (toView != null) {
                        if (!to!!.byId && toView.parent == null) {
                            if (isPush || from == null) {
                                container.addView(toView)
                            } else {
                                container.addView(toView, container.indexOfChild(fromView))
                            }
                        }
                    }
                }

                override fun removeFromView() {
                    if (fromView != null) {
                        if (!from.byId) container.removeView(fromView)
                        from as Component<View>
                        from.unbindView(fromView, true)

                        // only remove the view if the view ids are not the same
                        if (to?.viewKey != from.viewKey) {
                            views.remove(ViewKeyWithSlot(from.viewKey, from.slot!!))
                        }
                    }
                }

                override fun onComplete() {
                    if (to != null) runningTransitions -= to
                }
            }
        )

        handlerToUse.execute(changeData)
    }

}

private val viewManagerKey = tagKey("viewManager")

fun ViewGroup.getViewManager(component: Component<*>): ViewManager {
    var viewManager = getTag(viewManagerKey) as? ViewManager
    if (viewManager == null) {
        viewManager = ViewManager(component.key, this)
        setTag(viewManagerKey, viewManager)
    }

    return viewManager

}