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
import com.ivianuu.compose.internal.ensureLayoutParams
import com.ivianuu.compose.internal.log
import com.ivianuu.compose.internal.tagKey

class Component<T : View>(
    val key: Any,
    val viewKey: Any,
    val createView: (ViewGroup) -> T
) {

    var _parent: Component<*>? = null
        private set

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children

    private val _visibleChildren = mutableListOf<Component<*>>()
    val visibleChildren: List<Component<*>> get() = _visibleChildren

    private val boundViews = mutableSetOf<T>()

    private var bindViewCallbacks: MutableList<(T) -> Unit>? = null
    private var unbindViewCallbacks: MutableList<(T) -> Unit>? = null

    private var updateChildViewsCallback: ((T, Boolean) -> Unit)? = null
    private var clearChildViewsCallback: ((T) -> Unit)? = null

    internal var viewUpdater: ViewUpdater<T>? = null

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
    internal var isPush = true
    internal var hidden = false
        internal set
    internal var shareViews = true
        internal set
    internal var byId = false
        internal set
    internal var generation = 1

    fun update() {
        log { "lifecycle: $key -> update" }
        boundViews.forEach { bindView(it, false) }
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

        boundViews.forEach { updateChildViews(it, false) }
    }

    fun createView(container: ViewGroup): T {
        log { "lifecycle: $key -> create view $container" }
        val view = createView.invoke(container)
        view.ensureLayoutParams(container)
        return view
    }

    fun bindView(view: T, init: Boolean) {
        log { "lifecycle: $key -> bind view $view is new? ${view.generation == null}" }

        boundViews += view

        bindViewCallbacks?.forEach { it(view) }

        val newView = view.generation == null

        if (newView) {
            log { "updater: $key -> update new view ${view.generation} to $generation" }
            view.generation = generation
            viewUpdater?.getBlocks(
                ViewUpdater.Type.Init,
                ViewUpdater.Type.Update,
                ViewUpdater.Type.Value
            )?.forEach { it(view) }
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

        updateChildViews(view, init)
    }

    fun unbindView(view: T, clearChildViews: Boolean) {
        if (clearChildViews) {
            clearChildViews(view)
        }

        log { "lifecycle: $key -> unbind view $view" }
        unbindViewCallbacks?.forEach { it(view) }
        view.generation = null
        boundViews -= view
    }

    private fun updateChildViews(view: T, init: Boolean) {
        log { "lifecycle: $key -> update child views $view is init ? $init block ? $updateChildViewsCallback" }

        if (updateChildViewsCallback != null) {
            updateChildViewsCallback!!.invoke(view, init)
        } else {
            if (view !is ViewGroup) return
            with(view.getViewManager(this)) {
                update(
                    visibleChildren,
                    visibleChildren.lastOrNull()?.isPush ?: true,
                    !init
                )
            }
        }
    }

    private fun clearChildViews(view: T) {
        log { "lifecycle: $key -> clear child views $view ? $clearChildViewsCallback" }

        if (clearChildViewsCallback != null) {
            clearChildViewsCallback!!.invoke(view)
        } else {
            if (view !is ViewGroup) return
            view.getViewManager(this).update(
                newChildren = emptyList(),
                isPush = false,
                animate = false
            )
        }
    }

    @PublishedApi
    internal fun onBindView(callback: (T) -> Unit): () -> Unit {
        if (bindViewCallbacks == null) bindViewCallbacks = mutableListOf()
        bindViewCallbacks!! += callback
        return { bindViewCallbacks!! -= callback }
    }

    @PublishedApi
    internal fun onUnbindView(callback: (T) -> Unit): () -> Unit {
        if (unbindViewCallbacks == null) unbindViewCallbacks = mutableListOf()
        unbindViewCallbacks!! += callback
        return { }
    }

    @PublishedApi
    internal fun onUpdateChildViews(callback: (T, Boolean) -> Unit): () -> Unit {
        updateChildViewsCallback = callback
        return { updateChildViewsCallback = null }
    }

    @PublishedApi
    internal fun onClearChildViews(callback: (T) -> Unit): () -> Unit {
        clearChildViewsCallback = callback
        return { }
    }

}

private val viewGenerationKey = tagKey("viewGeneration")
private var View.generation: Int?
    get() = getTag(viewGenerationKey) as? Int
    set(value) {
        setTag(viewGenerationKey, value)
    }