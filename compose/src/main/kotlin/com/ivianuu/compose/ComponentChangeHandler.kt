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
import androidx.compose.Ambient

abstract class ComponentChangeHandler {

    internal var hasBeenUsed = false

    abstract fun execute(changeData: ChangeData)

    abstract fun cancel()

    abstract fun copy(): ComponentChangeHandler

    interface Callback {
        fun addToView()
        fun removeFromView()
        fun onComplete()
    }

    data class ChangeData(
        val container: ViewGroup,
        val from: View?,
        val to: View?,
        val isPush: Boolean,
        val callback: Callback
    ) {
        val addedToView = to != null && to.parent == null
    }

}

class DefaultChangeHandler : ComponentChangeHandler() {

    override fun execute(changeData: ChangeData) {
        with(changeData) {
            callback.removeFromView()
            callback.addToView()
            callback.onComplete()
        }
    }

    override fun copy(): ComponentChangeHandler =
        DefaultChangeHandler()

    override fun cancel() {
    }
}

val InChangeHandlerAmbient = Ambient.of<ComponentChangeHandler?>("InTransition")
val OutChangeHandlerAmbient = Ambient.of<ComponentChangeHandler?>("OutTransition")
val TransitionHintsAmbient = Ambient.of("TransitionHints") { true }

fun ComponentComposition.ChangeHandlers(
    handler: ComponentChangeHandler?,
    children: ComponentComposition.() -> Unit
) {
    ChangeHandlers(
        inHandler = handler,
        outHandler = handler,
        children = children
    )
}

fun ComponentComposition.ChangeHandlers(
    inHandler: ComponentChangeHandler? = null,
    outHandler: ComponentChangeHandler? = null,
    children: ComponentComposition.() -> Unit
) {
    InChangeHandlerAmbient.Provider(inHandler) {
        OutChangeHandlerAmbient.Provider(outHandler) {
            children()
        }
    }
}