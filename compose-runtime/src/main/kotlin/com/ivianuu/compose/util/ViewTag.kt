package com.ivianuu.compose.util

internal fun tagKey(key: String): Int = (3 shl 24) or key.hashCode()

val key = tagKey("key")