package com.ivianuu.compose.sample

import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.state
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroup
import com.ivianuu.compose.sample.common.RecyclerView
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.navigator

fun ViewComposition.List() = Route {
    Transitions(com.ivianuu.compose.common.VerticalViewTransition()) {
        val itemsState = +state { (1 until 100).map { "Title: $it" } }

        ViewGroup(
            createView = {
                FrameLayout(it.context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT
                    )
                    setBackgroundColor(Color.WHITE)
                }
            },
            children = {
                RecyclerView {
                    val navigator = navigator()

                    ListItem(text = "Go back", onClick = {
                        navigator.pop()
                    })

                    itemsState.value.forEach { item ->
                        ListItem(text = item, onClick = {
                            val newItems = itemsState.value.toMutableList()
                                .apply { remove(item) }
                            itemsState.value = newItems
                        })
                    }
                }
            }
        )
    }
}

private fun ViewComposition.ListItem(
    text: String,
    onClick: () -> Unit
) {
    InflateView<TextView>(text, R.layout.list_item) {
        this.text = text
        setOnClickListener { onClick() }
    }
}