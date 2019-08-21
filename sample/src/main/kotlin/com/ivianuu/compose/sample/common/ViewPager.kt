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

import androidx.viewpager2.widget.ViewPager2
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.common.ComposeRecyclerViewAdapter
import com.ivianuu.compose.init
import com.ivianuu.compose.onLayoutChildViews
import com.ivianuu.compose.onUnbindView
import com.ivianuu.compose.set

fun ComponentComposition.ViewPager(
    selectedPage: Int,
    onPageChanged: (Int) -> Unit,
    children: ComponentComposition.() -> Unit
) {
    View<ViewPager2> {
        init { adapter = ComposeRecyclerViewAdapter() }

        val component = component
        onLayoutChildViews { (it.adapter as ComposeRecyclerViewAdapter).submitList(component.visibleChildren) }

        set(selectedPage) { currentItem = selectedPage }

        init {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onPageChanged(position)
                }
            })
        }

        onUnbindView { it.adapter = null }

        children()
    }
}