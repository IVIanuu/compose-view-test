package com.ivianuu.compose.sample.common

import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.byId
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.R
import com.ivianuu.compose.sourceLocation

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
    View<LinearLayout>(key = key) {
        layoutRes(R.layout.scaffold)
        View<FrameLayout> {
            byId(R.id.app_bar)
            appBar?.invoke(this@Scaffold)
        }
        View<FrameLayout> {
            byId(R.id.content)
            content?.invoke(this@Scaffold)
        }
    }
}