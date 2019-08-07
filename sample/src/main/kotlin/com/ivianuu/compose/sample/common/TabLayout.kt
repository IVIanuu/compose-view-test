package com.ivianuu.compose.sample.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.ivianuu.compose.Component
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.getViewManager
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.R
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.TabLayout(
    selectedIndex: Int,
    noinline onTabChanged: (Int) -> Unit,
    noinline children: ViewComposition.() -> Unit
) {
    TabLayout(sourceLocation(), selectedIndex, onTabChanged, children)
}

fun ViewComposition.TabLayout(
    key: Any,
    selectedTab: Int,
    onTabChanged: (Int) -> Unit,
    children: ViewComposition.() -> Unit
) {
    emit(
        key = key,
        ctor = { TabLayoutComponent() },
        update = {
            this.selectedTab = selectedTab
            this.onTabChanged = onTabChanged
            children()
        }
    )
}

class TabLayoutComponent : Component<TabLayout>() {

    var selectedTab = 0
    lateinit var onTabChanged: (Int) -> Unit

    override fun createView(container: ViewGroup) =
        LayoutInflater.from(container.context).inflate(
            R.layout.tab_layout,
            container,
            false
        ) as TabLayout

    override fun bindView(view: TabLayout) {
        super.bindView(view)

        children
            .mapIndexed { i, child ->
                var tab = view.getTabAt(i)
                if (tab == null) {
                    tab = view.newTab()
                    tab.customView = FrameLayout(view.context).apply {
                        layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                    }
                    view.addTab(tab, i)
                }

                child to tab
            }
            .forEach { (child, tab) ->
                (tab.customView as ViewGroup).getViewManager().update(listOf(child), true) // todo
            }

        while (view.tabCount > children.size) {
            view.removeTabAt(view.tabCount - 1)
        }

        view.selectTab(view.getTabAt(selectedTab))

        view.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                onTabChanged(tab.position)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun unbindView(view: TabLayout) {
        (0 until view.tabCount)
            .forEach {
                (view.getTabAt(it)!!.customView as FrameLayout)
                    .getViewManager().clear()
            }
        view.removeAllTabs()
        super.unbindView(view)
    }
}

fun ViewComposition.TabItem(text: String) {
    View<TextView>(text) {
        layoutRes(R.layout.tab_item)
        updateView { this.text = text }
    }
}