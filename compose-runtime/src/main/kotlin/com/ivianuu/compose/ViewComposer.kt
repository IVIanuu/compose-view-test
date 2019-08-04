package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.Applier
import androidx.compose.ApplyAdapter
import androidx.compose.Composer
import androidx.compose.Effect
import androidx.compose.EffectsDsl
import androidx.compose.FrameManager
import androidx.compose.Recomposer
import androidx.compose.SlotTable
import androidx.compose.ViewUpdater
import com.ivianuu.compose.util.sourceLocation
import java.util.*

class ViewApplyAdapter(private val root: Any) : ApplyAdapter<Any> {

    private sealed class Op {
        data class Insert(val index: Int, val instance: Any) : Op()
        data class Move(val from: Int, val to: Int, val count: Int) : Op()
        data class Remove(val index: Int, val count: Int) : Op()
    }

    private var current = root
    private val currentStack = Stack<Any>()
    private var ops = mutableListOf<Op>()
    private val opsStack = Stack<MutableList<Op>>()

    override fun Any.start(instance: Any) {
        currentStack.push(current)
        current = this
        opsStack.push(ops)
        ops = mutableListOf()
    }

    override fun Any.insertAt(index: Int, instance: Any) {
        ops.add(Op.Insert(index, instance))
    }

    override fun Any.move(from: Int, to: Int, count: Int) {
        ops.add(Op.Move(from, to, count))
    }

    override fun Any.removeAt(index: Int, count: Int) {
        ops.add(Op.Remove(index, count))
    }

    override fun Any.end(instance: Any, parent: Any) {
        if (this != current && current == instance) {
            executeOps()
            current = currentStack.pop()
            ops = opsStack.pop()
            if (current == root) {
                executeOps()
                ops.clear()
            }
        }
    }

    private fun executeOps() {
        val current = current
        val container = when (current) {
            is ViewGroup -> current
            is Compose.Root -> current.container
            else -> error("Unsupported node type ${current.javaClass.simpleName}")
        }

        val viewManager = container.getViewManager()

        val oldViews = viewManager.views
        val newViews = oldViews.toMutableList()

        var insertCount = 0
        var removeCount = 0

        ops.forEach { op ->
            when (op) {
                is Op.Insert -> {
                    newViews.add(op.index, op.instance as View)
                    insertCount++
                }
                is Op.Move -> {
                    if (op.from > op.to) {
                        var currentFrom = op.from
                        var currentTo = op.to
                        repeat(op.count) {
                            Collections.swap(newViews, currentFrom, currentTo)
                            currentFrom++
                            currentTo++
                        }
                    } else {
                        repeat(op.count) {
                            Collections.swap(newViews, op.from, op.to - 1)
                        }
                    }
                }
                is Op.Remove -> {
                    for (i in op.index + op.count - 1 downTo op.index) {
                        newViews.removeAt(i)
                        removeCount++
                    }
                }
            }
        }

        viewManager.setViews(newViews, insertCount >= removeCount)
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
    inline operator fun <V> Effect<V>.unaryPlus(): V =
        resolve(this@ViewComposition.composer, sourceLocation().hashCode())

    private var currentContainer = (composer.root as Compose.Root).container

    fun <T : View> emit(
        key: Any,
        ctor: (ViewGroup) -> T,
        update: (ViewUpdater<T>.() -> Unit)? = null,
        children: (ViewComposition.() -> Unit)? = null
    ) = with(composer) {
        startNode(key)
        val node = if (inserting) {
            ctor(currentContainer).also { emitNode(it) }
        } else {
            useNode() as T
        }

        update?.let { ViewUpdater(this, node).it() }

        if (children != null) {
            node as ViewGroup
            val previousContainer = currentContainer
            currentContainer = node
            children()
            currentContainer = previousContainer
        }

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