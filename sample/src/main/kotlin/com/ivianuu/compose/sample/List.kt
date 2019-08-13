package com.ivianuu.compose.sample

import android.widget.TextView
import androidx.compose.memo
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.VerticalChangeHandler
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.RecyclerView
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold

fun ViewComposition.List() = Route {
    val handler = +memo { VerticalChangeHandler() }
    ChangeHandlers(handler = handler) {
        Scaffold(
            appBar = { AppBar("List") },
            content = {
                RecyclerView {
                    (0..100).forEach {
                        ListItem("Title $it")
                    }
                }
            }
        )
    }
}

private fun ViewComposition.ListItem(text: String) {
    View<TextView>(key = text) {
        layoutRes(R.layout.list_item)
        updateView { this.text = text }
    }
}