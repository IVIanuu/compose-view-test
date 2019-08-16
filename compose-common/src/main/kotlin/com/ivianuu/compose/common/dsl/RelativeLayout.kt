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

import android.view.View
import android.widget.RelativeLayout
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.setBy

fun ComponentComposition.RelativeLayout(block: (ComponentBuilder<RelativeLayout>.() -> Unit)? = null) {
    View(block = block)
}

fun <T : View> ComponentBuilder<T>.relativeLayoutRule(verb: Int, subject: Int) {
    setBy(verb, subject) {
        layoutParams = (layoutParams as RelativeLayout.LayoutParams).apply {
            addRule(verb, subject)
        }
    }
}

fun <T : View> ComponentBuilder<T>.toLeftOf(id: Int) =
    relativeLayoutRule(RelativeLayout.START_OF, id)

fun <T : View> ComponentBuilder<T>.toRightOf(id: Int) =
    relativeLayoutRule(RelativeLayout.END_OF, id)

fun <T : View> ComponentBuilder<T>.above(id: Int) = relativeLayoutRule(RelativeLayout.ABOVE, id)
fun <T : View> ComponentBuilder<T>.below(id: Int) = relativeLayoutRule(RelativeLayout.BELOW, id)

fun <T : View> ComponentBuilder<T>.alignBaseline(id: Int) =
    relativeLayoutRule(RelativeLayout.ALIGN_BASELINE, id)

fun <T : View> ComponentBuilder<T>.alignLeft(id: Int) =
    relativeLayoutRule(RelativeLayout.ALIGN_START, id)

fun <T : View> ComponentBuilder<T>.alignTop(id: Int) =
    relativeLayoutRule(RelativeLayout.ALIGN_TOP, id)

fun <T : View> ComponentBuilder<T>.alignRight(id: Int) =
    relativeLayoutRule(RelativeLayout.ALIGN_RIGHT, id)

fun <T : View> ComponentBuilder<T>.alignBottom(id: Int) =
    relativeLayoutRule(RelativeLayout.ALIGN_BOTTOM, id)

fun <T : View> ComponentBuilder<T>.alignParentLeft() = relativeLayoutRule(
    RelativeLayout.ALIGN_PARENT_START,
    RelativeLayout.TRUE
)

fun <T : View> ComponentBuilder<T>.alignParentTop() = relativeLayoutRule(
    RelativeLayout.ALIGN_PARENT_TOP,
    RelativeLayout.TRUE
)

fun <T : View> ComponentBuilder<T>.alignParentRight() = relativeLayoutRule(
    RelativeLayout.ALIGN_PARENT_END,
    RelativeLayout.TRUE
)

fun <T : View> ComponentBuilder<T>.alignParentBottom() =
    relativeLayoutRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)

fun <T : View> ComponentBuilder<T>.centerHorizontal() = relativeLayoutRule(
    RelativeLayout.CENTER_HORIZONTAL,
    RelativeLayout.TRUE
)

fun <T : View> ComponentBuilder<T>.centerVertical() = relativeLayoutRule(
    RelativeLayout.CENTER_VERTICAL,
    RelativeLayout.TRUE
)

fun <T : View> ComponentBuilder<T>.centerInParent() = relativeLayoutRule(
    RelativeLayout.CENTER_IN_PARENT,
    RelativeLayout.TRUE
)