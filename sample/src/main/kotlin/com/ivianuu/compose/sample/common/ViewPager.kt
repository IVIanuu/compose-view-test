package com.ivianuu.compose.sample.common

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ivianuu.compose.Component
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sourceLocation
import android.view.ViewGroup.LayoutParams as LayoutParams1

inline fun ViewComposition.ViewPager(
    noinline children: ViewComposition.() -> Unit
) {
    ViewPager(sourceLocation(), children)
}

fun ViewComposition.ViewPager(
    key: Any,
    children: ViewComposition.() -> Unit
) {
    emit(
        key = key,
        ctor = { ViewPagerComponent() },
        update = { children() }
    )
}

class ViewPagerComponent : Component<ViewPager2>() {

    override fun createView(container: ViewGroup): ViewPager2 =
        ViewPager2(container.context).apply {
            layoutParams = LayoutParams1(MATCH_PARENT, MATCH_PARENT)
            adapter = ComposePagerAdapter()
        }

    override fun bindView(view: ViewPager2) {
        super.bindView(view)
        (view.adapter as ComposePagerAdapter).submitList(children.toList())
    }

}

private class ComposePagerAdapter :
    ListAdapter<Component<*>, ComposePagerAdapter.Holder>(ITEM_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component = getItem(viewType)
        val view = component.performCreateView(parent)
        return Holder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        (getItem(position) as Component<View>).bindView(holder.itemView)
    }

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