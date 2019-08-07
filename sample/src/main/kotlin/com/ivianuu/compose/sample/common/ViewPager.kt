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
    emit(
        key = key,
        ctor = { ViewPagerComponent() },
        update = {
            this.selectedPage = selectedPage
            this.onPageChanged = onPageChanged
            children()
        }
    )
}

class ViewPagerComponent : Component<ViewPager2>() {

    var selectedPage = 0
    lateinit var onPageChanged: (Int) -> Unit

    override fun createView(container: ViewGroup): ViewPager2 =
        ViewPager2(container.context).apply {
            layoutParams = LayoutParams1(MATCH_PARENT, MATCH_PARENT)
        }

    override fun bindView(view: ViewPager2) {
        super.bindView(view)
        if (view.adapter == null) {
            view.adapter = ComposePagerAdapter()
        }
        (view.adapter as ComposePagerAdapter).submitList(children.toList())
        view.currentItem = selectedPage
        view.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onPageChanged(position)
            }
        })
    }

    override fun unbindView(view: ViewPager2) {
        view.adapter = null
        super.unbindView(view)
    }

}

private class ComposePagerAdapter :
    ListAdapter<Component<*>, ComposePagerAdapter.Holder>(ITEM_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val component = currentList[viewType]
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