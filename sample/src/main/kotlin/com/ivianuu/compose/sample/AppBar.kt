package com.ivianuu.compose.sample

import com.google.android.material.appbar.MaterialToolbar
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.navigator

fun ViewComposition.AppBar(title: String) {
    val navigator = navigator()
    View<MaterialToolbar> {
        layoutRes(R.layout.app_bar)
        bindView {
            this.title = title
            if (navigator.backStack.size > 1) {
                this.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
                setNavigationOnClickListener { navigator.pop() }
            } else {
                navigationIcon = null
            }
        }
    }
}