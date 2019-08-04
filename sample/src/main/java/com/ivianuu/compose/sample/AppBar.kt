package com.ivianuu.compose.sample

import androidx.appcompat.widget.Toolbar
import com.ivianuu.compose.InflateViewGroup
import com.ivianuu.compose.ViewComposition

fun ViewComposition.AppBar(title: String) {
    InflateViewGroup<Toolbar>(
        layoutRes = R.layout.app_bar,
        updateView = { this.title = title }
    )
}