package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.ComposeAccessor

class CompositionContext(composable: ViewComposition.() -> Unit) {

    private val root = Root(this)

    internal var container: ViewGroup? = null
        private set

    init {
        println("Context: init")
        root.composeContext = androidx.compose.CompositionContext.prepare(
            root.composeComponent,
            null
        ) { ViewComposer(root, recomposer = this) }
        setComposable(composable)
        compose()
    }

    fun setContainer(container: ViewGroup) {
        println("Context: set container $container")
        this.container = container
        root.attachToContainer()
    }

    fun removeContainer() {
        println("Context: remove container")
        root.detachFromContainer()
        this.container = null
    }

    fun setComposable(composable: ViewComposition.() -> Unit) {
        println("Context: set composable")
        root.composable = composable
    }

    fun compose() {
        println("Context: compose")
        root.compose()
    }

    fun dispose() {
        println("Context: dispose")
        removeContainer()
        // todo must be improved
        root.composable = null
        compose()
    }

}

internal class Root(val context: CompositionContext) : GroupComponent<ViewGroup>() {

    init {
        _key = "Root"
    }

    var composable: (ViewComposition.() -> Unit)? = null
    lateinit var composeContext: androidx.compose.CompositionContext

    fun compose() = composeContext.compose()

    val composeComponent = object : androidx.compose.Component() {
        @Suppress("PLUGIN_ERROR")
        override fun compose() {
            val cc = ComposeAccessor.getCurrentComposerNonNull()
            cc.startGroup(0)
            composable?.invoke(ViewComposition(cc as ViewComposer))
            cc.endGroup()
        }
    }

    override fun createView(container: ViewGroup): ViewGroup =
        error("")

    override fun endChildren() {
        super.endChildren()
        val container = context.container ?: return

        val views = children
            .map { child ->
                container.children()
                    .firstOrNull { it.component == child }
                    ?: child.createView(container).also {
                        it.component = child
                    }
            }

        container.getViewManager().setViews(views, true) // todo check for push
        updateView(container)
    }

    fun attachToContainer() {
        println("$key attach to container")
        val container = context.container ?: return
        val views = children.map { child ->
            child.createView(container)
                .also { it.component = child }
        }
        container.getViewManager().rebind(views)
        updateView(container)
    }

    fun detachFromContainer() {
        println("$key detach to container")
        val container = context.container ?: return
        val unprocessedChildren = children.toMutableList()
        container.children().forEach {
            unprocessedChildren.remove(it.component)
            (it.component as Component<View>).destroyView(it)
        }
        check(unprocessedChildren.isEmpty()) { unprocessedChildren }
        container.removeAllViews()
    }

    override fun updateView(view: ViewGroup) {
        super.updateView(view)
        val container = context.container ?: return
        children
            .map { child ->
                container.children()
                    .first { it.component == child }
            }
            .forEach { (it.component as Component<View>).updateView(it) }
    }

}