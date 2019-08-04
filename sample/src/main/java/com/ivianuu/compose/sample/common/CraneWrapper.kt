package com.ivianuu.compose.sample.common

import android.widget.FrameLayout
import com.ivianuu.compose.InflateViewGroup
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.R
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.CraneWrapper(noinline children: ViewComposition.() -> Unit) {
    CraneWrapper(sourceLocation(), children)
}

fun ViewComposition.CraneWrapper(key: Any, children: ViewComposition.() -> Unit) {
    InflateViewGroup<FrameLayout>(
        key = key,
        layoutRes = R.layout.crane,
        children = { children() }
    )
}