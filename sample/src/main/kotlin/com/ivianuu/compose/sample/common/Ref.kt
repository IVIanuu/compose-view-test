package com.ivianuu.compose.sample.common

typealias RefUpdateCallback<T> = (oldValue: T, newValue: T) -> Unit

class Ref<T>(value: T) {

    var value: T = value
        set(value) {
            val oldValue = field
            field = value
            callbacks.toList().forEach { it(oldValue, value) }
        }

    private val callbacks = mutableListOf<RefUpdateCallback<T>>()

    operator fun invoke(): T = value

    fun onUpdate(callback: RefUpdateCallback<T>): () -> Unit {
        callbacks.add(callback)
        return { callbacks.remove(callback) }
    }
}