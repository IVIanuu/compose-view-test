package com.ivianuu.compose.sample

import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ivianuu.compose.InflateViewGroup
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroupById
import com.ivianuu.compose.util.sourceLocation

inline fun ViewComposition.Scaffold(
    noinline appBar: (ViewComposition.() -> Unit)? = null,
    noinline content: (ViewComposition.() -> Unit)? = null
) {
    Scaffold(sourceLocation(), appBar, content)
}

fun ViewComposition.Scaffold(
    key: Any,
    appBar: (ViewComposition.() -> Unit)? = null,
    content: (ViewComposition.() -> Unit)? = null
) {
    InflateViewGroup<LinearLayout>(
        key = key,
        layoutRes = R.layout.scaffold
    ) {
        ViewGroupById<FrameLayout>("app bar", R.id.app_bar) {
            appBar?.invoke(this@Scaffold)
        }

        ViewGroupById<FrameLayout>("content", R.id.content) {
            content?.invoke(this@Scaffold)
        }
    }
}