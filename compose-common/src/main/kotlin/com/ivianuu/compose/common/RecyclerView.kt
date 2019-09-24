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

package com.ivianuu.compose.common

import android.annotation.SuppressLint
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.*
import kotlinx.coroutines.flow.Flow

fun <T> ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    flow: Flow<Iterable<T>>,
    placeholder: ComponentComposition.() -> Unit = {},
    itemBuilder: ComponentComposition.(Int, T) -> Unit
) {
    val items = flow(flow)
    if (items != null) {
        RecyclerView(layoutManager = layoutManager, items = items, itemBuilder = itemBuilder)
    } else {
        placeholder()
    }
}

fun <T> ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    items: Array<T>,
    itemBuilder: ComponentComposition.(Int, T) -> Unit
) {
    RecyclerView(layoutManager = layoutManager) {
        items.forEachIndexed { index, item ->
            itemBuilder(index, item)
        }
    }
}

fun <T> ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    items: Iterable<T>,
    itemBuilder: ComponentComposition.(Int, T) -> Unit
) {
    RecyclerView(layoutManager = layoutManager) {
        items.forEachIndexed { index, item ->
            itemBuilder(index, item)
        }
    }
}

fun ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    itemCount: Int,
    itemBuilder: ComponentComposition.(Int) -> Unit
) {
    RecyclerView(layoutManager = layoutManager) {
        (0 until itemCount).forEach { index ->
            itemBuilder(index)
        }
    }
}

fun ComponentComposition.RecyclerView(
    layoutManager: RecyclerView.LayoutManager? = null,
    children: ComponentComposition.() -> Unit
) {
    View<RecyclerView> {
        val layoutManagerStateHolder = memo { LayoutManagerStateHolder() }
        set(layoutManager) {
            this.layoutManager = it ?: LinearLayoutManager(context)
            if (layoutManagerStateHolder.state != null) {
                this.layoutManager!!.onRestoreInstanceState(layoutManagerStateHolder.state)
                layoutManagerStateHolder.state = null
            }
        }

        init { adapter = ComposeRecyclerViewAdapter() }

        onUnbindView {
            layoutManagerStateHolder.state = it.layoutManager?.onSaveInstanceState()
            it.adapter = null
        } // calls unbindView on all children

        val component = currentComponent()
        onUpdateChildViews { view, _ ->
            (view.adapter as ComposeRecyclerViewAdapter).submitList(component.visibleChildren.toList())
        }
        onClearChildViews {
            (it.adapter as ComposeRecyclerViewAdapter).terminated = true
        }

        children()
    }
}

private data class LayoutManagerStateHolder(var state: Parcelable? = null)

class ComposeRecyclerViewAdapter :
    ListAdapter<Component<*>, ComposeRecyclerViewAdapter.Holder>(ITEM_CALLBACK) {

    var terminated = false

    private var lastItemViewTypeRequest: Component<*>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component =
            lastItemViewTypeRequest ?: currentList.first { it.getViewType() == viewType }
        val view = component.createView(parent)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = getItem(position).key.hashCode().toLong()

    override fun getItemViewType(position: Int): Int {
        val component = getItem(position)
        lastItemViewTypeRequest = component
        return component.getViewType()
    }

    private fun Component<*>.getViewType(): Int =
        (viewKey to children.map { it.getViewType() }).hashCode()

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view) {

        private var boundComponent: Component<*>? = null

        private var init = true

        fun bind(component: Component<*>) {
            unbind()
            boundComponent = component
            component as Component<View>
            component.bindView(view, init)
            init = false
        }

        fun unbind() {
            boundComponent?.let {
                it as Component<View>
                it.unbindView(view, terminated)
                boundComponent = null
            }
        }
    }

    private companion object {
        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<Component<*>>() {
            override fun areItemsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                oldItem.key == newItem.key

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                true
        }
    }
}