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
import com.ivianuu.compose.internal.byId
import com.ivianuu.compose.internal.sourceLocation
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

fun <T : View> ComponentComposition.View(
    key: Any,
    viewType: Any,
    manageChildren: Boolean = true,
    createView: (ViewGroup) -> T,
    block: ComponentContext<T>.() -> Unit
) {
    emit(
        key = key,
        viewType = viewType,
        manageChildren = manageChildren,
        createView = createView,
        block = block
    )
}

inline fun <reified T : View> ComponentComposition.View(
    key: Any = sourceLocation(),
    manageChildren: Boolean = true,
    noinline block: ComponentContext<T>.() -> Unit
) {
    View(
        key = key,
        type = T::class,
        manageChildren = manageChildren,
        block = block
    )
}

private val constructorsByClass = ConcurrentHashMap<KClass<*>, Constructor<*>>()

fun <T : View> ComponentComposition.View(
    key: Any,
    type: KClass<T>,
    manageChildren: Boolean = true,
    block: ComponentContext<T>.() -> Unit
) {
    View<T>(
        key = key,
        viewType = type,
        manageChildren = manageChildren,
        createView = { container ->
            constructorsByClass.getOrPut(type) { type.java.getConstructor(Context::class.java) }
                .newInstance(container.context) as T
        },
        block = block
    )
}

inline fun <T : View> ComponentComposition.ViewByLayoutRes(
    layoutRes: Int,
    manageChildren: Boolean = true,
    noinline block: ComponentContext<T>.() -> Unit
) {
    ViewByLayoutRes(
        key = sourceLocation(),
        layoutRes = layoutRes,
        manageChildren = manageChildren,
        block = block
    )
}

fun <T : View> ComponentComposition.ViewByLayoutRes(
    key: Any,
    layoutRes: Int,
    manageChildren: Boolean = true,
    block: ComponentContext<T>.() -> Unit
) {
    View(
        key = key,
        viewType = layoutRes,
        manageChildren = manageChildren,
        createView = { container ->
            LayoutInflater.from(container.context)
                .inflate(layoutRes, container, false) as T
        },
        block = block
    )
}

inline fun <T : View> ComponentComposition.ViewById(
    id: Int,
    manageChildren: Boolean = true,
    noinline block: ComponentContext<T>.() -> Unit
) {
    ViewById(
        key = sourceLocation(),
        id = id,
        manageChildren = manageChildren,
        block = block
    )
}

fun <T : View> ComponentComposition.ViewById(
    key: Any,
    id: Int,
    manageChildren: Boolean = true,
    block: ComponentContext<T>.() -> Unit
) {
    View(
        key = key,
        viewType = id,
        manageChildren = manageChildren,
        createView = { container ->
            container.findViewById<T>(id)
                .also { it.byId = true }
        },
        block = block
    )
}