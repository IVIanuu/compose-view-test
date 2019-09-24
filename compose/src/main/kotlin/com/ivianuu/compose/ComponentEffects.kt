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

fun <T : View> ComponentBuilder<T>.onBindView(callback: (T) -> Unit) {
    callbackEffect(
        inputs = null,
        callback = callback
    ) { component, _callback ->
        component.onBindView(_callback)
    }
}

fun <T : View> ComponentBuilder<T>.onBindView(
    vararg inputs: Any?,
    callback: (T) -> Unit
) {
    callbackEffect(
        inputs = inputs,
        callback = callback
    ) { component, _callback ->
        component.onBindView(_callback)
    }
}

fun <T : View> ComponentBuilder<T>.onUnbindView(callback: (T) -> Unit) {
    callbackEffect(
        inputs = null,
        callback = callback
    ) { component, _callback ->
        component.onUnbindView(_callback)
    }
}

fun <T : View> ComponentBuilder<T>.onUnbindView(
    vararg inputs: Any?,
    callback: (T) -> Unit
) {
    callbackEffect(
        inputs = inputs,
        callback = callback
    ) { component, _callback ->
        component.onUnbindView(_callback)
    }
}

fun <T : View> ComponentBuilder<T>.onUpdateChildViews(callback: (T, Boolean) -> Unit) {
    callbackEffect(
        inputs = null,
        callback = callback
    ) { component, _callback ->
        component.onUpdateChildViews(_callback)
    }
}

fun <T : View> ComponentBuilder<T>.onUpdateChildViews(
    vararg inputs: Any?,
    callback: (T, Boolean) -> Unit
) {
    callbackEffect(
        inputs = inputs,
        callback = callback
    ) { component, _callback ->
        component.onUpdateChildViews(_callback)
    }
}

fun <T : View> ComponentBuilder<T>.onClearChildViews(callback: (T) -> Unit) {
    callbackEffect(
        inputs = null,
        callback = callback
    ) { component, _callback ->
        component.onClearChildViews(callback)
    }
}

fun <T : View> ComponentBuilder<T>.onClearChildViews(
    vararg inputs: Any?,
    callback: (T) -> Unit
) {
    callbackEffect(
        inputs = inputs,
        callback = callback
    ) { component, _callback ->
        component.onClearChildViews(_callback)
    }
}

@PublishedApi
internal fun <T : View, C> ComponentBuilder<T>.callbackEffect(
    inputs: Array<out Any?>?,
    callback: C,
    addCallback: (Component<T>, C) -> () -> Unit
) {
    with(composition) {
        checkIsComposing()
        key(uniqueKey()) {
            val component = currentComponent<T>()
            val callbackHolder = memo { CallbackHolder(null) }
            if (inputs != null) {
                composer.remember(*inputs) {
                    callbackHolder.callback?.invoke()
                    callbackHolder.callback = addCallback(component, callback)
                }
            } else {
                composer.changed(callback)
                callbackHolder.callback?.invoke()
                callbackHolder.callback = addCallback(component, callback)
            }
            onDispose { callbackHolder.callback?.invoke() }
        }
    }
}

private class CallbackHolder(var callback: (() -> Unit)? = null)