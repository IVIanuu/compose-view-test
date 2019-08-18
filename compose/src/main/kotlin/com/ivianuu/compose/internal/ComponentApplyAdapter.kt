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

import androidx.compose.ApplyAdapter
import com.ivianuu.compose.Component
import java.util.*

internal class ComponentApplyAdapter(private val root: Component<*>) : ApplyAdapter<Component<*>> {

    private var current: Component<*> = root
    private val currentStack = Stack<Component<*>>()

    private val childrenByParent =
        mutableMapOf<Component<*>, MutableList<Component<*>>>()

    private fun MutableMap<Component<*>, MutableList<Component<*>>>.getOrInit(key: Component<*>): MutableList<Component<*>> {
        return getOrPut(key) {
            mutableListOf<Component<*>>().apply {
                addAll(key.children)
            }
        }
    }

    override fun Component<*>.start(instance: Component<*>) {
        currentStack.push(current)
        current = this
    }

    override fun Component<*>.insertAt(index: Int, instance: Component<*>) {
        val children = childrenByParent.getOrInit(this)
        check(children.none { it.key == instance.key }) {
            "Duplicated key ${instance.key}"
        }
        children.add(index, instance)
    }

    override fun Component<*>.move(from: Int, to: Int, count: Int) {
        val children = childrenByParent.getOrInit(this)
        repeat(count) { children.add(to, children.removeAt(from)) }
    }

    override fun Component<*>.removeAt(index: Int, count: Int) {
        val children = childrenByParent.getOrInit(this)
        (index until index + count).forEach { children.removeAt(it) }
    }

    override fun Component<*>.end(instance: Component<*>, parent: Component<*>) {
        if (this != current && current == instance) {
            instance.updateChildren(childrenByParent[current] ?: current.children.toList())
            childrenByParent.remove(current)

            current = currentStack.pop()

            if (current == root) {
                root.updateChildren(childrenByParent[root] ?: root.children.toList())
                childrenByParent.remove(root)
            }
        }
    }
}