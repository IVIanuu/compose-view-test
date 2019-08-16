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

import android.graphics.drawable.Drawable
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.set
import com.ivianuu.compose.setBy

fun ComponentBuilder<*>.background(
    drawable: Drawable? = null,
    color: Int? = null,
    res: Int? = null
) {
    setBy(drawable, color, res) {
        when {
            drawable != null -> background = drawable
            color != null -> setBackgroundColor(color)
            res != null -> setBackgroundResource(res)
            else -> background = null
        }
    }
}

fun ComponentBuilder<*>.padding(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    setBy(left, top, right, bottom) {
        setPaddingRelative(
            left ?: paddingStart,
            top ?: paddingTop,
            right ?: paddingEnd,
            bottom ?: paddingBottom
        )
    }
}

fun ComponentBuilder<*>.padding(padding: Int) {
    padding(left = padding, top = padding, right = padding, bottom = padding)
}

fun ComponentBuilder<*>.onClick(onClick: () -> Unit) {
    set(onClick) { setOnClickListener { it() } }
}

fun ComponentBuilder<*>.onLongClick(onLongClick: () -> Unit) {
    set(onLongClick) { setOnLongClickListener { onLongClick(); true } }
}