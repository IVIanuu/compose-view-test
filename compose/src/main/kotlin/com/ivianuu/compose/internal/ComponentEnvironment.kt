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

package com.ivianuu.compose.internal

import android.view.View
import androidx.compose.Ambient
import com.ivianuu.compose.Component
import com.ivianuu.compose.ComponentChangeHandler
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ambient

internal val ComponentEnvironmentAmbient = Ambient.of<ComponentEnvironment>("ComponentEnvironment")

internal class ComponentEnvironment(
    var inChangeHandler: ComponentChangeHandler? = null,
    var outChangeHandler: ComponentChangeHandler? = null,
    var isPush: Boolean = true,
    var hidden: Boolean = false,
    var byId: Boolean = false,
    var viewUpdater: ViewUpdater<*>? = null,
    var currentComponent: Component<*>? = null,
    var groupKey: Any? = null
) {

    fun joinKey(key: Any): Any = if (groupKey != null) JoinedKey(key, groupKey) else key

    fun reset() {
        inChangeHandler = null
        outChangeHandler = null
        isPush = true
        hidden = false
        byId = false
        currentComponent = null
        viewUpdater = null
        groupKey = null
    }
}

@PublishedApi
internal fun <T : View> ComponentComposition.currentViewUpdater(): ViewUpdater<T> =
    ambient(ComponentEnvironmentAmbient).viewUpdater as ViewUpdater<T>