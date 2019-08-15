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
import com.ivianuu.compose.internal.ViewUpdater
import com.ivianuu.compose.internal.checkIsComposing
import com.ivianuu.compose.internal.log
import java.util.*

@EffectsDsl
open class ComponentComposition internal constructor(val composer: Composer<Component<*>>) {

    private val keysStack = Stack<MutableList<Any>>()
    private var keys = mutableListOf<Any>()

    private val groupKeyStack = Stack<Any?>()
    private var groupKey: Any? = null

    fun <T : View> emit(
        key: Any,
        viewType: Any,
        manageChildren: Boolean = true,
        createView: (ViewGroup) -> T,
        block: ComponentContext<T>.() -> Unit
    ) = with(composer) {
        checkIsComposing()

        val finalKey = joinKeyIfNeeded(key, groupKey)

        check(finalKey !in keys) {
            "Duplicated key $finalKey"
        }

        keys.add(finalKey)

        keysStack.push(keys)
        keys = mutableListOf()
        startNode(finalKey)

        log { "emit $finalKey inserting ? $inserting" }
        val node = if (inserting) {
            Component(viewType, manageChildren, createView)
                .also { emitNode(it) }
        } else {
            useNode() as Component<T>
        }

        node._key = finalKey

        val state = ambient(ComponentStateAmbient)

        state.currentComponent = node

        node.inChangeHandler = state.inChangeHandler
        node.outChangeHandler = state.outChangeHandler
        node.isPush = state.isPush
        node.hidden = state.hidden
        state.hidden = false

        val updater = ViewUpdater<T>(composer)
        state.viewUpdater = updater
        ComponentContext(composer, node).block()
        node.updateBlocks = updater.updateBlocks
        if (updater.hasChanges) {
            node.generation++
        }

        state.currentComponent = null
        state.viewUpdater = null

        node.update()

        endNode()
        keys = keysStack.pop()
        if (keysStack.isEmpty()) {
            keys.clear()
        }
    }

    fun key(
        key: Any,
        children: ComponentComposition.() -> Unit
    ) = with(composer) {
        val finalKey = joinKeyIfNeeded(key, groupKey)
        groupKeyStack.push(groupKey)
        groupKey = finalKey

        startGroup(finalKey)
        keysStack.push(keys)
        keys = mutableListOf()
        children()
        keys = keysStack.pop()
        endGroup()

        groupKey = groupKeyStack.pop()
    }

}

private data class JoinedKey(val left: Any, val right: Any) {
    override fun toString(): String = "($left,$right)"
}

private fun joinKeyIfNeeded(key: Any, groupKey: Any?): Any {
    return if (groupKey != null) {
        JoinedKey(key, groupKey)
    } else {
        key
    }
}