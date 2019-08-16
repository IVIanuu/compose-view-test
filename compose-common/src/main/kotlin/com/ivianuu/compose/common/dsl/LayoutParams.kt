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

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.set
import com.ivianuu.compose.setBy

fun ComponentBuilder<*>.layoutWidth(width: Int) {
    set(width) {
        layoutParams = layoutParams.apply {
            this.width = width
        }
    }
}

fun ComponentBuilder<*>.layoutHeight(height: Int) {
    set(height) {
        layoutParams = layoutParams.apply {
            this.height = height
        }
    }
}

fun ComponentBuilder<*>.layoutSize(size: Int) {
    layoutWidth(size)
    layoutHeight(size)
}

fun ComponentBuilder<*>.layoutMargin(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    setBy(left, top, right, bottom) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = left ?: marginStart
            topMargin = top ?: topMargin
            marginEnd = right ?: marginEnd
            bottomMargin = bottom ?: bottomMargin
        }
    }
}

fun ComponentBuilder<*>.layoutMargin(margin: Int) {
    layoutMargin(left = margin, top = margin, right = margin, bottom = margin)
}

fun ComponentBuilder<*>.layoutGravity(gravity: Int) {
    set(gravity) {
        layoutParams = layoutParams.apply {
            when (this) {
                is FrameLayout.LayoutParams -> this.gravity = it
                is LinearLayout.LayoutParams -> this.gravity = it
                else -> error("Cannot set layout gravity to $this")
            }
        }
    }
}