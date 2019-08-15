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
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.currentViewUpdater
import com.ivianuu.compose.internal.sourceLocation

inline fun <T : View> ComponentContext<T>.onBindView(
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onBindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

inline fun <T : View> ComponentContext<T>.onBindView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onBindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentContext<T>.onBindViewImpl(
    key: Any,
    inputs: Array<out Any?>?,
    callback: (T) -> Unit
) {
    checkIsComposing()
    key(key) {
        val component = currentComponent<T>()
        val callbackHolder = memo { CallbackHolder(null) }
        if (inputs != null) {
            composer.remember(*inputs) {
                callbackHolder.callback?.invoke()
                callbackHolder.callback = component.onBindView(callback)
            }
        } else {
            composer.changed(callback)
            callbackHolder.callback?.invoke()
            callbackHolder.callback = component.onBindView(callback)
        }
        onDispose { callbackHolder.callback?.invoke() }
    }
}

inline fun <T : View> ComponentContext<T>.onUnbindView(
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onUnbindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

inline fun <T : View> ComponentContext<T>.onUnbindView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onUnbindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentContext<T>.onUnbindViewImpl(
    key: Any,
    inputs: Array<out Any?>?,
    callback: (T) -> Unit
) {
    checkIsComposing()
    key(key) {
        val component = currentComponent<T>()
        val callbackHolder = memo { CallbackHolder(null) }
        if (inputs != null) {
            composer.remember(*inputs) {
                callbackHolder.callback?.invoke()
                callbackHolder.callback = component.onUnbindView(callback)
            }
        } else {
            composer.changed(callback)
            callbackHolder.callback?.invoke()
            callbackHolder.callback = component.onUnbindView(callback)
        }
        onDispose { callbackHolder.callback?.invoke() }
    }
}

fun <T : View, V> ComponentContext<T>.set(value: V, block: T.(V) -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().set(value) { block(it) }
}

fun <T : View> ComponentContext<T>.setBy(vararg values: Any?, block: T.() -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().setBy(*values) { block() }
}

fun <T : View> ComponentContext<T>.init(block: T.() -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().init(block)
}

fun <T : View> ComponentContext<T>.update(block: T.() -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().update(block)
}

private class CallbackHolder(var callback: (() -> Unit)? = null)