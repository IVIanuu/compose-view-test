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

@PublishedApi
internal class RemoveCallbackHolder(var removeCallback: (() -> Unit)? = null)

@JvmName("onBindViewUntyped")
inline fun ComponentComposition.onBindView(
    vararg inputs: Any?,
    noinline callback: View.() -> Unit
) {
    onBindView<View>(inputs = *inputs, callback = callback)
}

inline fun <T : View> ComponentComposition.onBindView(
    vararg inputs: Any?,
    noinline callback: T.() -> Unit
) {
    onBindViewImpl(key = sourceLocation(), inputs = *inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentComposition.onBindViewImpl(
    key: Any,
    vararg inputs: Any?,
    callback: T.() -> Unit
) {
    key(key) {
        val component = currentComponent<T>()
        val removeCallbackHolder = memo { RemoveCallbackHolder(null) }
        memo(*inputs) {
            removeCallbackHolder.removeCallback?.invoke()
            removeCallbackHolder.removeCallback = component.onBindView(callback)
        }
        onDispose { removeCallbackHolder.removeCallback?.invoke() }
    }
}

@JvmName("onUnbindViewUntyped")
inline fun ComponentComposition.onUnbindView(
    vararg inputs: Any?,
    noinline callback: View.() -> Unit
) {
    onUnbindView<View>(inputs = *inputs, callback = callback)
}

inline fun <T : View> ComponentComposition.onUnbindView(
    vararg inputs: Any?,
    noinline callback: T.() -> Unit
) {
    onUnbindViewImpl(key = sourceLocation(), inputs = *inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentComposition.onUnbindViewImpl(
    key: Any,
    vararg inputs: Any?,
    callback: T.() -> Unit
) {
    key(key) {
        val component = currentComponent<T>()
        val removeCallbackHolder = memo { RemoveCallbackHolder(null) }
        memo(*inputs) {
            removeCallbackHolder.removeCallback?.invoke()
            removeCallbackHolder.removeCallback = component.onUnbindView(callback)
        }
        onDispose { removeCallbackHolder.removeCallback?.invoke() }
    }
}