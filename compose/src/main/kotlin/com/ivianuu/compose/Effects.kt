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

package com.ivianuu.compose

import androidx.compose.Ambient
import androidx.compose.CommitScope
import androidx.compose.Effect

inline fun <T, V1> ComponentComposition.key(v1: V1, noinline block: Effect<T>.() -> T) =
    androidx.compose.key(v1 = v1, block = block).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ComponentComposition.key(v1: V1, v2: V2, noinline block: Effect<T>.() -> T) =
    androidx.compose.key(v1 = v1, v2 = v2, block = block).resolve(composer, sourceLocation())

inline fun <T> ComponentComposition.key(vararg inputs: Any?, noinline block: Effect<T>.() -> T) =
    androidx.compose.key(inputs = *inputs, block = block).resolve(composer, sourceLocation())

inline fun <T> ComponentComposition.memo(noinline calculation: () -> T) =
    androidx.compose.memo(calculation = calculation).resolve(composer, sourceLocation())

inline fun <T, V1> ComponentComposition.memo(v1: V1, noinline calculation: () -> T) =
    androidx.compose.memo(v1 = v1, calculation = calculation).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ComponentComposition.memo(v1: V1, v2: V2, noinline calculation: () -> T) =
    androidx.compose.memo(v1 = v1, v2 = v2, calculation = calculation).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.memo(vararg inputs: Any?, noinline calculation: () -> T) =
    androidx.compose.memo(inputs = *inputs, calculation = calculation).resolve(
        composer,
        sourceLocation()
    )

inline fun ComponentComposition.onActive(noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onActive(callback = callback).resolve(composer, sourceLocation())

inline fun ComponentComposition.onDispose(noinline callback: () -> Unit) =
    androidx.compose.onDispose(callback = callback).resolve(composer, sourceLocation())

inline fun ComponentComposition.onCommit(noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onCommit(callback = callback).resolve(composer, sourceLocation())

inline fun <V1> ComponentComposition.onCommit(v1: V1, noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onCommit(v1 = v1, callback = callback).resolve(composer, sourceLocation())

inline fun <V1, V2> ComponentComposition.onCommit(
    v1: V1,
    v2: V2,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(v1 = v1, v2 = v2, callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun ComponentComposition.onCommit(
    vararg inputs: Any?,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(callback = callback).resolve(composer, sourceLocation())

inline fun ComponentComposition.onPreCommit(noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onCommit(callback = callback).resolve(composer, sourceLocation())

inline fun <V1> ComponentComposition.onPreCommit(
    v1: V1,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(v1 = v1, callback = callback).resolve(composer, sourceLocation())

inline fun <V1, V2> ComponentComposition.onPreCommit(
    v1: V1,
    v2: V2,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(v1 = v1, v2 = v2, callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun ComponentComposition.onPreCommit(
    vararg inputs: Any?,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(inputs = *inputs, callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.state(noinline init: () -> T) =
    androidx.compose.state(init = init).resolve(composer, sourceLocation())

inline fun <T, V1> ComponentComposition.stateFor(v1: V1, noinline init: () -> T) =
    androidx.compose.stateFor(v1 = v1, init = init).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ComponentComposition.stateFor(v1: V1, v2: V2, noinline init: () -> T) =
    androidx.compose.stateFor(v1 = v1, v2 = v2, init = init).resolve(composer, sourceLocation())

inline fun <T> ComponentComposition.stateFor(vararg inputs: Any?, noinline init: () -> T) =
    androidx.compose.stateFor(inputs = *inputs, init = init).resolve(composer, sourceLocation())

inline fun <T> ComponentComposition.model(noinline init: () -> T) =
    androidx.compose.model(init = init).resolve(composer, sourceLocation())

inline fun <T, V1> ComponentComposition.modelFor(v1: V1, noinline init: () -> T) =
    androidx.compose.modelFor(v1 = v1, init = init).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ComponentComposition.modelFor(v1: V1, v2: V2, noinline init: () -> T) =
    androidx.compose.modelFor(v1 = v1, v2 = v2, init = init).resolve(composer, sourceLocation())

inline fun <T> ComponentComposition.modelFor(vararg inputs: Any?, noinline init: () -> T) =
    androidx.compose.modelFor(inputs = *inputs, init = init).resolve(composer, sourceLocation())

inline fun <T> ComponentComposition.ambient(key: Ambient<T>) =
    androidx.compose.ambient(key = key).resolve(composer, sourceLocation())

@PublishedApi
internal inline fun <T> Effect<T>.resolve(composer: ComponentComposer, key: Any) =
    resolve(composer, key.hashCode())