package com.ivianuu.compose.sample

import android.view.View
import androidx.compose.memo
import androidx.ui.graphics.Color
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.common.Route
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

fun ViewComposition.Counter(count: Int): Route = Route(key = "Count $count") {
    val color = +memo {
        Colors
            .filter { it != lastColor }
            .shuffled()
            .first()
            .also { lastColor = it }
    }

    val navigator = navigator()

    val transition = +memo { com.ivianuu.compose.common.HorizontalViewTransition() }

    Transitions(transition = transition) {
        InflateView<View>(key = "Counter $count", layoutRes = R.layout.counter) {
            setBackgroundColor(color.toArgb())
            title.text = "Count: $count"
            inc.setOnClickListener {
                navigator.push(Counter(count + 1))
            }
            dec.setOnClickListener { navigator.pop() }
            list.setOnClickListener {
                navigator.push(List())
            }
        }
    }
}
