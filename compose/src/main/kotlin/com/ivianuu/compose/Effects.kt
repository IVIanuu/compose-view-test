package com.ivianuu.compose

import androidx.compose.Ambient
import androidx.compose.CommitScope
import androidx.compose.Effect
import androidx.compose.unaryPlus

fun <T, V1> ViewComposition.key(v1: V1, block: Effect<T>.() -> T) =
    +androidx.compose.key(v1 = v1, block = block)

fun <T, V1, V2> ViewComposition.key(v1: V1, v2: V2, block: Effect<T>.() -> T) =
    +androidx.compose.key(v1 = v1, v2 = v2, block = block)

fun <T> ViewComposition.key(vararg inputs: Any?, block: Effect<T>.() -> T) =
    +androidx.compose.key(inputs = *inputs, block = block)

fun <T> memo(calculation: () -> T) = +androidx.compose.memo(calculation = calculation)

fun <T, V1> ViewComposition.memo(v1: V1, calculation: () -> T) =
    +androidx.compose.memo(v1 = v1, calculation = calculation)

fun <T, V1, V2> ViewComposition.memo(v1: V1, v2: V2, calculation: () -> T) =
    +androidx.compose.memo(v1 = v1, v2 = v2, calculation = calculation)

fun <T> ViewComposition.memo(vararg inputs: Any?, calculation: () -> T) =
    +androidx.compose.memo(inputs = *inputs, calculation = calculation)

fun ViewComposition.onActive(callback: CommitScope.() -> Unit) =
    +androidx.compose.onActive(callback = callback)

fun ViewComposition.onDispose(callback: () -> Unit) =
    +androidx.compose.onDispose(callback = callback)

fun ViewComposition.onCommit(callback: CommitScope.() -> Unit) =
    +androidx.compose.onCommit(callback = callback)

fun <V1> ViewComposition.onCommit(v1: V1, callback: CommitScope.() -> Unit) =
    +androidx.compose.onCommit(v1 = v1, callback = callback)

fun <V1, V2> ViewComposition.onCommit(v1: V1, v2: V2, callback: CommitScope.() -> Unit) =
    +androidx.compose.onCommit(v1 = v1, v2 = v2, callback = callback)

fun ViewComposition.onCommit(vararg inputs: Any?, callback: CommitScope.() -> Unit) =
    +androidx.compose.onCommit(callback = callback)

fun ViewComposition.onPreCommit(callback: CommitScope.() -> Unit) =
    +androidx.compose.onCommit(callback = callback)

fun <V1> ViewComposition.onPreCommit(v1: V1, callback: CommitScope.() -> Unit) =
    +androidx.compose.onPreCommit(v1 = v1, callback = callback)

fun <V1, V2> ViewComposition.onPreCommit(v1: V1, v2: V2, callback: CommitScope.() -> Unit) =
    +androidx.compose.onPreCommit(v1 = v1, v2 = v2, callback = callback)

fun ViewComposition.onPreCommit(vararg inputs: Any?, callback: CommitScope.() -> Unit) =
    +androidx.compose.onPreCommit(inputs = *inputs, callback = callback)

fun <T> ViewComposition.state(init: () -> T) = +androidx.compose.state(init = init)

fun <T, V1> ViewComposition.stateFor(v1: V1, init: () -> T) =
    +androidx.compose.stateFor(v1 = v1, init = init)

fun <T, V1, V2> ViewComposition.stateFor(v1: V1, v2: V2, init: () -> T) =
    +androidx.compose.stateFor(v1 = v1, v2 = v2, init = init)

fun <T> ViewComposition.stateFor(vararg inputs: Any?, init: () -> T) =
    +androidx.compose.stateFor(inputs = *inputs, init = init)

fun <T> ViewComposition.model(init: () -> T) = +androidx.compose.model(init = init)

fun <T, V1> ViewComposition.modelFor(v1: V1, init: () -> T) =
    +androidx.compose.modelFor(v1 = v1, init = init)

fun <T, V1, V2> ViewComposition.modelFor(v1: V1, v2: V2, init: () -> T) =
    +androidx.compose.modelFor(v1 = v1, v2 = v2, init = init)

fun <T> ViewComposition.modelFor(vararg inputs: Any?, init: () -> T) =
    +androidx.compose.modelFor(inputs = *inputs, init = init)

fun <T> ViewComposition.ambient(key: Ambient<T>) = +androidx.compose.ambient(key = key)