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

package com.ivianuu.compose.sample.common

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.Component
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.component
import com.ivianuu.compose.createView
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.RecyclerView(
    noinline children: ViewComposition.() -> Unit
) {
    RecyclerView(sourceLocation(), children)
}

fun ViewComposition.RecyclerView(
    key: Any,
    children: ViewComposition.() -> Unit
) {
    View<RecyclerView>(key = key) {
        manageChildren = true

        createView()

        bindView {
            if (layoutManager == null) {
                layoutManager = LinearLayoutManager(context)
            }

            if (adapter == null) {
                adapter = ComposeRecyclerViewAdapter()
            }

            println("bind recycler ${component!!.children.map { it.key }}")

            (adapter as ComposeRecyclerViewAdapter).submitList(component!!.children.toList())
        }

        unbindView { adapter = null }

        children()
    }
}

private class ComposeRecyclerViewAdapter :
    ListAdapter<Component<*>, ComposeRecyclerViewAdapter.Holder>(ITEM_CALLBACK) {

    private var lastItemViewTypeRequest: Component<*>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component =
            lastItemViewTypeRequest ?: currentList.first { it.key.hashCode() == viewType }
        val view = component.createView(parent)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position) as Component<View>)
    }

    override fun onViewRecycled(holder: Holder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = getItem(position).key.hashCode().toLong()

    override fun getItemViewType(position: Int): Int {
        val component = getItem(position)
        lastItemViewTypeRequest = component
        return component.key.hashCode()
    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view) {

        private var boundComponent: Component<View>? = null

        fun bind(component: Component<View>) {
            if (boundComponent != null && boundComponent != component) {
                unbind()
            }

            boundComponent = component
            component.bindView(view)
        }

        fun unbind() {
            boundComponent?.unbindView(view)
        }
    }

    private companion object {
        val ITEM_CALLBACK = object : DiffUtil.ItemCallback<Component<*>>() {
            override fun areItemsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                oldItem.key == newItem.key

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Component<*>, newItem: Component<*>): Boolean =
                oldItem == newItem
        }
    }
}