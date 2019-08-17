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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.Composer
import androidx.compose.EffectsDsl
import androidx.compose.remember
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.currentViewUpdater
import com.ivianuu.compose.internal.sourceLocation
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

fun <T : View> ComponentComposition.View(
    key: Any,
    viewType: Any,
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    createView: (ViewGroup) -> T,
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    emit(
        key = key,
        viewType = viewType,
        childViewController = childViewController,
        createView = createView,
        block = block
    )
}

inline fun <reified T : View> ComponentComposition.View(
    key: Any = sourceLocation(),
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View(
        key = key,
        type = T::class,
        childViewController = childViewController,
        block = block
    )
}

private val constructorsByClass = ConcurrentHashMap<KClass<*>, Constructor<*>>()

fun <T : View> ComponentComposition.View(
    key: Any,
    type: KClass<T>,
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View<T>(
        key = key,
        viewType = type,
        childViewController = childViewController,
        createView = { container ->
            constructorsByClass.getOrPut(type) { type.java.getConstructor(Context::class.java) }
                .newInstance(container.context) as T
        },
        block = block
    )
}

inline fun <T : View> ComponentComposition.ViewByLayoutRes(
    layoutRes: Int,
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ViewByLayoutRes(
        key = sourceLocation(),
        layoutRes = layoutRes,
        childViewController = childViewController,
        block = block
    )
}

fun <T : View> ComponentComposition.ViewByLayoutRes(
    key: Any,
    layoutRes: Int,
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View(
        key = key,
        viewType = layoutRes,
        childViewController = childViewController,
        createView = { container ->
            LayoutInflater.from(container.context)
                .inflate(layoutRes, container, false) as T
        },
        block = block
    )
}

inline fun <T : View> ComponentComposition.ViewById(
    id: Int,
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ViewById(
        key = sourceLocation(),
        id = id,
        childViewController = childViewController,
        block = block
    )
}

fun <T : View> ComponentComposition.ViewById(
    key: Any,
    id: Int,
    childViewController: ChildViewController<T> = DefaultChildViewController(),
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ById(value = true) {
        View(
            key = key,
            viewType = id,
            childViewController = childViewController,
            createView = { it.findViewById(id) },
            block = block
        )
    }
}

@EffectsDsl
class ComponentBuilder<T : View>(
    composer: Composer<Component<*>>,
    val component: Component<T>
) : ComponentComposition(composer)

inline fun <T : View> ComponentBuilder<T>.onBindView(
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onBindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

inline fun <T : View> ComponentBuilder<T>.onBindView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onBindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentBuilder<T>.onBindViewImpl(
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

inline fun <T : View> ComponentBuilder<T>.onUnbindView(
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onUnbindViewImpl(key = sourceLocation(), inputs = null, callback = callback)
}

inline fun <T : View> ComponentBuilder<T>.onUnbindView(
    vararg inputs: Any?,
    noinline callback: (T) -> Unit
) {
    checkIsComposing()
    onUnbindViewImpl(key = sourceLocation(), inputs = inputs, callback = callback)
}

@PublishedApi
internal fun <T : View> ComponentBuilder<T>.onUnbindViewImpl(
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

private class CallbackHolder(var callback: (() -> Unit)? = null)

fun <T : View, V> ComponentBuilder<T>.set(value: V, block: T.(V) -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().set(value) { block(it) }
}

fun <T : View> ComponentBuilder<T>.setBy(vararg values: Any?, block: T.() -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().setBy(*values) { block() }
}

fun <T : View> ComponentBuilder<T>.init(block: T.() -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().init(block)
}

fun <T : View> ComponentBuilder<T>.update(block: T.() -> Unit) {
    checkIsComposing()
    currentViewUpdater<T>().update(block)
}

inline fun <T : View> ComponentBuilder<T>.currentComponent() =
    (this as ComponentComposition).currentComponent<T>()