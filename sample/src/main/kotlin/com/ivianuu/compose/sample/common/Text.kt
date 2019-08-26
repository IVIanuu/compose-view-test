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

import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.Ambient
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.ambient
import com.ivianuu.compose.set
import com.ivianuu.compose.setBy

val TextAppearanceAmbient = Ambient.of<Int?>(key = "TextAppearance")
val TextColorAmbient = Ambient.of<Int?>(key = "TextColor")

fun ComponentComposition.TextStyle(
    textAppearance: Int? = null,
    textColor: Int? = null,
    children: ComponentComposition.() -> Unit
) {
    TextAppearanceAmbient.Provider(value = textAppearance) {
        TextColorAmbient.Provider(value = textColor) {
            children()
        }
    }
}

fun ComponentComposition.Text(
    text: String? = null,
    textRes: Int? = null,
    textAppearance: Int? = ambient(TextAppearanceAmbient),
    textColor: Int? = ambient(TextColorAmbient)
) {
    View<AppCompatTextView> {
        set(textAppearance) {
            if (it != null) {
                setTextAppearance(context, it)
            }
        }

        set(textColor) {
            if (it != null) {
                setTextColor(it)
            }
        }

        setBy(text, textRes) {
            when {
                text != null -> this.text = text
                textRes != null -> setText(textRes)
                else -> this.text = null
            }
        }
    }
}