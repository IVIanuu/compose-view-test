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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ivianuu.compose.Component
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.component
import com.ivianuu.compose.createView
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.ViewPager(
    selectedPage: Int,
    noinline onPageChanged: (Int) -> Unit,
    noinline children: ViewComposition.() -> Unit
) {
    ViewPager(sourceLocation(), selectedPage, onPageChanged, children)
}

fun ViewComposition.ViewPager(
    key: Any,
    selectedPage: Int,
    onPageChanged: (Int) -> Unit,
    children: ViewComposition.() -> Unit
) {
    View<ViewPager2>(key = key) {
        manageChildren = true

        createView()

        bindView {
            if (adapter == null) {
                adapter = ComposePagerAdapter()
            }
            (adapter as ComposePagerAdapter).submitList(component!!.children.toList())
            currentItem = selectedPage
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onPageChanged(position)
                }
            })
        }
        unbindView {
            adapter = null
        }

        children()
    }
}

private class ComposePagerAdapter :
    ListAdapter<Component<*>, ComposePagerAdapter.Holder>(ITEM_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component = currentList[viewType]
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

    override fun getItemViewType(position: Int): Int = position

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