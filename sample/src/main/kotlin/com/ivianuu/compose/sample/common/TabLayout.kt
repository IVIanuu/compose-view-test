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

package com.ivianuu.compose.sample.common

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.currentComponent
import com.ivianuu.compose.getViewManager
import com.ivianuu.compose.onBindView
import com.ivianuu.compose.onUnbindView
import com.ivianuu.compose.sample.R

fun ComponentComposition.TabLayout(
    selectedIndex: Int,
    onTabChanged: (Int) -> Unit,
    children: ComponentComposition.() -> Unit
) {
    ViewByLayoutRes<TabLayout>(layoutRes = R.layout.tab_layout, manageChildren = false) {
        val component = currentComponent<TabLayout>()
        onBindView {
            with(it) {
                component.children
                    .mapIndexed { i, child ->
                        var tab = getTabAt(i)
                        if (tab == null) {
                            tab = newTab()
                            tab.customView = FrameLayout(context).apply {
                                layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
                            }
                            addTab(tab, i)
                        }

                        child to tab
                    }
                    .forEach { (child, tab) ->
                        (tab.customView as ViewGroup).getViewManager()
                            .update(listOf(child), true)
                    }

                while (tabCount > component.children.size) {
                    removeTabAt(tabCount - 1)
                }

                selectTab(getTabAt(selectedIndex))

                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        onTabChanged(tab.position)
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                    }
                })
            }
        }

        onUnbindView {
            with(it) {
                (0 until tabCount)
                    .forEach {
                        (getTabAt(it)!!.customView as FrameLayout)
                            .getViewManager().clear()
                    }
                removeAllTabs()
            }
        }

        children()
    }
}

fun ComponentComposition.TabItem(text: String) {
    ViewByLayoutRes<TextView>(key = text, layoutRes = R.layout.tab_item) {
        onBindView<TextView> { it.text = text }
    }
}