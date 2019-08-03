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
import androidx.compose.ambient
import com.ivianuu.compose.transition.InTransitionAmbient
import com.ivianuu.compose.transition.OutTransitionAmbient
import com.ivianuu.compose.transition.inTransition
import com.ivianuu.compose.transition.outTransition
import com.ivianuu.compose.util.sourceLocation
import java.util.*

private fun invalidNode(node: Any): Nothing =
    error("Unsupported node type ${node.javaClass.simpleName}")

class ViewApplyAdapter(root: Any) : ApplyAdapter<Any> {

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
    }

    override fun Any.insertAt(index: Int, instance: Any) {
        if (current != this) {
            currentStack.push(current)
            current = this
            println("start $this instance $instance")
            opsStack.push(ops)
            ops = mutableListOf()
        }

        println("insert $this index $index instance $instance")
        ops.add(Op.Insert(index, instance))
    }

    override fun Any.move(from: Int, to: Int, count: Int) {
        println("move $this from $from to $to count $count")
        ops.add(Op.Move(from, to, count))
    }

    override fun Any.removeAt(index: Int, count: Int) {
        println("remove at $this index $index count $count")
        ops.add(Op.Remove(index, count))
    }

    override fun Any.end(instance: Any, parent: Any) {
        if (ops.isNotEmpty()) {
            val container = when (this) {
                is ViewGroup -> this
                is Compose.Root -> container
                else -> invalidNode(this)
            }

            println("container for $this is $container")

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

        if (!opsStack.isEmpty()) ops = opsStack.pop()
        if (!currentStack.isEmpty()) current = currentStack.pop()

        println("ended $this $instance ops stack is $opsStack")
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

        node.inTransition = +ambient(InTransitionAmbient)
        node.outTransition = +ambient(OutTransitionAmbient)

        if (children != null) {
            node as ViewGroup
            val previousContainer = currentContainer
            currentContainer = node
            children()
            currentContainer = previousContainer
        }

        endNode()
    }

}