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
import androidx.compose.Ambient
import com.ivianuu.compose.internal.ViewUpdater

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
    val state = ambient(ComponentStateAmbient)
    state.inChangeHandler = inHandler
    state.outChangeHandler = outHandler
    children()
}

fun ComponentComposition.TransitionHints(
    isPush: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentStateAmbient)
    state.isPush = isPush
    children()
}

fun ComponentComposition.Hidden(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentStateAmbient)
    state.hidden = value
    children()
}

internal val ComponentStateAmbient = Ambient.of<ComponentState>("ComponentState")

internal data class ComponentState(
    var inChangeHandler: ComponentChangeHandler? = null,
    var outChangeHandler: ComponentChangeHandler? = null,
    var isPush: Boolean = true,
    var hidden: Boolean = false,
    var currentComponent: Component<*>? = null,
    var viewUpdater: ViewUpdater<*>? = null
)

fun <T : View> ComponentComposition.currentComponent(): Component<T> =
    ambient(ComponentStateAmbient).currentComponent as Component<T>

@PublishedApi
internal fun <T : View> ComponentComposition.currentViewUpdater(): ViewUpdater<T> =
    ambient(ComponentStateAmbient).viewUpdater as ViewUpdater<T>