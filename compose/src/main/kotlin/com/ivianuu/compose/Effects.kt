package com.ivianuu.compose

import androidx.compose.Ambient
import androidx.compose.CommitScope
import androidx.compose.Effect
import androidx.compose.invalidate
import androidx.compose.unaryPlus
import kotlin.reflect.KProperty

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

class State<T> @PublishedApi internal constructor(value: T, val invalidate: () -> Unit) {
    /* NOTE(lmr): When this module is compiled with IR, we will need to remove the below Framed implementation */

    @Suppress("UNCHECKED_CAST")
    var value: T = value
        set(value) {
            field = value
            invalidate()
        }

    /**
     * The componentN() operators allow state objects to be used with the property destructuring syntax
     *
     * var (foo, setFoo) = +state { 0 }
     * setFoo(123) // set
     * foo == 123 // get
     */
    operator fun component1(): T = value

    operator fun component2(): (T) -> Unit = { value = it }

    /**
     * The getValue/setValue operators allow State to be used as a local variable with a delegate:
     *
     * var foo by +state { 0 }
     * foo += 123 // uses setValue(...)
     * foo == 123 // uses getValue(...)
     */
    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, next: T) {
        value = next
    }
}

fun <T> ViewComposition.state(init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo { State(init(), invalidate) }
}

fun <T, V1> ViewComposition.stateFor(v1: V1, init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo(v1) { State(init(), invalidate) }
}

fun <T, V1, V2> ViewComposition.stateFor(v1: V1, v2: V2, init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo(v1, v2) { State(init(), invalidate) }
}

fun <T> ViewComposition.stateFor(vararg inputs: Any?, init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo(*inputs) { State(init(), invalidate) }
}

fun <T> ViewComposition.model(init: () -> T) = +androidx.compose.model(init = init)

fun <T, V1> ViewComposition.modelFor(v1: V1, init: () -> T) =
    +androidx.compose.modelFor(v1 = v1, init = init)

fun <T, V1, V2> ViewComposition.modelFor(v1: V1, v2: V2, init: () -> T) =
    +androidx.compose.modelFor(v1 = v1, v2 = v2, init = init)

fun <T> ViewComposition.modelFor(vararg inputs: Any?, init: () -> T) =
    +androidx.compose.modelFor(inputs = *inputs, init = init)

fun <T> ViewComposition.ambient(key: Ambient<T>) = +androidx.compose.ambient(key = key)