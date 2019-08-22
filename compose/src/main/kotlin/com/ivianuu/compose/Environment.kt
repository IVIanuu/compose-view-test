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
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient

inline fun ComponentComposition.ChangeHandlers(
    handler: ComponentChangeHandler?,
    children: ComponentComposition.() -> Unit
) {
    ChangeHandlers(
        inHandler = handler,
        outHandler = handler,
        children = children
    )
}

inline fun ComponentComposition.ChangeHandlers(
    inHandler: ComponentChangeHandler? = null,
    outHandler: ComponentChangeHandler? = null,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.inChangeHandler = inHandler
    state.outChangeHandler = outHandler
    children()
}

inline fun ComponentComposition.TransitionHints(
    isPush: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.isPush = isPush
    children()
}

inline fun ComponentComposition.Hidden(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.hidden = value
    children()
}

inline fun ComponentComposition.ShareViews(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.shareViews = value
    children()
}

inline fun ComponentComposition.ById(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.byId = value
    children()
}

fun <T : View> ComponentComposition.currentComponent(): Component<T> =
    ambient(ComponentEnvironmentAmbient).currentComponent as Component<T>