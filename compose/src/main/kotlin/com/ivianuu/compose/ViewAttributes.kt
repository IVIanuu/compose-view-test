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
import androidx.compose.Applier
import androidx.compose.ApplyAdapter
import androidx.compose.Component
import androidx.compose.Composer
import androidx.compose.CompositionContext
import androidx.compose.Recomposer
import androidx.compose.SlotTable

private val viewAttributeContext = tagKey("viewAttributeContext")

internal fun <T : View> T.getViewUpdater(): ViewUpdater<T> {
    var context = getTag(viewAttributeContext) as? CompositionContext
    if (context == null) {
        context = CompositionContext.prepare(
            root,
            null
        ) { ViewAttributeComposer(this) }

        setTag(viewAttributeContext, context)
    }

    return ViewUpdater(context.composer as Composer<Any>, this)

}

internal inline fun <T : View> T.update(block: ViewUpdater<T>.() -> Unit) {
    val updater = getViewUpdater()

    updater.block()
}

class ViewUpdater<T>(
    val composer: Composer<Any>,
    val node: T
) {

    fun start() {
        composer.startRoot()
    }

    inline fun <V> set(
        value: V,
        crossinline block: T.(value: V) -> Unit
    ) {
        with(composer) {
            if (inserting || nextSlot() != value) {
                updateValue(value)
                node.block(value)
            } else skipValue()
        }
    }

    fun stop() {
        composer.endRoot()
        composer.applyChanges()
    }

}

private class ViewAttributeComposer(
    recomposer: Recomposer
) : Composer<Any>(
    SlotTable(),
    Applier(Unit, noOpApplyAdapter),
    recomposer
)

private val noOpApplyAdapter = object : ApplyAdapter<Any> {
    override fun Any.start(instance: Any) {
    }

    override fun Any.insertAt(index: Int, instance: Any) {
    }

    override fun Any.move(from: Int, to: Int, count: Int) {
    }

    override fun Any.removeAt(index: Int, count: Int) {
    }

    override fun Any.end(instance: Any, parent: Any) {
    }
}

private val root = object : Component() {
    override fun compose() {
    }
}