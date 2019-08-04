package com.ivianuu.compose.sample

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.compose.Compose
import com.ivianuu.compose.ViewComposition

fun ViewComposition.RecyclerView() {

}

interface ListComposable {
    val key: Any
    fun ViewComposition.compose()

    fun _compose(viewComposition: ViewComposition) = with(viewComposition) {
        compose()
    }
}

class ComposeRecyclerViewAdapter :
    ListAdapter<ListComposable, ComposeRecyclerViewAdapter.ViewHolder>(ITEM_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT
            )
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int =
        getItem(position).key.hashCode()

    class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private var composable: ListComposable? = null

        val composer = Compose.composeInto(itemView as ViewGroup) {
            composable?._compose(this)
        }

        fun bind(composable: ListComposable) {
            this.composable = composable
            composer?.compose()
        }

    }

    private companion object {
        private val ITEM_CALLBACK = object : DiffUtil.ItemCallback<ListComposable>() {
            override fun areItemsTheSame(
                oldItem: ListComposable,
                newItem: ListComposable
            ): Boolean = oldItem.key == newItem.key

            override fun areContentsTheSame(
                oldItem: ListComposable,
                newItem: ListComposable
            ): Boolean = oldItem.key == newItem.key

        }
    }
}