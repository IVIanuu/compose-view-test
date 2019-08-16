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

package com.ivianuu.compose.common.dsl

import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ContextAmbient
import com.ivianuu.compose.View
import com.ivianuu.compose.ambient
import com.ivianuu.compose.set
import com.ivianuu.compose.setBy

fun ComponentComposition.TextView(block: (ComponentBuilder<AppCompatTextView>.() -> Unit)? = null) {
    View(block = block)
}

fun <T : TextView> ComponentBuilder<T>.text(
    text: String? = null,
    res: Int? = null
) {
    setBy(text, res) {
        when {
            text != null -> this.text = text
            res != null -> setText(res)
            else -> this.text = null
        }
    }
}

fun <T : TextView> ComponentBuilder<T>.textAppearance(res: Int) {
    val context = ambient(ContextAmbient)
    set(res) { setTextAppearance(context, res) }
}

fun <T : TextView> ComponentBuilder<T>.textColor(color: Int) {
    set(color) { setTextColor(color) }
}

fun <T : TextView> ComponentBuilder<T>.maxLines(maxLines: Int) {
    set(maxLines) { setMaxLines(maxLines) }
}