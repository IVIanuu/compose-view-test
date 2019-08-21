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

class Component<T : View>(
    val key: Any,
    val createView: (ViewGroup) -> T
) {

    var _parent: Component<*>? = null
        private set

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children

    private val _visibleChildren = mutableListOf<Component<*>>()
    val visibleChildren: List<Component<*>> get() = _visibleChildren

    var view: T? = null
        private set

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

    fun update() {
        log { "lifecycle: $key -> update" }
        updateView()
    }

    fun updateChildren(newChildren: List<Component<*>>) {
        val newVisibleChildren = newChildren.filter { !it.hidden }

        if (_children == newChildren && _visibleChildren == newVisibleChildren) return

        log { "lifecycle: $key -> update children new ${newChildren.map { it.key }} old ${_children.map { it.key }}" }

        _children
            .filter { it !in newChildren }
            .forEach { it._parent = null }

        newChildren
            .filter { it !in _children }
            .forEach { it._parent = this }

        _children.clear()
        _children += newChildren

        _visibleChildren.clear()
        _visibleChildren += newVisibleChildren

        updateChildViews()
    }

    fun createView(container: ViewGroup): T {
        var view = this.view
        log { "lifecycle: $key -> create view $container is creating new ? ${view == null}" }
        if (view == null) {
            view = createView.invoke(container)
            view.ensureLayoutParams(container)
            this.view = view
            updateView()
            updateChildViews()
        }
        return view
    }

    fun destroyView() {
        val view = this.view ?: return
        log { "lifecycle: $key -> destroy view $view" }
        unbindViewBlocks?.forEach { it(view) }
        view.generation = null
        view.component = null
    }

    private fun updateView() {
        val view = this.view ?: return
        log { "lifecycle: $key -> bind view $view" }

        // todo remove
        // todo add a viewGeneration field or something
        val newView = view.component != this
        view.component = this

        bindViewBlocks?.forEach { it(view) }

        if (newView) {
            log { "updater: $key -> update new view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(
                ViewUpdater.Type.Init,
                ViewUpdater.Type.Update,
                ViewUpdater.Type.Value
            )
                ?.forEach { it(view) }
        } else if (view.generation != generation) {
            log { "updater: $key -> update view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(ViewUpdater.Type.Update, ViewUpdater.Type.Value)
                ?.forEach { it(view) }
        } else {
            log { "updater: $key -> skip update $generation" }
            viewUpdater?.getBlocks(ViewUpdater.Type.Update)
                ?.forEach { it(view) }
        }
    }

    private fun updateChildViews() {
        val view = this.view ?: return

        log { "lifecycle: $key -> layout child views $view block ? $layoutChildViewsBlock" }

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

    @PublishedApi
    internal fun onUpdateView(callback: (T) -> Unit): () -> Unit {
        if (bindViewBlocks == null) bindViewBlocks = mutableListOf()
        bindViewBlocks!! += callback
        return { bindViewBlocks!! -= callback }
    }

    @PublishedApi
    internal fun onUpdateChildViews(callback: (T) -> Unit): () -> Unit {
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