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

interface ChildViewController<T : View> {

    fun initChildViews(
        component: Component<T>,
        view: T
    )

    fun updateChildViews(
        component: Component<T>,
        view: T
    )

    fun clearChildViews(
        component: Component<T>,
        view: T
    )

}

class DefaultChildViewController<T : View> : ChildViewController<T> {

    override fun initChildViews(component: Component<T>, view: T) {
        if (view !is ViewGroup) return
        view.getViewManager().init(component.visibleChildren)
    }

    override fun updateChildViews(component: Component<T>, view: T) {
        if (view !is ViewGroup) return
        view.getViewManager().update(
            component.visibleChildren,
            component.visibleChildren.lastOrNull()?.isPush ?: true
        )
    }

    override fun clearChildViews(component: Component<T>, view: T) {
        if (view !is ViewGroup) return
        view.getViewManager().clear()
    }

}