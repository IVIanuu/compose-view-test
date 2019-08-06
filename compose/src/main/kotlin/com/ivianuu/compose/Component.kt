package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

abstract class Component<T : View> {

    internal var inChangeHandler: ViewChangeHandler? = null
    internal var outChangeHandler: ViewChangeHandler? = null
    internal var wasPush: Boolean = true

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    internal var _parent: Component<*>? = null
    val parent: Component<*> get() = _parent ?: error("Not mounted ${javaClass.canonicalName}")

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children

    val views = mutableListOf<T>()

    open fun update() {
        views.forEach { updateView(it) }
    }

    open fun start() {
        println("start children $key current children ${children.map { it.key }}")
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

    open fun end() {
        println("end children $key children ${children.map { it.key }}")
    }

    protected abstract fun createView(container: ViewGroup): T

    open fun performCreateView(container: ViewGroup): T {
        println("create view $key $container")
        val view = createView(container)
        view.component = this
        views.add(view)
        updateView(view)
        return view
    }

    open fun updateView(view: T) {
        println("update view $key $view")
    }

    open fun destroyView(view: T) {
        println("destroy view $key $view")
        view.component = null
    }

}

abstract class ViewGroupComponent<T : ViewGroup> : Component<T>() {

    private var firstUpdate = true

    override fun performCreateView(container: ViewGroup): T {
        firstUpdate = true
        val view = super.performCreateView(container)
        val childViews = children.map { child -> child.performCreateView(view) }
        view.getViewManager().rebind(childViews)
        return view
    }

    override fun end() {
        super.end()

        views.forEach { view ->
            val childViews = children
                .map { child ->
                    view.children()
                        .firstOrNull { it.component == child }
                        ?.also { (child as Component<View>).updateView(it) }
                        ?: child.performCreateView(view)
                }

            view.getViewManager()
                .setViews(childViews, childViews.lastOrNull()?.component?.wasPush ?: true)
        }
    }

    override fun updateView(view: T) {
        super.updateView(view)

        if (!firstUpdate) {
            val childViews = children
                .map { child ->
                    view.children()
                        .firstOrNull { it.component == child }
                        ?.also { (child as Component<View>).updateView(it) }
                        ?: child.performCreateView(view)
                }

            view.getViewManager()
                .setViews(childViews, childViews.lastOrNull()?.component?.wasPush ?: true)
        } else {
            firstUpdate = false
        }
    }

    override fun destroyView(view: T) {
        super.destroyView(view)

        val unprocessedChildren = children.toMutableList()
        view.children().forEach { childView ->
            val component = childView.component as Component<View>
            unprocessedChildren.remove(component)
            component.destroyView(childView)
            if (!childView.byId) {
                view.removeView(childView)
            }
        }
        check(unprocessedChildren.isEmpty()) { unprocessedChildren }
    }

}