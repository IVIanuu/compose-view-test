package com.ivianuu.compose

internal fun tagKey(key: String): Int = (3 shl 24) or key.hashCode()