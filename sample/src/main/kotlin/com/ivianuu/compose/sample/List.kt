package com.ivianuu.compose.sample

import android.widget.TextView
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.RecyclerView
import com.ivianuu.compose.sample.common.Route

fun ViewComposition.List() = Route {
    RecyclerView {
        (0..100).forEach { ListItem("Title $it") }
    }
}

private fun ViewComposition.ListItem(text: String) {
    View<TextView>(key = text) {
        layoutRes(R.layout.list_item)
        updateView { this.text = text }
    }
}