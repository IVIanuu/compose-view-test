package com.ivianuu.compose.sample

import android.view.View
import androidx.compose.memo
import androidx.compose.state
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.HorizontalChangeHandler
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import kotlinx.android.synthetic.main.counter.view.*

fun ViewComposition.Counter() = Route {
    val transition = +memo { HorizontalChangeHandler() }

    Transitions(changeHandler = transition) {
        Scaffold(
            appBar = { AppBar("Counter") },
            content = {
                val (count, setCount) = +state { 0 }
                InflateView<View>(layoutRes = R.layout.counter, updateView = {
                    title.text = "Count: $count"
                    inc.setOnClickListener { setCount(count + 1) }
                    dec.setOnClickListener { setCount(count - 1) }
                })
            }
        )
    }
}