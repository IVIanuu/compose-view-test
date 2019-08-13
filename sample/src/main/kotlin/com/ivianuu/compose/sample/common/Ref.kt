/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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