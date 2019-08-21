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
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.currentViewUpdater
import com.ivianuu.compose.internal.sourceLocation
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

inline fun <T : View> ComponentComposition.View(
    viewKey: Any,
    noinline createView: (ViewGroup) -> T,
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    emit(key = sourceLocation(), viewKey = viewKey, createView = createView, block = block)
}

private val constructorsByClass = ConcurrentHashMap<KClass<*>, Constructor<*>>()

@PublishedApi
internal fun <T : View> createViewByType(type: KClass<T>): (ViewGroup) -> T = { container ->
    constructorsByClass.getOrPut(type) { type.java.getConstructor(Context::class.java) }
        .newInstance(container.context) as T
}

inline fun <reified T : View> ComponentComposition.View(
    viewKey: Any = T::class,
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View(
        viewKey = viewKey,
        createView = createViewByType(T::class),
        block = block
    )
}

@PublishedApi
internal fun <T : View> createViewByLayoutRes(layoutRes: Int): (ViewGroup) -> T = { container ->
    LayoutInflater.from(container.context)
        .inflate(layoutRes, container, false) as T
}

inline fun <T : View> ComponentComposition.ViewByLayoutRes(
    layoutRes: Int,
    viewKey: Any = layoutRes,
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    View(
        viewKey = viewKey,
        createView = createViewByLayoutRes(layoutRes),
        block = block
    )
}

inline fun <T : View> ComponentComposition.ViewById(
    id: Int,
    viewKey: Any = id,
    noinline block: (ComponentBuilder<T>.() -> Unit)? = null
) {
    ById(value = true) {
        View(
            viewKey = viewKey,
            createView = { it.findViewById(id) },
            block = block
        )
    }
}

//@EffectsDsl
class ComponentBuilder<T : View>(
    val composition: ComponentComposition,
    val component: Component<T>
)

fun <T : View, V> ComponentBuilder<T>.set(value: V, block: T.(V) -> Unit) {
    with(composition) {
        checkIsComposing()
        currentViewUpdater<T>().set(value) { block(it) }
    }
}

fun <T : View> ComponentBuilder<T>.setBy(vararg values: Any?, block: T.() -> Unit) {
    with(composition) {
        checkIsComposing()
        currentViewUpdater<T>().setBy(*values) { block() }
    }
}

fun <T : View> ComponentBuilder<T>.init(block: T.() -> Unit) {
    with(composition) {
        checkIsComposing()
        currentViewUpdater<T>().init(block)
    }
}

fun <T : View> ComponentBuilder<T>.update(block: T.() -> Unit) {
    with(composition) {
        checkIsComposing()
        currentViewUpdater<T>().update(block)
    }
}

inline fun <T : View> ComponentBuilder<T>.currentComponent() =
    composition.currentComponent<T>()