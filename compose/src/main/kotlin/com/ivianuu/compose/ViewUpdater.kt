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
import androidx.compose.Composer

class ViewUpdater<T : View>(internal val composer: Composer<*>) {

    var hasChanges = false
        private set

    var updateBlocks: MutableList<T.() -> Unit>? = null

    fun <V> set(value: V, block: T.(V) -> Unit) {
        with(composer) {
            if (inserting || nextSlot() != value) {
                hasChanges = true
                updateValue(value)
            } else skipValue()
            if (updateBlocks == null) updateBlocks = mutableListOf()
            updateBlocks!! += { block(value) }
        }
    }

}