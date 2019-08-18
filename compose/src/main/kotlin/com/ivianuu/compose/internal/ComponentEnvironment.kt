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
import java.util.*

internal val ComponentEnvironmentAmbient = Ambient.of<ComponentEnvironment>("ComponentEnvironment")

internal class ComponentEnvironment(
    var inChangeHandler: ComponentChangeHandler? = null,
    var outChangeHandler: ComponentChangeHandler? = null,
    var isPush: Boolean = true,
    var hidden: Boolean = false,
    var byId: Boolean = false,
    var viewUpdater: ViewUpdater<*>? = null
) {

    var currentComponent: Component<*>? = null
        private set
    private val componentStack = Stack<Component<*>?>()

    private var groupKey: Any? = null
    private val groupKeyStack = Stack<Any?>()

    private var currentChildren = mutableListOf<Any>()
    private val childrenStack = Stack<MutableList<Any>>()

    fun pushComponent(component: Component<*>) {
        check(component.key !in currentChildren) {
            "Duplicated key ${component.key}"
        }

        currentChildren.add(component.key)

        componentStack.push(currentComponent)
        currentComponent = component
        childrenStack.push(currentChildren)
        currentChildren = mutableListOf()
    }

    fun popComponent() {
        currentComponent = componentStack.pop()
        currentChildren = childrenStack.pop()
    }

    fun pushGroupKey(key: Any): Any {
        groupKeyStack.push(groupKey)
        val finalKey = if (groupKey != null) JoinedKey(key, groupKey) else key
        groupKey = finalKey
        return finalKey
    }

    fun popGroupKey() {
        groupKey = groupKeyStack.pop()
    }

    fun joinKey(key: Any): Any = if (groupKey != null) JoinedKey(key, groupKey) else key

    fun reset() {
        inChangeHandler = null
        outChangeHandler = null
        isPush = true
        hidden = false
        byId = false
        currentComponent = null
        componentStack.clear()
        currentChildren = mutableListOf()
        childrenStack.clear()
        viewUpdater = null
        groupKey = null
        groupKeyStack.clear()
    }
}

@PublishedApi
internal fun <T : View> ComponentComposition.currentViewUpdater(): ViewUpdater<T> =
    ambient(ComponentEnvironmentAmbient).viewUpdater as ViewUpdater<T>