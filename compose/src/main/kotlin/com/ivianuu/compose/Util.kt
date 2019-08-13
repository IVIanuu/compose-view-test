package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

var loggingEnabled = true

inline fun log(block: () -> String) {
    if (loggingEnabled) {
        println(block())
    }
}

inline fun sourceLocation(): String {
    val element = Throwable().stackTrace.first()
    return "${element.className}:${element.methodName}:${element.lineNumber}"
}

fun ViewGroup.children(): List<View> {
    return (0 until childCount)
        .map { getChildAt(it) }
}

internal fun tagKey(key: String): Int = (3 shl 24) or key.hashCode()

private val componentKey = tagKey("component")

var View.component: Component<*>?
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