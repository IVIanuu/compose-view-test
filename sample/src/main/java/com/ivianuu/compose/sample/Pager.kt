package com.ivianuu.compose.sample

import android.view.View
import androidx.compose.memo
import androidx.ui.graphics.Color
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.FadeChangeHandler
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.ViewPager
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
    val transition = +memo { FadeChangeHandler() }
    Transitions(changeHandler = transition) {
        Scaffold(
            appBar = { AppBar("Pager") },
            content = {
                ViewPager {
                    (1..5).forEach { i ->
                        Page(i, AllColors.toList().shuffled()[i])
                    }
                }
            }
        )
    }
}

private fun ViewComposition.Page(
    index: Int,
    color: Color
) {
    InflateView<View>(layoutRes = R.layout.page, updateView = {
        page_bg.setBackgroundColor(color.toArgb())
        page_text.text = "#$index"
    })
}