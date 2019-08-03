package com.ivianuu.compose.sample

import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ivianuu.compose.InflateViewGroup
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroupById

fun ViewComposition.Scaffold(
    key: Any,
    appBar: (ViewComposition.() -> Unit)? = null,
    content: (ViewComposition.() -> Unit)? = null
) {
    InflateViewGroup<LinearLayout>(
        key = key,
        layoutRes = R.layout.scaffold
    ) {
        ViewGroupById<FrameLayout>(R.id.app_bar) {
            appBar?.invoke(this@Scaffold)
        }

        ViewGroupById<FrameLayout>(R.id.content) {
            content?.invoke(this@Scaffold)
        }
    }
}