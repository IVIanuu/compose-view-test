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
import androidx.compose.Composer
import androidx.compose.EffectsDsl
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.currentViewUpdater
import com.ivianuu.compose.internal.sourceLocation
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

fun <T : View> ComponentComposition.View(
    key: Any,
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    emit(key = key, block = block)
}

inline fun <reified T : View> ComponentComposition.View(
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View(key = sourceLocation(), type = T::class, block = block)
}

private val constructorsByClass = ConcurrentHashMap<KClass<*>, Constructor<*>>()

fun <T : View> ComponentComposition.View(
    key: Any,
    type: KClass<T>,
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View<T>(key = key) {
        viewType = type
        onCreateView { container ->
            constructorsByClass.getOrPut(type) { type.java.getConstructor(Context::class.java) }
                .newInstance(container.context) as T
        }

        block?.invoke(this)
    }
}

inline fun <T : View> ComponentComposition.ViewByLayoutRes(
    layoutRes: Int,
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ViewByLayoutRes(
        key = sourceLocation(),
        layoutRes = layoutRes,
        block = block
    )
}

fun <T : View> ComponentComposition.ViewByLayoutRes(
    key: Any,
    layoutRes: Int,
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View<T>(key = key) {
        viewType = layoutRes
        onCreateView { container ->
            LayoutInflater.from(container.context)
                .inflate(layoutRes, container, false) as T
        }
        block?.invoke(this)
    }
}

inline fun <T : View> ComponentComposition.ViewById(
    id: Int,
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ViewById(
        key = sourceLocation(),
        id = id,
        block = block
    )
}

fun <T : View> ComponentComposition.ViewById(
    key: Any,
    id: Int,
    block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ById(value = true) {
        View<T>(key = key) {
            viewType = id
            onCreateView { it.findViewById(id) }
            block?.invoke(this)
        }
    }
}

@EffectsDsl
class ComponentBuilder<T : View>(
    composer: Composer<Component<*>>,
    val component: Component<T>
) : ComponentComposition(composer) {
    var viewType: Any
        get() = component.viewType
        set(value) {
            component.viewType = value
        }
}

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