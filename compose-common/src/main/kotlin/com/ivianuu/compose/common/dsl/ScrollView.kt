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

package com.ivianuu.compose.common.dsl

import android.widget.HorizontalScrollView
import android.widget.ScrollView
import com.ivianuu.compose.ComponentBuilder
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.View

fun ComponentComposition.ScrollView(block: (ComponentBuilder<ScrollView>.() -> Unit)? = null) {
    View(block = block)
}

fun ComponentComposition.HorizontalScrollView(block: (ComponentBuilder<HorizontalScrollView>.() -> Unit)? = null) {
    View(block = block)
}