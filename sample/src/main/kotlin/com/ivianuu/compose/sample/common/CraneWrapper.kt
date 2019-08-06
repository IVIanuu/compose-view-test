package com.ivianuu.compose.sample.common

import android.widget.FrameLayout
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.createView
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.CraneWrapper(noinline children: ViewComposition.() -> Unit) {
    CraneWrapper(sourceLocation(), children)
}

fun ViewComposition.CraneWrapper(key: Any, children: ViewComposition.() -> Unit) {
    View<FrameLayout>(key) {
        createView()
        children()
    }
}