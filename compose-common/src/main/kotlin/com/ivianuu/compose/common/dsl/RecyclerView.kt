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
import android.widget.RelativeLayout.*
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View
import com.ivianuu.compose.setBy

fun ComponentComposition.RecyclerView(block: (ComponentBuilder<RecyclerView>.() -> Unit)? = null) {
    View(block = block)
}

fun <T : View> ComponentBuilder<T>.relativeLayoutRule(verb: Int, subject: Int) {
    setBy(verb, subject) {
        layoutParams = (layoutParams as LayoutParams).apply {
            addRule(verb, subject)
        }
    }
}

fun <T : View> ComponentBuilder<T>.toLeftOf(id: Int) = relativeLayoutRule(START_OF, id)
fun <T : View> ComponentBuilder<T>.toRightOf(id: Int) = relativeLayoutRule(END_OF, id)

fun <T : View> ComponentBuilder<T>.above(id: Int) = relativeLayoutRule(ABOVE, id)
fun <T : View> ComponentBuilder<T>.below(id: Int) = relativeLayoutRule(BELOW, id)

fun <T : View> ComponentBuilder<T>.alignBaseline(id: Int) = relativeLayoutRule(ALIGN_BASELINE, id)

fun <T : View> ComponentBuilder<T>.alignLeft(id: Int) = relativeLayoutRule(ALIGN_START, id)
fun <T : View> ComponentBuilder<T>.alignTop(id: Int) = relativeLayoutRule(ALIGN_TOP, id)
fun <T : View> ComponentBuilder<T>.alignRight(id: Int) = relativeLayoutRule(ALIGN_RIGHT, id)
fun <T : View> ComponentBuilder<T>.alignBottom(id: Int) = relativeLayoutRule(ALIGN_BOTTOM, id)

fun <T : View> ComponentBuilder<T>.alignParentLeft() = relativeLayoutRule(ALIGN_PARENT_START, TRUE)
fun <T : View> ComponentBuilder<T>.alignParenTop() = relativeLayoutRule(ALIGN_PARENT_TOP, TRUE)
fun <T : View> ComponentBuilder<T>.alignParentRight() = relativeLayoutRule(ALIGN_PARENT_END, TRUE)
fun <T : View> ComponentBuilder<T>.alignParentBottom() =
    relativeLayoutRule(ALIGN_PARENT_BOTTOM, TRUE)

fun <T : View> ComponentBuilder<T>.centerHorizontal() = relativeLayoutRule(CENTER_HORIZONTAL, TRUE)
fun <T : View> ComponentBuilder<T>.centerVertical() = relativeLayoutRule(CENTER_VERTICAL, TRUE)
fun <T : View> ComponentBuilder<T>.centerInParent() = relativeLayoutRule(CENTER_IN_PARENT, TRUE)