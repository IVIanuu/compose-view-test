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
import androidx.compose.memo
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.RecyclerView
import com.ivianuu.compose.common.changehandler.VerticalChangeHandler
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold

fun ViewComposition.List() = Route {
    val handler = +memo { VerticalChangeHandler() }
    ChangeHandlers(handler = handler) {
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

private fun ViewComposition.ListItem(text: String) {
    View<TextView>(key = text) {
        layoutRes(R.layout.list_item)
        bindView { this.text = text }
    }
}