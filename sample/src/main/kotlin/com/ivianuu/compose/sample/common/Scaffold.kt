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
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ViewById
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.sample.R

fun ComponentComposition.Scaffold(
    appBar: (ComponentComposition.() -> Unit)? = null,
    content: (ComponentComposition.() -> Unit)? = null
) {
    ViewByLayoutRes<LinearLayout>(layoutRes = R.layout.scaffold) {
        ViewById<FrameLayout>(id = R.id.app_bar) { appBar?.invoke(composition) }
        ViewById<FrameLayout>(id = R.id.content) { content?.invoke(composition) }
    }
}