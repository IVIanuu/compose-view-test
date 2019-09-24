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
import android.view.ContextThemeWrapper
import android.view.View
import androidx.compose.Ambient
import com.ivianuu.compose.internal.ComponentEnvironmentAmbient

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
    val state = ambient(ComponentEnvironmentAmbient)
    state.inChangeHandler = inHandler
    state.outChangeHandler = outHandler
    children()
}

fun ComponentComposition.TransitionHints(
    isPush: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.isPush = isPush
    children()
}

fun ComponentComposition.Hidden(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.hidden = value
    children()
}

fun ComponentComposition.ShareViews(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.shareViews = value
    children()
}

fun ComponentComposition.ById(
    value: Boolean,
    children: ComponentComposition.() -> Unit
) {
    val state = ambient(ComponentEnvironmentAmbient)
    state.byId = value
    children()
}

@PublishedApi
internal val ContextMapperAmbient = Ambient.of<(Context) -> Context> { { it } }

fun ComponentComposition.ContextMapper(
    mapper: (Context) -> Context,
    children: ComponentComposition.() -> Unit
) {
    val prevMapper = ambient(ContextMapperAmbient)
    val finalMapper: (Context) -> Context = { mapper(prevMapper(it)) }
    ContextMapperAmbient.Provider(value = finalMapper) {
        children()
    }
}

fun ComponentComposition.Theme(
    theme: Int,
    children: ComponentComposition.() -> Unit
) {
    ContextMapper(
        mapper = { ContextThemeWrapper(it, theme) },
        children = children
    )
}

fun <T : View> ComponentComposition.currentComponent(): Component<T> =
    ambient(ComponentEnvironmentAmbient).currentComponent as Component<T>