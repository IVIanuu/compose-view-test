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

import android.view.View
import android.view.ViewGroup
import androidx.compose.Composer
import androidx.compose.EffectsDsl
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient
import com.ivianuu.compose.internal.ViewUpdater
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.join
import com.ivianuu.compose.internal.log

@EffectsDsl
open class ComponentComposition internal constructor(val composer: Composer<Component<*>>) {

    fun <T : View> emit(
        key: Any,
        viewType: Any,
        createView: (ViewGroup) -> T,
        block: (ComponentBuilder<T>.() -> Unit)? = null
    ) = with(composer) {
        checkIsComposing()

        val environment = ambient(ComponentEnvironmentAmbient)
        val finalKey = environment.joinKey(key)

        log { "composer: emit $finalKey" }

        join {
            log { "composer: run $finalKey inserting ? $inserting" }

            startNode(finalKey)

            val node = if (inserting) {
                Component(
                    key = finalKey,
                    viewType = viewType,
                    createView = createView
                ).also { emitNode(it) }
            } else {
                useNode() as Component<T>
            }

            node.inChangeHandler = environment.inChangeHandler
            environment.inChangeHandler = null
            node.outChangeHandler = environment.outChangeHandler
            environment.outChangeHandler = null
            node.isPush = environment.isPush
            environment.isPush = true
            node.hidden = environment.hidden
            environment.hidden = false
            node.byId = environment.byId
            environment.byId = false

            if (block != null) {
                val updater = ViewUpdater<T>(composer)
                environment.pushComponent(node)
                environment.viewUpdater = updater
                ComponentBuilder(composer, node).block()
                node.viewUpdater = updater
                if (updater.hasChanges) {
                    node.generation++
                }

                environment.viewUpdater = null
                environment.popComponent()
            }

            onCommit {
                node.boundViews.forEach {
                    node.bindView(it)
                }
            }

            endNode()
        }
    }

}