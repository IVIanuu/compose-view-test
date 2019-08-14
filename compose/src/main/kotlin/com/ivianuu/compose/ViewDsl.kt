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
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates
import kotlin.reflect.KClass

inline fun <reified T : View> ComponentComposition.View(
    noinline block: ViewDsl<T>.() -> Unit
) {
    View<T>(key = sourceLocation()) {
        createView()
        block()
    }
}

fun <T : View> ComponentComposition.View(
    key: Any,
    block: ViewDsl<T>.() -> Unit
) {
    emit<ViewDslComponent<T>>(
        key = key,
        ctor = { ViewDslComponent() },
        update = {
            val dsl = ViewDsl<T>().apply(block)
            viewType = dsl.viewType
            createView = dsl.createView
            bindViewBlocks = dsl.bindViewBlocks
            unbindViewBlocks = dsl.unbindViewBlocks
            manageChildren = dsl.manageChildren
        }
    )
}

class ViewDsl<T : View> {

    var viewType: Any by Delegates.notNull()

    var createView: (ViewGroup) -> T by Delegates.notNull()
    internal var bindViewBlocks: MutableList<T.() -> Unit>? = null
    internal var unbindViewBlocks: MutableList<T.() -> Unit>? = null

    var manageChildren = false

    inline fun <V> set(value: V, crossinline block: T.(V) -> Unit) {
        bindView { block(value) }
    }

    fun bindView(block: T.() -> Unit) {
        if (bindViewBlocks == null) bindViewBlocks = mutableListOf()
        bindViewBlocks!! += block
    }

    fun unbindView(block: T.() -> Unit) {
        if (unbindViewBlocks == null) unbindViewBlocks = mutableListOf()
        unbindViewBlocks!! += block
    }

}

inline fun <reified T : View> ViewDsl<T>.createView() {
    createView(T::class)
}

private val constructorsByClass = ConcurrentHashMap<KClass<*>, Constructor<*>>()

fun <T : View> ViewDsl<T>.createView(type: KClass<T>) {
    createView = { container ->
        constructorsByClass.getOrPut(type) { type.java.getConstructor(Context::class.java) }
            .newInstance(container.context) as T
    }
    viewType = type
}

fun <T : View> ViewDsl<T>.layoutRes(layoutRes: Int) {
    createView = { container ->
        LayoutInflater.from(container.context)
            .inflate(layoutRes, container, false) as T
    }
    viewType = layoutRes
}

fun <T : View> ViewDsl<T>.byId(id: Int) {
    createView = { container ->
        container.findViewById<T>(id)
            .also { it.byId = true }
    }
    viewType = id
}

private class ViewDslComponent<T : View> : Component<T>() {

    override lateinit var viewType: Any

    lateinit var createView: (ViewGroup) -> T
    var bindViewBlocks: List<T.() -> Unit>? = null
    var unbindViewBlocks: List<T.() -> Unit>? = null

    var manageChildren = false

    override fun onCreateView(container: ViewGroup): T =
        createView.invoke(container)

    override fun bindView(view: T) {
        super.bindView(view)
        bindViewBlocks?.forEach { it(view) }
    }

    override fun unbindView(view: T) {
        unbindViewBlocks?.forEach { it(view) }
        super.unbindView(view)
    }

    override fun initChildViews(view: T) {
        if (!manageChildren) super.initChildViews(view)
    }

    override fun updateChildViews(view: T) {
        if (!manageChildren) super.updateChildViews(view)
    }

    override fun clearChildViews(view: T) {
        if (!manageChildren) super.clearChildViews(view)
    }
}