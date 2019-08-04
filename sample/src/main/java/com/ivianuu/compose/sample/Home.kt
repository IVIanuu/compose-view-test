package com.ivianuu.compose.sample

import android.graphics.PorterDuff
import android.view.View
import androidx.ui.graphics.Color
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.common.RecyclerView
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.navigator
import com.ivianuu.compose.sample.transition.TransitionDemos
import kotlinx.android.synthetic.main.home_item.view.*

enum class HomeItem(
    val title: String,
    val color: Color,
    val route: ViewComposition.() -> Route
) {
    Counter(
        title = "Counter",
        color = Color.Magenta,
        route = { Counter() }
    ),
    Pager(
        title = "Pager",
        color = Color.Cyan,
        route = { Pager() }
    ),
    Transition(
        title = "Transitions",
        color = Color.Red,
        route = { TransitionDemos() }
    )
}

fun ViewComposition.Home() = Route {
    Scaffold(
        appBar = { AppBar("Home") },
        content = {
            RecyclerView {
                HomeItem.values()
                    .forEach {
                        group(it.name) {
                            HomeItem(it)
                        }
                    }
            }
        }
    )
}

private fun ViewComposition.HomeItem(item: HomeItem) {
    val navigator = navigator()
    val route = item.route(this)
    InflateView<View>(layoutRes = R.layout.home_item, updateView = {
        home_title.text = item.title
        home_color.setColorFilter(item.color.toArgb(), PorterDuff.Mode.SRC_IN)
        setOnClickListener { navigator.push(route) }
    })
}