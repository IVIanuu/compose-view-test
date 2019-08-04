package com.ivianuu.compose.sample.common

import android.widget.FrameLayout
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroup
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.FrameLayout(
    noinline children: ViewComposition.() -> Unit
) {
    FrameLayout(sourceLocation(), children)
}

fun ViewComposition.FrameLayout(
    key: Any,
    children: ViewComposition.() -> Unit
) {
    ViewGroup<FrameLayout>(key, children = children)
}