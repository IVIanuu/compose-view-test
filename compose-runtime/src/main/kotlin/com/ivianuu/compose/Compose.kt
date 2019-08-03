package com.ivianuu.compose

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.MainThread
import androidx.compose.Component
import androidx.compose.CompositionContext
import androidx.compose.CompositionReference
import androidx.compose.CurrentComposerAccessor

object Compose {

    class Root(val container: ViewGroup) : Component() {
        fun update() = composer.compose()

        lateinit var composable: ViewComposition.() -> Unit
        lateinit var composer: CompositionContext
        @Suppress("PLUGIN_ERROR")
        override fun compose() {
            val cc = CurrentComposerAccessor.getCurrentComposerNonNull()
            cc.startGroup(0)
            composable(ViewComposition(cc as ViewComposer))
            cc.endGroup()
        }
    }

    private val TAG_ROOT_COMPONENT = "composeRootComponent".hashCode()

    private fun getRootComponent(view: View): Component? {
        return view.getTag(TAG_ROOT_COMPONENT) as? Component
    }

    internal fun setRoot(view: View, component: Component) {
        view.setTag(TAG_ROOT_COMPONENT, component)
    }

    @MainThread
    fun composeInto(
        container: ViewGroup,
        composable: ViewComposition.() -> Unit
    ): CompositionContext? {
        var root = getRootComponent(container) as? Root
        if (root == null) {
            container.removeAllViews()
            root = Root(container)
            root.composable = composable
            setRoot(container, root)
            val cc = CompositionContext.prepare(
                root,
                null
            ) { ViewComposer(root, recomposer = this) }
            root.composer = cc
            root.update()
            return cc
        } else {
            root.composable = composable
            root.update()
        }
        return null
    }

    @MainThread
    fun disposeComposition(container: ViewGroup, parent: CompositionReference? = null) {
        // temporary easy way to call correct lifecycles on everything
        // need to remove compositionContext from context map as well
        composeInto(container) { }
        container.setTag(TAG_ROOT_COMPONENT, null)
    }

}

fun Activity.setViewContent(composable: ViewComposition.() -> Unit): CompositionContext? {
    // If there is already a FrameLayout in the root, we assume we want to compose
    // into it instead of create a new one. This allows for `setContent` to be
    // called multiple times.
    val root = window
        .decorView
        .findViewById<ViewGroup>(android.R.id.content)
        .getChildAt(0) as? ViewGroup
        ?: FrameLayout(this).also {
            it.id = android.R.id.content
            setContentView(it)
        }
    return root.setViewContent(composable)
}

fun Activity.disposeComposition() {
    val view = window
        .decorView
        .findViewById<ViewGroup>(android.R.id.content)
        .getChildAt(0) as? ViewGroup
        ?: error("No root view found")
    Compose.disposeComposition(view, null)
}

fun ViewGroup.setViewContent(composable:  ViewComposition.() -> Unit): CompositionContext? =
    Compose.composeInto(this, composable)

fun ViewGroup.disposeComposition() = Compose.disposeComposition(this, null)
