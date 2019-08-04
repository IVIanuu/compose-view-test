package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

inline fun sourceLocation(): String {
    val element = Throwable().stackTrace.first()
    return "${element.className}:${element.methodName}:${element.lineNumber}"
}

internal fun ViewGroup.children(): List<View> = (0 until childCount)
    .map { getChildAt(it) }

internal fun tagKey(key: String): Int = (3 shl 24) or key.hashCode()

private val componentKey = tagKey("component")

internal var View.component: Component<*>?
    get() = getTag(componentKey) as? Component<*>
    set(value) {
        setTag(componentKey, value)
    }

private val byIdKey = tagKey("byId")

internal var View.byId: Boolean
    get() = getTag(byIdKey) as? Boolean ?: false
    set(value) {
        setTag(byIdKey, value)
    }