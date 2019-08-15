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
import androidx.compose.Composer

@PublishedApi
internal class ViewUpdater<T : View>(private val composer: Composer<*>) {

    var hasChanges = false
        private set

    private var blocks: MutableList<Entry>? = null

    fun <V> set(value: V, block: T.(V) -> Unit) {
        with(composer) {
            if (inserting || nextSlot() != value) {
                hasChanges = true
                updateValue(value)
            } else skipValue()
            if (blocks == null) blocks = mutableListOf()
            blocks!! += Entry(block = { block(value) }, type = Type.Value)
        }
    }

    fun init(block: T.() -> Unit) {
        if (blocks == null) blocks = mutableListOf()
        blocks!! += Entry(block = block, type = Type.Init)
    }

    fun update(block: T.() -> Unit) {
        if (blocks == null) blocks = mutableListOf()
        blocks!! += Entry(block = block, type = Type.Update)
    }

    fun getBlocks(vararg types: Type): List<T.() -> Unit> {
        return blocks
            ?.filter { it.type in types }
            ?.map { it.block }
            ?: emptyList()
    }

    enum class Type {
        Value, Init, Update
    }

    private inner class Entry(
        val block: T.() -> Unit,
        val type: Type
    )
}