package com.ivianuu.compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ViewUpdater
import com.ivianuu.compose.util.sourceLocation

inline fun <T : View> ViewComposition.ViewById(
    id: Int,
    noinline update: (ViewUpdater<T>.() -> Unit)? = null
) {
    ViewById(sourceLocation(), id, update)
}

fun <T : View> ViewComposition.ViewById(
    key: Any,
    id: Int,
    update: (ViewUpdater<T>.() -> Unit)? = null
) {
    View(
        key = key,
        ctor = { it.findViewById(id) },
        update = update
    )
}

inline fun <T : ViewGroup> ViewComposition.ViewGroupById(
    id: Int,
    noinline update: (ViewUpdater<T>.() -> Unit)? = null,
    noinline children: (ViewComposition.() -> Unit)? = null
) {
    ViewGroupById(sourceLocation(), id, update, children)
}

fun <T : ViewGroup> ViewComposition.ViewGroupById(
    key: Any,
    id: Int,
    update: (ViewUpdater<T>.() -> Unit)? = null,
    children: (ViewComposition.() -> Unit)? = null
) {
    ViewGroup(
        key = key,
        ctor = { it.findViewById(id) },
        update = update,
        children = children
    )
}

inline fun <T : View> ViewComposition.InflateView(
    layoutRes: Int,
    noinline update: (ViewUpdater<T>.() -> Unit)? = null
) {
    InflateView(sourceLocation(), layoutRes, update)
}

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

inline fun <T : ViewGroup> ViewComposition.InflateViewGroup(
    layoutRes: Int,
    noinline update: (ViewUpdater<T>.() -> Unit)? = null,
    noinline children: (ViewComposition.() -> Unit)? = null
) {
    InflateViewGroup(sourceLocation(), layoutRes, update, children)
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

inline fun <T : ViewGroup> ViewComposition.ViewGroup(
    noinline ctor: (ViewGroup) -> T,
    noinline update: (ViewUpdater<T>.() -> Unit)? = null,
    noinline children: (ViewComposition.() -> Unit)? = null
) {
    ViewGroup(sourceLocation(), ctor, update, children)
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
        update = update,
        children = children
    )
}

inline fun <T : View> ViewComposition.View(
    noinline ctor: (ViewGroup) -> T,
    noinline update: (ViewUpdater<T>.() -> Unit)? = null
) {
    View(sourceLocation(), ctor, update)
}

fun <T : View> ViewComposition.View(
    key: Any,
    ctor: (ViewGroup) -> T,
    update: (ViewUpdater<T>.() -> Unit)? = null
) {
    emit(
        key = key,
        ctor = ctor,
        update = update
    )
}