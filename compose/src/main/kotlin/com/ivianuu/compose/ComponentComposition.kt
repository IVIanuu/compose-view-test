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

import androidx.compose.EffectsDsl

@Suppress("UNCHECKED_CAST")
@EffectsDsl
class ComponentComposition(val composer: ComponentComposer) {

    fun <T : Component<*>> emit(
        key: Any,
        ctor: () -> T,
        update: (T.() -> Unit)? = null
    ) = with(composer) {
        startNode(key)
        log { "emit $key inserting ? $inserting" }
        val node = if (inserting) {
            ctor().also { emitNode(it) }
        } else {
            useNode() as T
        }

        node._key = key

        // todo remove
        node.inChangeHandler = ambient(InChangeHandlerAmbient)
        node.outChangeHandler = ambient(OutChangeHandlerAmbient)
        node.wasPush = ambient(TransitionHintsAmbient)

        update?.let { node.it() }
        node.update()

        endNode()
    }

    fun group(
        key: Any,
        children: ComponentComposition.() -> Unit
    ) = with(composer) {
        startGroup(key)
        children()
        endGroup()
    }

    inline fun group(noinline children: ComponentComposition.() -> Unit) =
        group(sourceLocation(), children)

}