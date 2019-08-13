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

package com.ivianuu.compose.sample.common

import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.byId
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.R

fun ViewComposition.Scaffold(
    appBar: (ViewComposition.() -> Unit)? = null,
    content: (ViewComposition.() -> Unit)? = null
) {
    View<LinearLayout> {
        layoutRes(R.layout.scaffold)
        View<FrameLayout> {
            byId(R.id.app_bar)
            appBar?.invoke(this@Scaffold)
        }
        View<FrameLayout> {
            byId(R.id.content)
            content?.invoke(this@Scaffold)
        }
    }
}