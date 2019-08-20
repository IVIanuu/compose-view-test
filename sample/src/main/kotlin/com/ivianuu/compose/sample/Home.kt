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
import android.widget.ImageView
import androidx.ui.graphics.Color
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.Hidden
import com.ivianuu.compose.ViewById
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.common.RecyclerView
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.navigator
import com.ivianuu.compose.key
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.set
import com.ivianuu.compose.state
import kotlinx.android.synthetic.main.checkable.view.*
import kotlinx.android.synthetic.main.home_item.view.*

fun HomeRoute() = Route(keepState = true) {
    val (checked, setChecked) = state { false }

    Scaffold(
        appBar = { AppBar("Home") },
        content = {
            RecyclerView {
                key(1) {
                    Checkable(checked, setChecked)
                }
                key(2) {
                    Hidden(checked) {
                        Checkable(checked, setChecked)
                    }
                }
                HomeItem.values().forEach { item -> HomeItem(item = item) }
            }
        }
    )
}

private fun ComponentComposition.Checkable(
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    ViewByLayoutRes<View>(layoutRes = R.layout.checkable) {
        set(value) {
            title.text = "Checked: $value"
            checkbox.isChecked = value
            setOnClickListener { onChange(!value) }
        }
    }
}

private enum class HomeItem(
    val title: String,
    val color: Color,
    val route: () -> Route
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
    MultipleChildren(
        title = "Multiple children",
        color = Color.Lime,
        route = { MultipleChildrenRoute() }
    ),
    Pager(
        title = "Pager",
        color = Color.Cyan,
        route = { PagerRoute() }
    ),
    Transition(
        title = "Transitions",
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
    key(item) {
        val navigator = navigator
        val route = item.route()

        ViewByLayoutRes<View>(layoutRes = R.layout.home_item) {
            set(item) {
                home_title.text = item.title
                setOnClickListener { navigator.push(route) }
            }

            ViewById<View>(id = R.id.home_color_container) {
                ViewByLayoutRes<ImageView>(layoutRes = R.layout.home_color) {
                    set(item.color) {
                        setColorFilter(
                            it.toArgb(),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }
            }
        }
    }
}