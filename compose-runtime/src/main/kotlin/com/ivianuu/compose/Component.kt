package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

abstract class Component<T : View> {

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted")

    internal var _parent: Component<out View>? = null
    val parent: Component<out View> get() = _parent ?: error("Not mounted")

    abstract fun createView(container: ViewGroup): T

    open fun updateView(view: T) {
    }

    open fun destroyView(view: T) {
    }

}

abstract class GroupComponent<T : ViewGroup> : Component<T>() {

    private val _children = mutableListOf<Component<out View>>()
    val children: List<Component<out View>> get() = _children

    open fun beginChildren() {
        println("begin children $key")
    }

    open fun addChild(index: Int, child: Component<out View>) {
        println("insert child $key index $index child ${child.key}")
        _children.add(index, child)
        child._parent = this
    }

    open fun moveChild(from: Int, to: Int) {
        println("move child $key from $from to $to")
        _children.add(to, _children.removeAt(from))
    }

    open fun removeChild(index: Int) {
        println("remove child $key index $index")
        _children.removeAt(index).also {
            it._parent = null
        }
    }

    open fun endChildren() {
        println("end children $key")
    }

}

/*
     val current = current
        val container = when (current) {
            is ViewGroup -> current
            is Compose.Root -> current.container
            else -> error("Unsupported node type ${current.javaClass.simpleName}")
        }

        val viewManager = container.getViewManager()

        val oldViews = viewManager.views
        val newViews = oldViews.toMutableList()

        var insertCount = 0
        var removeCount = 0

        ops.forEach { op ->
            when (op) {
                is Op.Insert -> {
                    newViews.add(op.index, op.instance as View)
                    insertCount++
                }
                is Op.Move -> {
                    if (op.from > op.to) {
                        var currentFrom = op.from
                        var currentTo = op.to
                        repeat(op.count) {
                            Collections.swap(newViews, currentFrom, currentTo)
                            currentFrom++
                            currentTo++
                        }
                    } else {
                        repeat(op.count) {
                            Collections.swap(newViews, op.from, op.to - 1)
                        }
                    }
                }
                is Op.Remove -> {
                    for (i in op.index + op.count - 1 downTo op.index) {
                        newViews.removeAt(i)
                        removeCount++
                    }
                }
            }
        }

        viewManager.setViews(newViews, insertCount >= removeCount)
 */