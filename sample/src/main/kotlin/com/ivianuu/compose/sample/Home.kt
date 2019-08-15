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

import android.graphics.PorterDuff
import android.view.View
import androidx.ui.graphics.Color
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.common.RecyclerView
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.navigator
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.Scaffold
import kotlinx.android.synthetic.main.home_item.view.*

fun HomeRoute() = Route {
    Scaffold(
        appBar = { AppBar("Home") },
        content = {
            RecyclerView {
                HomeItem.values()
                    .forEach { HomeItem(it) }
            }
        }
    )
}

private enum class HomeItem(
    val title: String,
    val color: Color,
    val route: ComponentComposition.() -> Route
) {
    Counter(
        title = "Counter",
        color = Color.Magenta,
        route = { CounterRoute() }
    ),
    List(
        title = "List",
        color = Color.Maroon,
        route = { ListRoute() }
    ),
    Pager(
        title = "Pager",
        color = Color.Cyan,
        route = { PagerRoute() }
    ),
    Transition(
        title = "ChangeHandlers",
        color = Color.Red,
        route = { TransitionDemos() }
    ),
    Animation(
        title = "Animation",
        color = Color.Yellow,
        route = { AnimationRoute() }
    )
}

private fun ComponentComposition.HomeItem(item: HomeItem) {
    val navigator = navigator
    val route = item.route(this)
    View<View>(key = item) {
        layoutRes(R.layout.home_item)
        bindView {
            home_title.text = item.title
            home_color.setColorFilter(item.color.toArgb(), PorterDuff.Mode.SRC_IN)
            setOnClickListener { navigator.push(route) }
        }
    }
}