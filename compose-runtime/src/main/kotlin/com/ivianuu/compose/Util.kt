package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

inline fun sourceLocation(): String {
    val element = Throwable().stackTrace.first()
    return "${element.className}:${element.methodName}:${element.lineNumber}"
}

fun ViewGroup.children(): List<View> = (0 until childCount)
    .map { getChildAt(it) }

internal fun tagKey(key: String): Int = (3 shl 24) or key.hashCode()

private val componentKey = tagKey("component")

internal var View.component: Component<*>?
    get() = getTag(componentKey) as? Component<*>
    set(value) {
        setTag(componentKey, value)
    }