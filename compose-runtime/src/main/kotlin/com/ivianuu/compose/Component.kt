package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

abstract class Component<T : View> {

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    internal var _parent: Component<*>? = null
    val parent: Component<*> get() = _parent ?: error("Not mounted ${javaClass.canonicalName}")

    abstract fun createView(container: ViewGroup): T

    open fun updateView(view: T) {
    }

    open fun destroyView(view: T) {
    }

}

abstract class GroupComponent<T : View> : Component<T>() {

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children

    open fun beginChildren() {
        println("begin children $key")
    }

    open fun addChild(index: Int, child: Component<*>) {
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

abstract class ViewGroupComponent<T : ViewGroup> : GroupComponent<T>() {

    override fun updateView(view: T) {
        super.updateView(view)

        val views = children
            .map { child ->
                view.children()
                    .firstOrNull { it.component == child }
                    ?: child.createView(view).also {
                        it.component = child
                    }
            }

        view.getViewManager().setViews(views, true) // todo check for push

        children
            .map { child ->
                view.children()
                    .first { it.component == child }
            }
            .forEach { (it.component as Component<View>).updateView(it) }
    }

    override fun destroyView(view: T) {
        super.destroyView(view)
        val unprocessedChildren = children.toMutableList()
        view.children().forEach {
            unprocessedChildren.remove(it.component)
            (it.component as Component<View>).destroyView(it)
        }
        check(unprocessedChildren.isEmpty()) { unprocessedChildren }
        view.removeAllViews()
    }

}