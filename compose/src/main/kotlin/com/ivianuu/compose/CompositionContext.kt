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
import androidx.compose.Composer
import com.ivianuu.compose.internal.ComponentComposer
import com.ivianuu.compose.internal.ComponentEnvironment
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient
import com.ivianuu.compose.internal.log

class CompositionContext {

    private val root = Component(
        key = "Root",
        createView = { it }
    )

    private val composeComponent = object : androidx.compose.Component() {
        @Suppress("PLUGIN_ERROR")
        override fun compose() {
            val cc = ComposeAccessor.getCurrentComposerNonNull()
            cc.startGroup(0)

            with(ComponentComposition(cc as Composer<Component<*>>)) {
                val state = memo { ComponentEnvironment() }
                ComponentEnvironmentAmbient.Provider(value = state) {
                    state.pushComponent(root)
                    this@CompositionContext.composable?.invoke(this)
                    state.popComponent()
                }
                state.reset()
            }

            cc.endGroup()
        }
    }

    private val composeContext = androidx.compose.CompositionContext.prepare(
        composeComponent,
        null
    ) { ComponentComposer(root, recomposer = this) }

    private var composable: (ComponentComposition.() -> Unit)? = null

    internal var container: ViewGroup? = null
        private set

    init {
        log { "context: init" }
    }

    fun setContainer(container: ViewGroup) {
        removeContainer()

        log { "context: set container $container" }
        this.container = container
        root.createView(container)
    }

    fun removeContainer() {
        log { "context: remove container" }
        root.destroyView()
        this.container = null
    }

    fun setComposable(composable: ComponentComposition.() -> Unit) {
        log { "context: set composable" }
        this.composable = composable
    }

    fun compose() {
        log { "context: compose" }
        composeContext.compose()
    }

    fun dispose() {
        log { "context: dispose" }
        removeContainer()
        // todo must be improved
        this.composable = null
        compose()
    }

}