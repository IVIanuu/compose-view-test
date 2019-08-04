package com.ivianuu.compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.util.sourceLocation

inline fun <T : View> ViewComposition.ViewById(
    id: Int,
    noinline updateView: (T.() -> Unit)? = null
) {
    ViewById(sourceLocation(), id, updateView)
}

fun <T : View> ViewComposition.ViewById(
    key: Any,
    id: Int,
    updateView: (T.() -> Unit)? = null
) {
    View(
        key = key,
        createView = { it.findViewById(id) },
        updateView = updateView
    )
}

inline fun <T : ViewGroup> ViewComposition.ViewGroupById(
    id: Int,
    noinline updateView: (T.() -> Unit)? = null,
    noinline children: (ViewComposition.() -> Unit)? = null
) {
    ViewGroupById(sourceLocation(), id, updateView, children)
}

fun <T : ViewGroup> ViewComposition.ViewGroupById(
    key: Any,
    id: Int,
    updateView: (T.() -> Unit)? = null,
    children: (ViewComposition.() -> Unit)? = null
) {
    ViewGroup(
        key = key,
        createView = { it.findViewById(id) },
        updateView = updateView,
        children = children
    )
}

inline fun <T : View> ViewComposition.InflateView(
    layoutRes: Int,
    noinline updateView: (T.() -> Unit)? = null
) {
    InflateView(sourceLocation(), layoutRes, updateView)
}

fun <T : View> ViewComposition.InflateView(
    key: Any,
    layoutRes: Int,
    updateView: (T.() -> Unit)? = null
)  {
    View(
        key = key,
        createView = {
            LayoutInflater.from(it.context)
                .inflate(layoutRes, it, false) as T
        },
        updateView = updateView
    )
}

inline fun <T : ViewGroup> ViewComposition.InflateViewGroup(
    layoutRes: Int,
    noinline updateView: (T.() -> Unit)? = null,
    noinline children: (ViewComposition.() -> Unit)? = null
) {
    InflateViewGroup(sourceLocation(), layoutRes, updateView, children)
}

fun <T : ViewGroup> ViewComposition.InflateViewGroup(
    key: Any,
    layoutRes: Int,
    updateView: (T.() -> Unit)? = null,
    children: (ViewComposition.() -> Unit)? = null
)  {
    ViewGroup(
        key = key,
        createView = {
            LayoutInflater.from(it.context)
                .inflate(layoutRes, it, false) as T
        },
        updateView = updateView,
        children = children
    )
}

inline fun <T : ViewGroup> ViewComposition.ViewGroup(
    noinline createView: (ViewGroup) -> T,
    noinline updateView: (T.() -> Unit)? = null,
    noinline children: (ViewComposition.() -> Unit)?
) {
    ViewGroup(sourceLocation(), createView, updateView, children)
}

fun <T : ViewGroup> ViewComposition.ViewGroup(
    key: Any,
    createView: (ViewGroup) -> T,
    updateView: (T.() -> Unit)? = null,
    children: (ViewComposition.() -> Unit)? = null
) {
    emit(
        key = key,
        ctor = { SimpleGroupComponent<T>() },
        update = {
            this.createView = createView
            this.updateView = updateView
        },
        children = children
    )
}

inline fun <T : View> ViewComposition.View(
    noinline createView: (ViewGroup) -> T,
    noinline updateView: (T.() -> Unit)? = null
) {
    View(sourceLocation(), createView, updateView)
}

fun <T : View> ViewComposition.View(
    key: Any,
    createView: (ViewGroup) -> T,
    updateView: (T.() -> Unit)? = null
) {
    emit(
        key = key,
        ctor = { SimpleComponent<T>() },
        update = {
            this.createView = createView
            this.updateView = updateView
        }
    )
}

private class SimpleComponent<T : View> : Component<T>() {

    lateinit var createView: (ViewGroup) -> T
    var updateView: (T.() -> Unit)? = null

    override fun createView(container: ViewGroup): T = createView.invoke(container)

    override fun updateView(view: T) {
        updateView?.invoke(view)
    }
}

private class SimpleGroupComponent<T : ViewGroup> : GroupComponent<T>() {

    lateinit var createView: (ViewGroup) -> T
    var updateView: (T.() -> Unit)? = null

    override fun createView(container: ViewGroup): T = createView.invoke(container)

    override fun updateView(view: T) {
        updateView?.invoke(view)
    }
}