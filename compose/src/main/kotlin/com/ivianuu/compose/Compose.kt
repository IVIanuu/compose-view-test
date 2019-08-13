/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose

import android.view.ViewGroup
import androidx.compose.ComposeAccessor

class CompositionContext(composable: ViewComposition.() -> Unit) {

    private val root = Root()

    internal var container: ViewGroup? = null
        private set

    init {
        log { "Context: init" }
        root.composeContext = androidx.compose.CompositionContext.prepare(
            root.composeComponent,
            null
        ) { ViewComposer(root, recomposer = this) }
        setComposable(composable)
        compose()
    }

    fun setContainer(container: ViewGroup) {
        log { "Context: set container $container" }
        this.container = container
        root.createView(container)
        root.bindView(container)
    }

    fun removeContainer() {
        log { "Context: remove container" }
        root.unbindView(container!!)
        this.container = null
    }

    fun setComposable(composable: ViewComposition.() -> Unit) {
        log { "Context: set composable" }
        root.composable = composable
    }

    fun compose() {
        log { "Context: compose" }
        root.compose()
    }

    fun dispose() {
        log { "Context: dispose" }
        removeContainer()
        // todo must be improved
        root.composable = null
        compose()
    }

}

internal class Root : Component<ViewGroup>() {

    var composable: (ViewComposition.() -> Unit)? = null
    lateinit var composeContext: androidx.compose.CompositionContext

    init {
        _key = "Root"
    }

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

    override fun onCreateView(container: ViewGroup): ViewGroup = container
}