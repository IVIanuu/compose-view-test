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
import com.ivianuu.compose.internal.sourceLocation

inline fun <T : View> ComponentBuilder<T>.onUpdateView(
    noinline callback: (T) -> Unit
) {
    callbackEffect(
        key = sourceLocation(),
        inputs = null,
        callback = callback
    ) { component, _callback ->
        component.onUpdateView(_callback)
    }
}

inline fun <T : View> ComponentBuilder<T>.onUpdateView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    callbackEffect(
        key = sourceLocation(),
        inputs = inputs,
        callback = callback
    ) { component, _callback ->
        component.onUpdateView(_callback)
    }
}

inline fun <T : View> ComponentBuilder<T>.onUpdateChildViews(
    noinline callback: (T) -> Unit
) {
    callbackEffect(
        key = sourceLocation(),
        inputs = null,
        callback = callback
    ) { component, _callback ->
        component.onUpdateChildViews(_callback)
    }
}

inline fun <T : View> ComponentBuilder<T>.onUpdateChildViews(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    callbackEffect(
        key = sourceLocation(),
        inputs = inputs,
        callback = callback
    ) { component, _callback ->
        component.onUpdateChildViews(_callback)
    }
}

@PublishedApi
internal fun <T : View, C> ComponentBuilder<T>.callbackEffect(
    key: Any,
    inputs: Array<out Any?>?,
    callback: C,
    addCallback: (Component<T>, C) -> () -> Unit
) {
    with(composition) {
        checkIsComposing()
        key(key) {
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