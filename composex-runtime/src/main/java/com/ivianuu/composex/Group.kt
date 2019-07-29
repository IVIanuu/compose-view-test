package com.ivianuu.composex

import androidx.compose.ViewComposition

inline fun ViewComposition.group(key: Any = sourceLocation(), block: ViewComposition.() -> Unit) {
    with(composer) {
        startGroup(key)
        block()
        endGroup()
    }
}