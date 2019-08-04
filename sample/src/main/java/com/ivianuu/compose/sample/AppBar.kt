package com.ivianuu.compose.sample

import androidx.appcompat.widget.Toolbar
import com.ivianuu.compose.InflateViewGroup
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.common.navigator

fun ViewComposition.AppBar(title: String) {
    val navigator = navigator()
    InflateViewGroup<Toolbar>(
        layoutRes = R.layout.app_bar,
        updateView = {
            this.title = title
            if (navigator.backStack.size > 1) {
                this.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
                setNavigationOnClickListener { navigator.pop() }
            } else {
                navigationIcon = null
            }
        }
    )
}