package com.ivianuu.compose.sample.common

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.Component
import com.ivianuu.compose.ViewComposition
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
        update = { children() }
    )
}

class RecyclerViewComponent : Component<RecyclerView>() {

    override fun createView(container: ViewGroup): RecyclerView {
        return RecyclerView(container.context).apply {
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun bindView(view: RecyclerView) {
        super.bindView(view)
        if (view.adapter == null) {
            view.adapter = ComposeRecyclerViewAdapter()
        }
        (view.adapter as ComposeRecyclerViewAdapter).submitList(children.toList())
    }

    override fun unbindView(view: RecyclerView) {
        view.adapter = null
        super.unbindView(view)
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
        val view = component.performCreateView(parent)
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