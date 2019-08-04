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
        println("update view $key $view")
    }

    open fun destroyView(view: T) {
        println("destroy view $key $view")
    }

}

abstract class GroupComponent<T : View> : Component<T>() {

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children

    open fun beginChildren() {
        println("begin children $key current children ${children.map { it.key }}")
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
        println("end children $key children ${children.map { it.key }}")
    }

}

abstract class ViewGroupComponent<T : ViewGroup> : GroupComponent<T>() {

    private val views = mutableListOf<ViewGroup>()

    override fun endChildren() {
        super.endChildren()
        views.forEach { view ->
            val childViews = children
                .map { child ->
                    view.children()
                        .firstOrNull { it.component == child }
                        ?: child.createView(view).also {
                            it.component = child
                        }
                }

            view.getViewManager().setViews(childViews, true) // todo check for push
        }
    }

    final override fun createView(container: ViewGroup): T {
        val view = createViewGroup(container)
        views.add(view)

        val childViews = children.map { child ->
            child.createView(view)
                .also { it.component = child }
        }
        view.getViewManager().rebind(childViews)

        return view
    }

    protected abstract fun createViewGroup(container: ViewGroup): T

    override fun updateView(view: T) {
        super.updateView(view)

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
        view.children().forEach { childView ->
            val component = childView.component as Component<View>
            unprocessedChildren.remove(component)
            component.destroyView(childView)
            childView.component = null
            if (!childView.byId) {
                view.removeView(childView)
            }
        }
        check(unprocessedChildren.isEmpty()) { unprocessedChildren }
        views.remove(view)
    }

}