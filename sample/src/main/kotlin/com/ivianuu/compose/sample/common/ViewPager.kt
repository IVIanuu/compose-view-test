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

import androidx.compose.State
import androidx.viewpager2.widget.ViewPager2
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.common.ComposeRecyclerViewAdapter
import com.ivianuu.compose.init
import com.ivianuu.compose.onUpdateChildViews
import com.ivianuu.compose.set
import com.ivianuu.compose.state

fun ComponentComposition.ViewPager(
    selectedPage: Int,
    onPageChanged: (Int) -> Unit,
    children: ComponentComposition.() -> Unit
) {
    View<ViewPager2> {
        init { adapter = ComposeRecyclerViewAdapter() }

        val component = component
        onUpdateChildViews { view, _ ->
            (view.adapter as ComposeRecyclerViewAdapter)
                .submitList(component.visibleChildren)
        }

        set(selectedPage) { currentItem = selectedPage }

        init {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onPageChanged(position)
                }
            })
        }

        // todo onUnbindView { it.adapter = null }

        children()
    }
}

fun ComponentComposition.ViewPager(
    initialPage: Int,
    children: ComponentComposition.() -> Unit
) {
    val controller = ViewPagerController(state { initialPage })
    ViewPager(controller = controller, children = children)
}

fun ComponentComposition.ViewPager(
    controller: ViewPagerController,
    children: ComponentComposition.() -> Unit
) {
    ViewPager(
        selectedPage = controller.selectedPage,
        onPageChanged = { controller.selectedPage = it },
        children = children
    )
}

class ViewPagerController(private val state: State<Int>) {

    var selectedPage
        get() = state.value
        set(value) {
            state.value = value
        }

}