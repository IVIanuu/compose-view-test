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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.Composer
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient
import com.ivianuu.compose.internal.ViewUpdater
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.stackTrace

//@EffectsDsl
class ComponentComposition(val composer: Composer<Component<*>>) {

    fun <T : View> emit(
        key: Any,
        viewKey: Any,
        createView: (ViewGroup, Context) -> T,
        block: (ComponentBuilder<T>.() -> Unit)? = null
    ) = with(composer) {
        checkIsComposing()

        val environment = ambient(ComponentEnvironmentAmbient)
        val finalKey = environment.joinKey(key)

        stackTrace { "composer: emit $finalKey" }

        startNode(finalKey)

        val node = if (inserting) {
            Component(
                key = finalKey,
                viewKey = viewKey,
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
        node.shareViews = environment.shareViews
        environment.shareViews = true
        node.byId = environment.byId
        environment.byId = false

        node.contextMapper = ambient(ContextMapperAmbient)

        var changed = false

        if (block != null) {
            val updater = ViewUpdater<T>(composer)
            environment.pushComponent(node)
            environment.pushKey()
            environment.viewUpdater = updater
            ComponentBuilder(this@ComponentComposition, node).block()
            node.viewUpdater = updater
            if (updater.hasChanges) {
                node.generation++
                changed = true
            }

            environment.viewUpdater = null
            environment.popKey()
            environment.popComponent()
        }

        onCommit {
            if (changed) {
                node.update()
            }
        }

        endNode()
    }

}