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
import android.view.ViewGroup
import androidx.compose.Ambient

abstract class Component<T : View> {

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    abstract val viewType: Any

    private var _parent: Component<*>? = null
    val parent: Component<*> get() = _parent ?: error("Not mounted ${javaClass.canonicalName}")

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children
    val visibleChildren: List<Component<*>> get() = children.filterNot { it.hidden }

    val boundViews: Set<T> get() = _boundViews
    private val _boundViews = mutableSetOf<T>()

    private var bindViewBlocks: MutableList<(T) -> Unit>? = null
    private var unbindViewBlocks: MutableList<(T) -> Unit>? = null

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
    internal var wasPush = true
    internal var hidden = false

    open fun update() {
        log { "update $key bound views ${_boundViews.size}" }
        _boundViews.forEach { bindView(it) }
    }

    open fun updateChildren(newChildren: List<Component<*>>) {
        if (_children == newChildren) return

        log { "update children $key new ${newChildren.map { it.key }} old ${_children.map { it.key }}" }

        _children
            .filter { it !in newChildren }
            .forEach { it._parent = null }

        newChildren
            .filter { it !in _children }
            .forEach { it._parent = this }

        _children.clear()
        _children += newChildren

        _boundViews.forEach { updateChildViews(it) }
    }

    open fun createView(container: ViewGroup): T {
        log { "create view $key" }
        val view = onCreateView(container)
        view.ensureLayoutParams(container)
        initChildViews(view)
        return view
    }

    protected abstract fun onCreateView(container: ViewGroup): T

    open fun bindView(view: T) {
        log { "bind view $key $view" }
        _boundViews += view
        view.component = this
        bindViewBlocks?.forEach { it(view) }
        updateChildViews(view)
    }

    open fun unbindView(view: T) {
        clearChildViews(view)
        log { "unbind view $key $view" }
        unbindViewBlocks?.forEach { it(view) }
        _boundViews -= view
        view.component = null
    }

    protected open fun initChildViews(view: T) {
        val visibleChildren = visibleChildren
        log { "init child views $key ${view.javaClass} visible children ${visibleChildren.map { it.key }} all children ${children.map { it.key }}" }
        if (view !is ViewGroup) return
        view.getViewManager().init(visibleChildren)
    }

    protected open fun updateChildViews(view: T) {
        val visibleChildren = visibleChildren
        log { "update child views $key ${view.javaClass} visible children ${visibleChildren.map { it.key }} all children ${children.map { it.key }}" }
        if (view !is ViewGroup) return
        view.getViewManager().update(visibleChildren, visibleChildren.lastOrNull()?.wasPush ?: true)
    }

    protected open fun clearChildViews(view: T) {
        log { "clear child views $key ${view.javaClass} visible children ${visibleChildren.map { it.key }} all children ${children.map { it.key }}" }
        if (view !is ViewGroup) return
        view.getViewManager().clear()
    }

    internal fun onBindView(callback: (T) -> Unit): () -> Unit {
        if (bindViewBlocks == null) bindViewBlocks = mutableListOf()
        bindViewBlocks!! += callback
        return { bindViewBlocks!! -= callback }
    }

    internal fun onUnbindView(callback: (T) -> Unit): () -> Unit {
        if (unbindViewBlocks == null) unbindViewBlocks = mutableListOf()
        unbindViewBlocks!! += callback
        return { unbindViewBlocks!! -= callback }
    }

}

internal val ComponentAmbient = Ambient.of<Component<*>>()

fun <T : View> ComponentComposition.currentComponent(): Component<T> =
    ambient(ComponentAmbient) as Component<T>