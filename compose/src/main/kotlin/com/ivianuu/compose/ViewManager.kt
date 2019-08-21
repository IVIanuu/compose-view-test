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
import com.ivianuu.compose.internal.component
import com.ivianuu.compose.internal.log
import com.ivianuu.compose.internal.tagKey

class ViewManager(val container: ViewGroup) {

    val children = mutableListOf<Component<*>>()
    private val runningTransitions = mutableMapOf<Component<*>, ComponentChangeHandler>()

    fun update(newChildren: List<Component<*>>, isPush: Boolean) {
        log { "view manager: ${container.component?.key} -> update new ${newChildren.map { it.key }} old ${children.map { it.key }}" }

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
                    && oldTopChild !in newChildren
                    && newTopChild !in oldChildren

        // Remove all views which are not present anymore from top to bottom
        removedChildren
            .dropLast(if (replacingTopChildren) 1 else 0)
            .reversed()
            .forEach { child ->
                log { "view manager: ${container.component?.key} -> remove old ${child.key}" }

                cancelTransition(child)
                performChange(
                    from = child,
                    to = null,
                    isPush = false,
                    changeHandler = child.outChangeHandler
                )
            }

        // Add any new views to the backStack from bottom to top
        addedChildren
            .dropLast(if (replacingTopChildren) 1 else 0)
            .forEach { child ->
                log { "view manager: ${container.component?.key} -> add new ${child.key}" }

                performChange(
                    from = null,
                    to = child,
                    isPush = true,
                    changeHandler = child.inChangeHandler
                )
            }

        // Replace the old visible top with the new one
        if (replacingTopChildren) {
            log { "view manager: ${container.component?.key} -> replace top new ${newTopChild?.key} old ${oldTopChild?.key}" }
            val transition = if (isPush) newTopChild?.inChangeHandler
            else oldTopChild?.outChangeHandler

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

        log { "view manager: ${container.component?.key} -> perform change from ${from?.key} to ${to?.key} is push $isPush changeHandler $handlerToUse" }

        from?.let { cancelTransition(it) }
        to?.let { runningTransitions[it] = handlerToUse }

        val fromView = from?.view

        val toView = to?.createView(container)

        val changeData = ComponentChangeHandler.ChangeData(
            container = container,
            from = fromView,
            to = toView,
            isPush = isPush,
            callback = object : ComponentChangeHandler.Callback {
                override fun addToView() {
                    if (toView != null) {
                        if (!to.byId && toView.parent == null) {
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
                        from.unbindView()
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

fun ViewGroup.getViewManager(): ViewManager {
    var viewManager = getTag(viewManagerKey) as? ViewManager
    if (viewManager == null) {
        viewManager = ViewManager(this)
        setTag(viewManagerKey, viewManager)
    }

    return viewManager

}