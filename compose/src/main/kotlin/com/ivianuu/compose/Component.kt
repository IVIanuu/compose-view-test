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
import com.ivianuu.compose.internal.ViewUpdater
import com.ivianuu.compose.internal.component
import com.ivianuu.compose.internal.ensureLayoutParams
import com.ivianuu.compose.internal.log
import com.ivianuu.compose.internal.tagKey
import kotlin.properties.Delegates

class Component<T : View> {

    var viewType: Any by Delegates.notNull()
        internal set

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    private var _parent: Component<*>? = null

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children
    val visibleChildren: List<Component<*>> get() = children.filterNot { it.hidden }

    val boundViews: Set<T> get() = _boundViews
    private val _boundViews = mutableSetOf<T>()

    private var createViewBlock: ((ViewGroup) -> T)? = null
    private var bindViewBlocks: MutableList<(T) -> Unit>? = null
    private var unbindViewBlocks: MutableList<(T) -> Unit>? = null
    private var layoutChildViewsBlock: ((T) -> Unit)? = null
    internal var viewUpdater: ViewUpdater<T>? = null

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
    internal var isPush = true
    internal var hidden = false
        internal set
    internal var byId = false
        internal set
    internal var generation = 0

    fun updateChildren(newChildren: List<Component<*>>) {
        if (_children == newChildren && _children.map { it.hidden } == newChildren.map { it.hidden }) return

        log { "update children $key new ${newChildren.map { it.key }} old ${_children.map { it.key }}" }

        _children
            .filter { it !in newChildren }
            .forEach { it._parent = null }

        newChildren
            .filter { it !in _children }
            .forEach { it._parent = this }

        _children.clear()
        _children += newChildren

        _boundViews.forEach { layoutChildViews(it) }
    }

    fun createView(container: ViewGroup): T {
        check(createViewBlock != null)
        log { "create view $key" }
        val view = createViewBlock!!(container)
        view.ensureLayoutParams(container)
        return view
    }

    fun bindView(view: T) {
        log { "bind view $key $view" }

        val newView = view.component != this

        _boundViews += view
        view.component = this

        bindViewBlocks?.forEach { it(view) }

        if (newView) {
            log { "updater: $key update new view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(
                ViewUpdater.Type.Init,
                ViewUpdater.Type.Update,
                ViewUpdater.Type.Value
            )
                ?.forEach { it(view) }
        } else if (view.generation != generation) {
            log { "updater: $key update view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(ViewUpdater.Type.Update, ViewUpdater.Type.Value)
                ?.forEach { it(view) }
        } else {
            log { "updater: $key skip update $generation" }
            viewUpdater?.getBlocks(ViewUpdater.Type.Update)
                ?.forEach { it(view) }
        }
    }

    fun unbindView(view: T) {
        log { "unbind view $key $view" }
        unbindViewBlocks?.forEach { it(view) }
        view.generation = null
        view.component = null
        _boundViews -= view
    }

    fun layoutChildViews(view: T) {
        log { "layout child views $key $view block ? $layoutChildViewsBlock" }

        if (layoutChildViewsBlock != null) {
            layoutChildViewsBlock!!.invoke(view)
        } else {
            if (view !is ViewGroup) return
            view.getViewManager().update(
                visibleChildren,
                visibleChildren.lastOrNull()?.isPush ?: true
            )
        }
    }

    fun bindChildViews(view: T) {
        if (view !is ViewGroup) return
        view.getViewManager().viewsByChild.forEach { (component, view) ->
            (component as Component<View>).bindView(view)
        }
    }

    @PublishedApi
    internal fun onCreateView(callback: (ViewGroup) -> T): () -> Unit {
        createViewBlock = callback
        return { createViewBlock = null }
    }

    @PublishedApi
    internal fun onBindView(callback: (T) -> Unit): () -> Unit {
        if (bindViewBlocks == null) bindViewBlocks = mutableListOf()
        bindViewBlocks!! += callback
        return { bindViewBlocks!! -= callback }
    }

    @PublishedApi
    internal fun onUnbindView(callback: (T) -> Unit): () -> Unit {
        if (unbindViewBlocks == null) unbindViewBlocks = mutableListOf()
        unbindViewBlocks!! += callback
        return { unbindViewBlocks!! -= callback }
    }

    @PublishedApi
    internal fun onLayoutChildViews(callback: (T) -> Unit): () -> Unit {
        layoutChildViewsBlock = callback
        return { layoutChildViewsBlock = null }
    }
}

private val generationKey = tagKey("generation")

var View.generation: Int?
    get() = getTag(generationKey) as? Int
    set(value) {
        setTag(generationKey, value)
    }