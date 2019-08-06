package com.ivianuu.compose.sample.common

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.Component
import com.ivianuu.compose.GroupComponent
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.component
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
    emit(
        key = key,
        ctor = { RecyclerViewComponent() },
        children = children
    )
}

class RecyclerViewComponent : GroupComponent<RecyclerView>() {

    override fun createView(container: ViewGroup): RecyclerView {
        return RecyclerView(container.context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ComposeRecyclerViewAdapter()
        }
    }

    override fun updateView(view: RecyclerView) {
        super.updateView(view)
        (view.adapter as ComposeRecyclerViewAdapter).submitList(children.toList())
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
        view.component = component
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        (getItem(position) as Component<View>).updateView(holder.itemView)
    }

    override fun getItemViewType(position: Int): Int {
        val component = getItem(position)
        lastItemViewTypeRequest = component
        return component.key.hashCode()
    }

    override fun getItemId(position: Int): Long = getItem(position).key.hashCode().toLong()

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)

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