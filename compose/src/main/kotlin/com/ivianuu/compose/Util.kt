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

package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

var loggingEnabled = true

inline fun log(block: () -> String) {
    if (loggingEnabled) {
        println(block())
    }
}

inline fun sourceLocation(): String {
    val element = Throwable().stackTrace.first()
    return "${element.className}:${element.methodName}:${element.lineNumber}"
}

fun ViewGroup.children(): List<View> {
    return (0 until childCount)
        .map { getChildAt(it) }
}

internal fun tagKey(key: String): Int = (3 shl 24) or key.hashCode()

private val componentKey = tagKey("component")

var View.component: Component<*>?
    get() = getTag(componentKey) as? Component<*>
    set(value) {
        setTag(componentKey, value)
    }

private val byIdKey = tagKey("byId")

internal var View.byId: Boolean
    get() = getTag(byIdKey) as? Boolean ?: false
    set(value) {
        setTag(byIdKey, value)
    }