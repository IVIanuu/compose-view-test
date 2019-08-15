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
import androidx.compose.remember

@PublishedApi
internal class RemoveCallbackHolder(var removeCallback: (() -> Unit)? = null)

@JvmName("onBindViewUntyped")
inline fun ComponentComposition.onBindView(
    noinline callback: (View) -> Unit
) {
    onBindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

@JvmName("onBindViewUntyped")
inline fun ComponentComposition.onBindView(
    vararg inputs: Any?,
    noinline callback: (View) -> Unit
) {
    onBindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

inline fun <T : View> ComponentComposition.onBindView(
    noinline callback: (T) -> Unit
) {
    onBindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

inline fun <T : View> ComponentComposition.onBindView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    onBindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentComposition.onBindViewImpl(
    key: Any,
    inputs: Array<out Any?>?,
    callback: (T) -> Unit
) {
    key(key) {
        val component = currentComponent<T>()
        val removeCallbackHolder = memo { RemoveCallbackHolder(null) }
        if (inputs != null) {
            composer.remember(*inputs) {
                removeCallbackHolder.removeCallback?.invoke()
                removeCallbackHolder.removeCallback = component.onBindView(callback)
            }
        } else {
            composer.changed(callback)
            removeCallbackHolder.removeCallback?.invoke()
            removeCallbackHolder.removeCallback = component.onBindView(callback)
        }
        onDispose { removeCallbackHolder.removeCallback?.invoke() }
    }
}

@JvmName("onUnbindViewUntyped")
inline fun ComponentComposition.onUnbindView(
    noinline callback: (View) -> Unit
) {
    onUnbindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

@JvmName("onUnbindViewUntyped")
inline fun ComponentComposition.onUnbindView(
    vararg inputs: Any?,
    noinline callback: (View) -> Unit
) {
    onUnbindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

inline fun <T : View> ComponentComposition.onUnbindView(
    noinline callback: (T) -> Unit
) {
    onUnbindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

inline fun <T : View> ComponentComposition.onUnbindView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    onUnbindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentComposition.onUnbindViewImpl(
    key: Any,
    inputs: Array<out Any?>?,
    callback: (T) -> Unit
) {
    key(key) {
        val component = currentComponent<T>()
        val removeCallbackHolder = memo { RemoveCallbackHolder(null) }
        if (inputs != null) {
            composer.remember(*inputs) {
                removeCallbackHolder.removeCallback?.invoke()
                removeCallbackHolder.removeCallback = component.onUnbindView(callback)
            }
        } else {
            composer.changed(callback)
            removeCallbackHolder.removeCallback?.invoke()
            removeCallbackHolder.removeCallback = component.onUnbindView(callback)
        }
        onDispose { removeCallbackHolder.removeCallback?.invoke() }
    }
}