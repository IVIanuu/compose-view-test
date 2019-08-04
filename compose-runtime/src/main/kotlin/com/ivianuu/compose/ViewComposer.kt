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

private fun invalidNode(node: Any): Nothing =
    error("Unsupported node type ${node.javaClass.simpleName}")

class ViewApplyAdapter(private val root: Any) : ApplyAdapter<Any> {

    override fun Any.start(instance: Any) {
    }

    override fun Any.insertAt(index: Int, instance: Any) {
        val container = when (this) {
            is ViewGroup -> this
            is Compose.Root -> container
            else -> invalidNode(this)
        }

        container.getViewManager().addView(index, instance as View)
    }

    override fun Any.move(from: Int, to: Int, count: Int) {
        val container = when (this) {
            is ViewGroup -> this
            is Compose.Root -> container
            else -> invalidNode(this)
        }

        container.getViewManager().moveViews(from, to, count)

    }

    override fun Any.removeAt(index: Int, count: Int) {
        val container = when (this) {
            is ViewGroup -> this
            is Compose.Root -> container
            else -> invalidNode(this)
        }

        container.removeViews(index, count)
    }

    override fun Any.end(instance: Any, parent: Any) {
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

}