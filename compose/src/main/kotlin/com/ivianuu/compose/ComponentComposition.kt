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
import com.ivianuu.compose.internal.log
import java.util.*

@EffectsDsl
open class ComponentComposition internal constructor(val composer: Composer<Component<*>>) {

    private val keysStack = Stack<MutableList<Any>>()
    private var keys = mutableListOf<Any>()

    fun <T : View> emit(
        key: Any,
        viewType: Any,
        childViewController: ChildViewController<T>,
        createView: (ViewGroup) -> T,
        block: (ComponentBuilder<T>.() -> Unit)? = null
    ) = with(composer) {
        checkIsComposing()

        val environment = ambient(ComponentEnvironmentAmbient)

        val finalKey = environment.joinKey(key)

        check(finalKey !in keys) {
            "Duplicated key $finalKey"
        }

        keys.add(finalKey)

        keysStack.push(keys)
        keys = mutableListOf()
        startNode(finalKey)

        log { "emit $finalKey inserting ? $inserting" }
        val node = if (inserting) {
            Component(viewType, childViewController, createView)
                .also { emitNode(it) }
        } else {
            useNode() as Component<T>
        }

        node._key = finalKey

        node.inChangeHandler = environment.inChangeHandler
        node.outChangeHandler = environment.outChangeHandler
        node.isPush = environment.isPush
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

        onCommit { node.update() }

        endNode()

        keys = keysStack.pop()
        if (keysStack.isEmpty()) {
            keys.clear()
        }
    }

}