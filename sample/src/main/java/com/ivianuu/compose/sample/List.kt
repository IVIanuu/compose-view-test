package com.ivianuu.compose.sample

import android.widget.TextView
import androidx.compose.state
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.common.RecyclerView
import com.ivianuu.compose.sample.common.Route

fun ViewComposition.List() = Route {
    println("ttt build list")
    val (items, setItems) = +state {
        println("ttt init state")
        (1 until 100).map { "Title: $it" }
    }

    RecyclerView {
        println("ttt build recycler view size ${items.size}")
        items.forEach { item ->
            ListItem(text = item, onClick = {
                val newItems = items.toMutableList()
                    .apply { remove(item) }
                println("ttt set new items")
                setItems(newItems)
            })
        }
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