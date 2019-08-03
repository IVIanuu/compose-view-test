package com.ivianuu.compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ViewUpdater

fun <T : View> ViewComposition.InflateView(
    key: Any,
    layoutRes: Int,
    update: (ViewUpdater<T>.() -> Unit)? = null
)  {
    View(
        key = key,
        ctor = {
            LayoutInflater.from(it.context)
                .inflate(layoutRes, it, false) as T
        },
        update = update
    )
}

fun <T : ViewGroup> ViewComposition.InflateViewGroup(
    key: Any,
    layoutRes: Int,
    update: (ViewUpdater<T>.() -> Unit)? = null,
    children: (ViewComposition.() -> Unit)? = null
)  {
    ViewGroup(
        key = key,
        ctor = {
            LayoutInflater.from(it.context)
                .inflate(layoutRes, it, false) as T
        },
        update = update,
        children = children
    )
}

fun <T : ViewGroup> ViewComposition.ViewGroup(
    key: Any,
    ctor: (ViewGroup) -> T,
    update: (ViewUpdater<T>.() -> Unit)? = null,
    children: (ViewComposition.() -> Unit)? = null
) {
    emit(
        key = key,
        ctor = ctor,
        update = { update?.invoke(this) },
        children = { children?.invoke(this) }
    )
}

fun <T : View> ViewComposition.View(
    key: Any,
    ctor: (ViewGroup) -> T,
    update: (ViewUpdater<T>.() -> Unit)? = null
) {
    emit(
        key = key,
        ctor = ctor,
        update = { update?.invoke(this) }
    )
}