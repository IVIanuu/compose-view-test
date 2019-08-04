package com.ivianuu.compose.sample.common

/**

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.google.android.material.tabs.TabLayout
import com.ivianuu.compose.Component
import com.ivianuu.compose.GroupComponent
import com.ivianuu.compose.InflateViewGroup
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.byId
import com.ivianuu.compose.children
import com.ivianuu.compose.component
import com.ivianuu.compose.getViewManager
import com.ivianuu.compose.sample.R
import android.view.ViewGroup.LayoutParams as LayoutParams1

private fun ViewComposition.TabLayout(
children: ViewComposition.() -> Unit
) {
InflateViewGroup<TabLayout>(layoutRes = R.layout.tab_layout, children = children)
}

class TabLayoutComponent : GroupComponent<TabLayout>() {

private val views = mutableListOf<TabLayout>()

override fun update() {
super.update()
views.forEach { updateView(it) }
}

override fun endChildren() {
super.endChildren()

views.forEach { view ->
val childViews = children
.map { child ->
view.children()
.firstOrNull { it.component == child }
?.also {
(child as Component<View>).updateView(it)
}
?: child.createView(view).also {
it.component = child
(child as Component<View>).updateView(it)
}
}

view.getViewManager()
.setViews(childViews, childViews.lastOrNull()?.component?.wasPush ?: true)
}
}

override fun createView(container: ViewGroup): TabLayout {
val view = TabLayout(container.context).apply {
layoutParams = LayoutParams1(MATCH_PARENT, MATCH_PARENT)
}
views.add(view)

val childViews = children.map { child ->
child.createView(view)
.also {
it.component = child
(child as Component<View>).updateView(it)
}
}

return view
}

override fun updateView(view: TabLayout) {
super.updateView(view)

children
.map { child ->
view.children()
.first { it.component == child }
}
.forEach { (it.component as Component<View>).updateView(it) }
}

override fun destroyView(view: TabLayout) {
super.destroyView(view)
val unprocessedChildren = children.toMutableList()
view.children().forEach { childView ->
val component = childView.component as Component<View>
unprocessedChildren.remove(component)
component.destroyView(childView)
childView.component = null
if (!childView.byId) {
view.removeView(childView)
}
}
check(unprocessedChildren.isEmpty()) { unprocessedChildren }
views.remove(view)
}

private fun ensu

}

private fun ViewComposition.Tab(
key: Any,
title: String
) {

}*/