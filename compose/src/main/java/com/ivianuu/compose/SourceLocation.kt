package com.ivianuu.compose

inline fun sourceLocation(): String {
    val element = Throwable().stackTrace.first()
    return "${element.className}:${element.methodName}:${element.lineNumber}"
}