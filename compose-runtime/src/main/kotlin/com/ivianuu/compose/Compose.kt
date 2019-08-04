package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.ComposeAccessor

class CompositionContext(composable: ViewComposition.() -> Unit) {

    private val root = Root(this)

    internal var container: ViewGroup? = null
        private set

    init {
        root.composeContext = androidx.compose.CompositionContext.prepare(
            root.composeComponent,
            null
        ) { ViewComposer(root, recomposer = this) }
        root.composable = composable
        root.compose()
    }

    fun setContainer(container: ViewGroup) {
        this.container = container
        root.attachToContainer()
    }

    fun removeContainer() {
        root.detachFromContainer()
        this.container = null
    }

    fun setComposable(composable: ViewComposition.() -> Unit) {
        root.composable = composable
        root.compose()
    }

    fun dispose() {
        removeContainer()
        // todo must be improved
        root.composable = null
        root.compose()
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
                .also {
                    println("created view $it for child ${child.key}")
                    it.component = child
                }
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