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
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.ViewById
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.ambient
import com.ivianuu.compose.common.Navigator
import com.ivianuu.compose.common.NavigatorAmbient
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.FadeChangeHandler
import com.ivianuu.compose.common.changehandler.HorizontalChangeHandler
import com.ivianuu.compose.key
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.set
import kotlinx.android.synthetic.main.navigation.view.*

fun MultipleChildrenRoute() = Route {
    ChangeHandlers(handler = FadeChangeHandler()) {
        Scaffold(
            appBar = { AppBar("Multiple children") },
            content = {
                ViewByLayoutRes<View>(layoutRes = R.layout.multiple_children) {
                    listOf(R.id.container_0, R.id.container_1, R.id.container_2)
                        .forEachIndexed { index, containerId ->
                            key(containerId) {
                                ViewById<View>(id = containerId) {
                                    Navigator {
                                        ChildRoute(index = 0)
                                    }
                                }
                            }
                        }
                }
            }
        )
    }
}

// todo fix will re use view???

private fun ChildRoute(index: Int): Route = Route(key = index) {
    ChangeHandlers(handler = HorizontalChangeHandler()) {
        ViewByLayoutRes<View>(layoutRes = R.layout.navigation) {
            val navigator = ambient(NavigatorAmbient)
            set(index) {
                title.text = "#$index"
                next_button.setOnClickListener { navigator.push(ChildRoute(index = index + 1)) }
                pop_to_root_button.setOnClickListener {
                    while (navigator.backStack.size > 1) {
                        navigator.pop()
                    }
                }
            }
        }
    }
}