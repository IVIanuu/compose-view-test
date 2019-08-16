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

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.VERTICAL
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.common.dsl.FrameLayout
import com.ivianuu.compose.common.dsl.LinearLayout
import com.ivianuu.compose.common.dsl.layoutHeight
import com.ivianuu.compose.common.dsl.layoutSize
import com.ivianuu.compose.common.dsl.layoutWidth
import com.ivianuu.compose.common.dsl.orientation

fun ComponentComposition.Scaffold(
    appBar: (ComponentComposition.() -> Unit)? = null,
    content: (ComponentComposition.() -> Unit)? = null
) {
    LinearLayout {
        layoutSize(MATCH_PARENT)
        orientation(VERTICAL)

        FrameLayout {
            layoutWidth(MATCH_PARENT)
            layoutHeight(WRAP_CONTENT)
            appBar?.invoke(this)
        }

        FrameLayout {
            layoutSize(MATCH_PARENT)
            content?.invoke(this)
        }
    }
}