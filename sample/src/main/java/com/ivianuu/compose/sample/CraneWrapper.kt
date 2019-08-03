package com.ivianuu.compose.sample

import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroup
import com.ivianuu.compose.sourceLocation

inline fun ViewComposition.CraneWrapper(noinline children: ViewComposition.() -> Unit) {
    CraneWrapper(sourceLocation(), children)
}

fun ViewComposition.CraneWrapper(key: Any, children: ViewComposition.() -> Unit) {
    ViewGroup(
        key = key,
        ctor = {
            FrameLayout(it.context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            }
        },
        children = { children() }
    )
}