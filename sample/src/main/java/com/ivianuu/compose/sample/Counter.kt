package com.ivianuu.compose.sample

import android.view.View
import androidx.compose.ambient
import androidx.compose.memo
import androidx.ui.graphics.Color
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.transition.HorizontalViewTransition
import com.ivianuu.compose.transition.inTransition
import com.ivianuu.compose.transition.outTransition
import com.ivianuu.compose.util.sourceLocation
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

fun ViewComposition.Counter(count: Int): Route = Route(key = sourceLocation() + count) {
    val navigator = +ambient(NavigatorAmbient)
    val color = +memo {
        Colors
            .filter { it != lastColor }
            .shuffled()
            .first()
            .also { lastColor = it }
    }

    InflateView<View>(key = "Counter $count", layoutRes = R.layout.counter) {
        val transition = +memo { HorizontalViewTransition() }
        inTransition = transition
        outTransition = transition
        setBackgroundColor(color.toArgb())
        title.text = "Count: $count"
        inc.setOnClickListener {
            navigator.push(Counter(count + 1))
        }
        dec.setOnClickListener { navigator.pop() }
    }
}
