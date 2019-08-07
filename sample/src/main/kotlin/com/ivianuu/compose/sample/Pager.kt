package com.ivianuu.compose.sample

import android.view.View
import androidx.ui.graphics.Color
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.FadeChangeHandler
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.memo
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.TabItem
import com.ivianuu.compose.sample.common.TabLayout
import com.ivianuu.compose.sample.common.ViewPager
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

fun ViewComposition.Pager() = Route {
    val transition = memo { FadeChangeHandler() }
    Transitions(changeHandler = transition) {
        var selectedPage by state { 0 }

        Scaffold(
            appBar = {
                TabLayout(
                    selectedTab = selectedPage,
                    onTabChanged = { selectedPage = it },
                    children = {
                        (1..5).forEach { i ->
                            TabItem("Tab $i")
                        }
                    }
                )
            },
            content = {
                ViewPager(
                    selectedPage = selectedPage,
                    onPageChanged = { selectedPage = it },
                    children = {
                        (1..5).forEach { i ->
                            group(i) {
                                val color = memo { AllColors.toList().shuffled()[i] }
                                Page(i, color)
                            }
                        }
                    }
                )
            }
        )
    }
}

private fun ViewComposition.Page(
    index: Int,
    color: Color
) {
    View<View> {
        layoutRes(R.layout.page)
        updateView {
            page_bg.setBackgroundColor(color.toArgb())
            page_text.text = "#$index"
        }
    }
}
