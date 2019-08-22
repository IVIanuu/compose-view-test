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

package com.ivianuu.compose.sample

import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import androidx.ui.graphics.Color
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.FadeChangeHandler
import com.ivianuu.compose.init
import com.ivianuu.compose.key
import com.ivianuu.compose.memo
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.TabItem
import com.ivianuu.compose.sample.common.TabLayout
import com.ivianuu.compose.sample.common.ViewPager
import com.ivianuu.compose.set
import com.ivianuu.compose.state
import kotlinx.android.synthetic.main.page.view.*

val AllColors = arrayOf(
    Color.Black,
    Color.DarkGray,
    Color.Gray,
    Color.LightGray,
    Color.White,
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Cyan,
    Color.Magenta,
    Color.Transparent,
    Color.Aqua,
    Color.Fuchsia,
    Color.Lime,
    Color.Maroon,
    Color.Navy,
    Color.Olive,
    Color.Purple,
    Color.Silver,
    Color.Teal
)

fun PagerRoute() = Route(handler = FadeChangeHandler()) {
    var selectedPage by state { 0 }

    Scaffold(
        appBar = {
            View<LinearLayout> {
                init {
                    layoutParams = layoutParams.apply {
                        width = MATCH_PARENT
                        height = WRAP_CONTENT
                    }
                    orientation = VERTICAL
                }

                AppBar(title = "Pager")

                TabLayout(
                    selectedIndex = selectedPage,
                    onTabChanged = { selectedPage = it },
                    children = {
                        (1..5).forEach { i ->
                            key(i) {
                                TabItem("Tab $i")
                            }
                        }
                    }
                )
            }
        },
        content = {
            ViewPager(
                selectedPage = selectedPage,
                onPageChanged = { selectedPage = it },
                children = {
                    (1..5).forEach { i ->
                        key(i) {
                            val color = memo { AllColors.toList().shuffled()[i] }
                            Page(i, color)
                        }
                    }
                }
            )
        }
    )
}

private fun ComponentComposition.Page(
    index: Int,
    color: Color
) {
    ViewByLayoutRes<View>(layoutRes = R.layout.page) {
        set(color) { page_bg.setBackgroundColor(it.toArgb()) }
        set(index) { page_text.text = "#$it" }
    }
}
