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

package com.ivianuu.compose.sample

import com.google.android.material.appbar.MaterialToolbar
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.ambient
import com.ivianuu.compose.common.NavigatorAmbient
import com.ivianuu.compose.common.RouteAmbient
import com.ivianuu.compose.set

fun ComponentComposition.AppBar(title: String) {
    ViewByLayoutRes<MaterialToolbar>(layoutRes = R.layout.app_bar) {
        set(title) { this.title = it }

        val route = ambient(RouteAmbient) ?: return@ViewByLayoutRes
        val navigator = ambient(NavigatorAmbient)

        set(navigator.backStack.indexOf(route) > 0) {
            if (it) {
                this.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
                setNavigationOnClickListener { navigator.pop() }
            } else {
                navigationIcon = null
                setNavigationOnClickListener(null)
            }
        }
    }
}