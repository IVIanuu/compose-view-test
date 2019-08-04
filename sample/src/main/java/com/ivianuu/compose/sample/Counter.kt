package com.ivianuu.compose.sample

import android.view.View
import androidx.compose.memo
import androidx.ui.graphics.Color
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.HorizontalViewTransition
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.navigator
import kotlinx.android.synthetic.main.counter.view.*

private val Colors = arrayOf(
    Color.Aqua,
    Color.Fuchsia,
    Color.Red,
    Color.Green,
    Color.Cyan,
    Color.Magenta
)

private var lastColor: Color? = null

fun nextColor() = Colors
    .filter { it != lastColor }
    .shuffled()
    .first()
    .also { lastColor = it }

fun ViewComposition.Counter(
    count: Int,
    color: Color
): Route = Route(key = "Count $count") {
    val navigator = navigator()

    val transition = +memo { HorizontalViewTransition() }

    Transitions(transition = transition) {
        Scaffold(
            appBar = { AppBar("Counter") },
            content = {
                InflateView<View>(layoutRes = R.layout.counter, updateView = {
                    setBackgroundColor(color.toArgb())
                    title.text = "Count: $count"
                    inc.setOnClickListener {
                        navigator.push(Counter(count + 1, nextColor()))
                    }
                    dec.setOnClickListener { navigator.pop() }
                })
            }
        )
    }
}
