package com.ivianuu.compose

import androidx.compose.Ambient
import androidx.compose.CommitScope
import androidx.compose.Effect
import androidx.compose.invalidate
import kotlin.reflect.KProperty

inline fun <T, V1> ViewComposition.key(v1: V1, noinline block: Effect<T>.() -> T) =
    androidx.compose.key(v1 = v1, block = block).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ViewComposition.key(v1: V1, v2: V2, noinline block: Effect<T>.() -> T) =
    androidx.compose.key(v1 = v1, v2 = v2, block = block).resolve(composer, sourceLocation())

inline fun <T> ViewComposition.key(vararg inputs: Any?, noinline block: Effect<T>.() -> T) =
    androidx.compose.key(inputs = *inputs, block = block).resolve(composer, sourceLocation())

inline fun <T> ViewComposition.memo(noinline calculation: () -> T) =
    androidx.compose.memo(calculation = calculation).resolve(composer, sourceLocation())

inline fun <T, V1> ViewComposition.memo(v1: V1, noinline calculation: () -> T) =
    androidx.compose.memo(v1 = v1, calculation = calculation).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ViewComposition.memo(v1: V1, v2: V2, noinline calculation: () -> T) =
    androidx.compose.memo(v1 = v1, v2 = v2, calculation = calculation).resolve(
        composer,
        sourceLocation()
    )

inline fun <T> ViewComposition.memo(vararg inputs: Any?, noinline calculation: () -> T) =
    androidx.compose.memo(inputs = *inputs, calculation = calculation).resolve(
        composer,
        sourceLocation()
    )

inline fun ViewComposition.onActive(noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onActive(callback = callback).resolve(composer, sourceLocation())

inline fun ViewComposition.onDispose(noinline callback: () -> Unit) =
    androidx.compose.onDispose(callback = callback).resolve(composer, sourceLocation())

inline fun ViewComposition.onCommit(noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onCommit(callback = callback).resolve(composer, sourceLocation())

inline fun <V1> ViewComposition.onCommit(v1: V1, noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onCommit(v1 = v1, callback = callback).resolve(composer, sourceLocation())

inline fun <V1, V2> ViewComposition.onCommit(
    v1: V1,
    v2: V2,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(v1 = v1, v2 = v2, callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun ViewComposition.onCommit(
    vararg inputs: Any?,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onCommit(callback = callback).resolve(composer, sourceLocation())

inline fun ViewComposition.onPreCommit(noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onCommit(callback = callback).resolve(composer, sourceLocation())

inline fun <V1> ViewComposition.onPreCommit(v1: V1, noinline callback: CommitScope.() -> Unit) =
    androidx.compose.onPreCommit(v1 = v1, callback = callback).resolve(composer, sourceLocation())

inline fun <V1, V2> ViewComposition.onPreCommit(
    v1: V1,
    v2: V2,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(v1 = v1, v2 = v2, callback = callback).resolve(
        composer,
        sourceLocation()
    )

inline fun ViewComposition.onPreCommit(
    vararg inputs: Any?,
    noinline callback: CommitScope.() -> Unit
) =
    androidx.compose.onPreCommit(inputs = *inputs, callback = callback).resolve(
        composer,
        sourceLocation()
    )

class State<T> @PublishedApi internal constructor(value: T, val invalidate: () -> Unit) {

    @Suppress("UNCHECKED_CAST")
    var value: T = value
        set(value) {
            field = value
            invalidate()
        }

    operator fun component1(): T = value

    operator fun component2(): (T) -> Unit = { value = it }

    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = value

    operator fun setValue(thisObj: Any?, property: KProperty<*>, next: T) {
        value = next
    }
}

inline fun <T> ViewComposition.state(noinline init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo { State(init(), invalidate) }
}

inline fun <T, V1> ViewComposition.stateFor(v1: V1, noinline init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo(v1) { State(init(), invalidate) }
}

inline fun <T, V1, V2> ViewComposition.stateFor(v1: V1, v2: V2, noinline init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo(v1, v2) { State(init(), invalidate) }
}

inline fun <T> ViewComposition.stateFor(vararg inputs: Any?, noinline init: () -> T): State<T> {
    val invalidate = +invalidate
    return memo(*inputs) { State(init(), invalidate) }
}

inline fun <T> ViewComposition.model(noinline init: () -> T) =
    androidx.compose.model(init = init).resolve(composer, sourceLocation())

inline fun <T, V1> ViewComposition.modelFor(v1: V1, noinline init: () -> T) =
    androidx.compose.modelFor(v1 = v1, init = init).resolve(composer, sourceLocation())

inline fun <T, V1, V2> ViewComposition.modelFor(v1: V1, v2: V2, noinline init: () -> T) =
    androidx.compose.modelFor(v1 = v1, v2 = v2, init = init).resolve(composer, sourceLocation())

inline fun <T> ViewComposition.modelFor(vararg inputs: Any?, noinline init: () -> T) =
    androidx.compose.modelFor(inputs = *inputs, init = init).resolve(composer, sourceLocation())

inline fun <T> ViewComposition.ambient(key: Ambient<T>) =
    androidx.compose.ambient(key = key).resolve(composer, sourceLocation())

@PublishedApi
internal inline fun <T> Effect<T>.resolve(composer: ViewComposer, key: Any) =
    resolve(composer, key.hashCode())