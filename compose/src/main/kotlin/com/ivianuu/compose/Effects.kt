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
import androidx.compose.Composer
import androidx.compose.Effect
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient
import com.ivianuu.compose.internal.JoinedKey
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.sourceLocation

inline fun <T> ComponentComposition.memo(noinline calculation: () -> T) =
    androidx.compose.memo(calculation = calculation).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.memo(vararg inputs: Any?, noinline calculation: () -> T) =
    androidx.compose.memo(inputs = *inputs, calculation = calculation).resolve(
        composer,
        sourceLocation()
    )

inline fun ComponentComposition.onActive(
    noinline callback: CommitScope.() -> Unit
) = androidx.compose.onActive(callback = callback).resolve(
    composer,
    sourceLocation()
)

inline fun ComponentComposition.onActive(
    vararg inputs: Any?,
    noinline callback: CommitScope.() -> Unit
) {
    key(*inputs) { onActive(callback) }
}

inline fun ComponentComposition.onDispose(
    noinline callback: () -> Unit
) = androidx.compose.onDispose(callback = callback).resolve(
    composer,
    sourceLocation()
)

inline fun ComponentComposition.onDispose(
    vararg inputs: Any?,
    noinline callback: () -> Unit
) {
    key(*inputs) { onDispose(callback) }
}

inline fun ComponentComposition.onCommit(
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun ComponentComposition.onCommit(
    vararg inputs: Any?,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(inputs = *inputs, callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun ComponentComposition.onPreCommit(
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(callback = callback).resolve(
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
    androidx.compose.state(init = init).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.stateFor(vararg inputs: Any?, noinline init: () -> T) =
    androidx.compose.stateFor(inputs = *inputs, init = init).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.model(noinline init: () -> T) =
    androidx.compose.modelFor(init = init).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.modelFor(vararg inputs: Any?, noinline init: () -> T) =
    androidx.compose.modelFor(inputs = *inputs, init = init).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ComponentComposition.ambient(key: Ambient<T>) =
    androidx.compose.ambient(key = key).resolve(
        composer,
        sourceLocation()
    )

fun <T> ComponentComposition.key(
    key: Any,
    block: ComponentComposition.() -> T
): T = with(composer) {
    checkIsComposing()
    val environment = ambient(ComponentEnvironmentAmbient)
    environment.pushGroupKey(key)
    startGroup(key)
    val result = block()
    endGroup()
    environment.popGroupKey()
    return@with result
}

inline fun <T> ComponentComposition.key(
    vararg inputs: Any?,
    noinline block: ComponentComposition.() -> T
) = key(key = sourceLocation(), inputs = *inputs, block = block)

fun <T> ComponentComposition.key(
    key: Any,
    vararg inputs: Any?,
    block: ComponentComposition.() -> T
): T {
    val inputsKey = inputs.reduce { acc, any -> JoinedKey(acc, any) }
    val finalKey = if (inputsKey != null) JoinedKey(key, inputsKey) else key
    return key(finalKey, block)
}

@PublishedApi
internal fun <T> Effect<T>.resolve(composer: Composer<Component<*>>, key: Any): T {
    composer.checkIsComposing()
    return resolve(composer, key.hashCode())
}