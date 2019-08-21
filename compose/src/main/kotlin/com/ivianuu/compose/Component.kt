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

    private var updateViewCallbacks: MutableList<(T) -> Unit>? = null
    private var destroyViewCallbacks: MutableList<(T) -> Unit>? = null

    private var updateChildViewCallback: ((T, Boolean) -> Unit)? = null

    internal var viewUpdater: ViewUpdater<T>? = null

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
    internal var isPush = true
    internal var hidden = false
        internal set
    internal var byId = false
        internal set
    internal var generation = 1

    private var viewGeneration: Int? = null

    fun update() {
        if (generation == viewGeneration) return
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

        updateChildViews(false)
    }

    fun createView(container: ViewGroup): T {
        var view = this.view
        log { "lifecycle: $key -> create view $container is creating new ? ${view == null}" }
        if (view == null) {
            view = createView.invoke(container)
            view.ensureLayoutParams(container)
            this.view = view
            updateView()
            if (_children.isNotEmpty()) updateChildViews(true)
        }
        return view
    }

    fun destroyView() {
        val view = this.view ?: return
        children.reversed().forEach { it.destroyView() }

        log { "lifecycle: $key -> destroy view $view" }

        destroyViewCallbacks?.forEach { it(view) }
        this.view = null
        viewGeneration = null
    }

    private fun updateView() {
        val view = this.view ?: return
        log { "lifecycle: $key -> update view $view" }

        updateViewCallbacks?.forEach { it(view) }

        val newView = viewGeneration == null

        if (newView) {
            log { "updater: $key -> update new view $viewGeneration to $generation" }
            viewGeneration = generation
            viewUpdater?.getBlocks(
                ViewUpdater.Type.Init,
                ViewUpdater.Type.Update,
                ViewUpdater.Type.Value
            )?.forEach { it(view) }
        } else if (viewGeneration != generation) {
            log { "updater: $key -> update view $viewGeneration to $generation" }
            viewGeneration = generation
            viewUpdater?.getBlocks(ViewUpdater.Type.Update, ViewUpdater.Type.Value)
                ?.forEach { it(view) }
        } else {
            log { "updater: $key -> skip update $generation" }
            viewUpdater?.getBlocks(ViewUpdater.Type.Update)
                ?.forEach { it(view) }
        }
    }

    private fun updateChildViews(init: Boolean) {
        val view = this.view ?: return

        log { "lifecycle: $key -> update child views $view is init ? $init block ? $updateChildViewCallback" }

        if (updateChildViewCallback != null) {
            updateChildViewCallback!!.invoke(view, init)
        } else {
            if (view !is ViewGroup) return
            with(view.getViewManager(this)) {
                if (init) {
                    rebind(visibleChildren)
                } else {
                    update(
                        visibleChildren,
                        visibleChildren.lastOrNull()?.isPush ?: true
                    )
                }
            }
        }
    }

    @PublishedApi
    internal fun onUpdateView(callback: (T) -> Unit): () -> Unit {
        if (updateViewCallbacks == null) updateViewCallbacks = mutableListOf()
        updateViewCallbacks!! += callback
        return { updateViewCallbacks!! -= callback }
    }

    @PublishedApi
    internal fun onDestroyView(callback: (T) -> Unit): () -> Unit {
        if (destroyViewCallbacks == null) destroyViewCallbacks = mutableListOf()
        destroyViewCallbacks!! += callback
        return { destroyViewCallbacks!! -= callback }
    }

    @PublishedApi
    internal fun onUpdateChildViews(callback: (T, Boolean) -> Unit): () -> Unit {
        updateChildViewCallback = callback
        return { updateChildViewCallback = null }
    }

}