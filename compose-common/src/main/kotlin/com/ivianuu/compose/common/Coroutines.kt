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

package com.ivianuu.compose.common

import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.memo
import com.ivianuu.compose.onActive
import com.ivianuu.compose.onCommit
import com.ivianuu.compose.onDispose
import com.ivianuu.compose.onPreCommit
import com.ivianuu.compose.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val ComponentComposition.coroutineScope: CoroutineScope
    get() = coroutineScope { Dispatchers.Main }

fun ComponentComposition.coroutineScope(context: () -> CoroutineContext): CoroutineScope {
    val coroutineScope = memo { CoroutineScope(context = context() + Job()) }
    onDispose { coroutineScope.coroutineContext[Job]!!.cancel() }
    return coroutineScope
}

// todo launchOnBindView ?

fun ComponentComposition.launchOnActive(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = coroutineScope
    onActive { coroutineScope.launch(context, start, block) }
}

fun ComponentComposition.launchOnPreCommit(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = coroutineScope
    onPreCommit { coroutineScope.launch(context, start, block) }
}

fun ComponentComposition.launchOnPreCommit(
    vararg inputs: Any?,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = coroutineScope
    onPreCommit(*inputs) { coroutineScope.launch(context, start, block) }
}

fun ComponentComposition.launchOnCommit(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = coroutineScope
    onCommit { coroutineScope.launch(context, start, block) }
}

fun ComponentComposition.launchOnCommit(
    vararg inputs: Any?,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = coroutineScope
    onCommit(*inputs) { coroutineScope.launch(context, start, block) }
}

@BuilderInference
fun <T> ComponentComposition.load(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): T? = load(placeholder = null, context = context, start = start, block = block)

fun <T> ComponentComposition.load(
    placeholder: T,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): T {
    val state = state { placeholder }
    launchOnActive(context = context, start = start) {
        state.value = block()
    }
    return state.value
}

fun <T> ComponentComposition.flow(
    flow: Flow<T>
): T? = flow(null, flow)

fun <T> ComponentComposition.flow(
    placeholder: T,
    flow: Flow<T>
): T {
    val state = state { placeholder }
    launchOnActive { flow.collect { state.value = it } }
    return state.value
}