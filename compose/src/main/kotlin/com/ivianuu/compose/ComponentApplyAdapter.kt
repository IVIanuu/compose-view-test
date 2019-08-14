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

import androidx.compose.ApplyAdapter
import java.util.*

class ComponentApplyAdapter(private val root: Component<*>) : ApplyAdapter<Component<*>> {

    private var current: Component<*> = root
    private val currentStack = Stack<Component<*>>()

    private val childrenByParent =
        mutableMapOf<Component<*>, MutableList<Component<*>>>()

    private fun MutableMap<Component<*>, MutableList<Component<*>>>.getOrPut(key: Component<*>): MutableList<Component<*>> {
        return getOrPut(key) {
            mutableListOf<Component<*>>().apply {
                addAll(key.children)
            }
        }
    }

    override fun Component<*>.start(instance: Component<*>) {
        log { "composition $key start" }
        currentStack.push(current)
        current = this
    }

    override fun Component<*>.insertAt(index: Int, instance: Component<*>) {
        log { "composition $key insert at $index ${instance.key}" }
        childrenByParent.getOrPut(this).add(index, instance)
    }

    override fun Component<*>.move(from: Int, to: Int, count: Int) {
        val children = childrenByParent.getOrPut(this)
        repeat(count) {
            log { "composition $key move from $from to $to" }
            children.add(to, children.removeAt(from))
        }
    }

    override fun Component<*>.removeAt(index: Int, count: Int) {
        val children = childrenByParent.getOrPut(this)
        (index until index + count).forEach {
            log { "composition $key remove at $it" }
            children.removeAt(it)
        }
    }

    override fun Component<*>.end(instance: Component<*>, parent: Component<*>) {
        if (this != current && current == instance) {
            log { "composition ${current.key} end" }
            instance.updateChildren(childrenByParent.getOrPut(current))
            childrenByParent.remove(current)

            current = currentStack.pop()

            if (current == root) {
                root.updateChildren(childrenByParent.getOrPut(root))
                childrenByParent.remove(root)
            }
        }
    }
}