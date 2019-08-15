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

import androidx.compose.Ambient

// todo all of those shouldn't exist but we do not have better solution yet

internal val InChangeHandlerAmbient = Ambient.of<ComponentChangeHandler?>("InTransition")
internal val OutChangeHandlerAmbient = Ambient.of<ComponentChangeHandler?>("OutTransition")
internal val TransitionHintsAmbient = Ambient.of("TransitionHints") { true }
internal val HiddenAmbient = Ambient.of("Hidden") { Hidden(false) }

internal data class Hidden(var value: Boolean)

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

fun ComponentComposition.TransitionHints(
    isPush: Boolean,
    children: ComponentComposition.() -> Unit
) {
    TransitionHintsAmbient.Provider(isPush) {
        children()
    }
}

fun ComponentComposition.Hidden(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val hidden = ambient(HiddenAmbient)
    hidden.value = value
    HiddenAmbient.Provider(hidden) {
        children()
    }
}