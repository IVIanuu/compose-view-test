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

import android.widget.TextView
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.common.RecyclerView
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.VerticalChangeHandler
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.set

fun ListRoute() = Route {
    ChangeHandlers(handler = VerticalChangeHandler()) {
        Scaffold(
            appBar = { AppBar("List") },
            content = {
                RecyclerView {
                    (0..100).forEach {
                        ListItem("Title $it")
                    }
                }
            }
        )
    }
}

private fun ComponentComposition.ListItem(text: String) {
    ViewByLayoutRes<TextView>(key = text, layoutRes = R.layout.list_item) {
        set(text) { this.text = it }
    }
}