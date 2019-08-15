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
    val viewType: Any,
    val childViewController: ChildViewController<T>,
    val createView: (ViewGroup) -> T
) {

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    private var _parent: Component<*>? = null

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children
    val visibleChildren: List<Component<*>> get() = children.filterNot { it.hidden }

    private val _boundViews = mutableSetOf<T>()

    private var bindViewBlocks: MutableList<(T) -> Unit>? = null
    private var unbindViewBlocks: MutableList<(T) -> Unit>? = null
    internal var viewUpdater: ViewUpdater<T>? = null

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
    internal var isPush = true
    var hidden = false
        internal set
    internal var generation = 0

    fun update() {
        log { "update $key bound views ${_boundViews.size}" }
        _boundViews.forEach { bindView(it) }
    }

    fun updateChildren(newChildren: List<Component<*>>) {
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

        _boundViews.forEach { childViewController.updateChildViews(this, it) }
    }

    fun createView(container: ViewGroup): T {
        log { "create view $key" }
        val view = createView.invoke(container)
        view.ensureLayoutParams(container)
        childViewController.initChildViews(this, view)
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

        childViewController.updateChildViews(this, view)
    }

    fun unbindView(view: T) {
        childViewController.clearChildViews(this, view)
        log { "unbind view $key $view" }
        unbindViewBlocks?.forEach { it(view) }
        view.generation = null
        view.component = null
        _boundViews -= view
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

private val generationKey = tagKey("generation")

var View.generation: Int?
    get() = getTag(generationKey) as? Int
    set(value) {
        setTag(generationKey, value)
    }