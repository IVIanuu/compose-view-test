package com.ivianuu.compose

import androidx.compose.Applier
import androidx.compose.ApplyAdapter
import androidx.compose.ComposeAccessor
import androidx.compose.Composer
import androidx.compose.Effect
import androidx.compose.EffectsDsl
import androidx.compose.FrameManager
import androidx.compose.Recomposer
import androidx.compose.SlotTable
import java.util.*

class ViewApplyAdapter(private val root: Any) : ApplyAdapter<Any> {

    private var current = root
    private val currentStack = Stack<Any>()

    override fun Any.start(instance: Any) {
        currentStack.push(current)
        current = this
        if (this is GroupComponent<*>) {
            beginChildren()
        }
    }

    override fun Any.insertAt(index: Int, instance: Any) {
        when (this) {
            is GroupComponent<*> -> addChild(index, instance as Component<*>)
            else -> error("Unexpected node $this")
        }
    }

    override fun Any.move(from: Int, to: Int, count: Int) {
        when (this) {
            is GroupComponent<*> -> {
                repeat(count) {
                    moveChild(from, to)
                }
            }
            else -> error("Unexpected node $this")
        }
    }

    override fun Any.removeAt(index: Int, count: Int) {
        when (this) {
            is GroupComponent<*> -> {
                (index..count).forEach { removeChild(it) }
            }
            else -> error("Unexpected node $this")
        }
    }

    override fun Any.end(instance: Any, parent: Any) {
        if (this != current && current == instance) {
            if (instance is GroupComponent<*>) {
                instance.endChildren()
            }
            current = currentStack.pop()
            if (current == root) {
                (current as GroupComponent<*>).endChildren()
            }
        }
    }

}

class ViewComposer(
    val root: Any,
    val applyAdapter: ViewApplyAdapter = ViewApplyAdapter(root),
    recomposer: Recomposer
) : Composer<Any>(
    SlotTable(),
    Applier(root, applyAdapter), recomposer
) {

    init {
        FrameManager.ensureStarted()
    }

}

@Suppress("UNCHECKED_CAST")
@EffectsDsl
class ViewComposition(val composer: ViewComposer) {

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun <V> Effect<V>.unaryPlus(): V {
        check(ComposeAccessor.isComposing(this@ViewComposition.composer)) {
            "Can only use effects while composing"
        }
        return resolve(this@ViewComposition.composer, sourceLocation().hashCode())
    }

    fun <T : Component<*>> emit(
        key: Any,
        ctor: () -> T,
        update: (T.() -> Unit)? = null, // todo
        children: (ViewComposition.() -> Unit)? = null
    ) = with(composer) {
        startNode(key)
        val node = if (inserting) {
            ctor().also { emitNode(it) }
        } else {
            useNode() as T
        }

        node._key = key

        update?.let { node.it() }
        children?.invoke(this@ViewComposition)

        endNode()
    }

    inline fun group(noinline children: ViewComposition.() -> Unit) =
        group(sourceLocation(), children)

    fun group(
        key: Any,
        children: ViewComposition.() -> Unit
    ) = with(composer) {
        startGroup(key)
        children()
        endGroup()
    }

}