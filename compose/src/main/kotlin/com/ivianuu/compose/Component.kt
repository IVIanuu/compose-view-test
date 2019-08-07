package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

abstract class Component<T : View> {

    internal var inChangeHandler: ViewChangeHandler? = null
    internal var outChangeHandler: ViewChangeHandler? = null
    internal var wasPush: Boolean = true

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted ${javaClass.canonicalName}")

    private var _parent: Component<*>? = null
    val parent: Component<*> get() = _parent ?: error("Not mounted ${javaClass.canonicalName}")

    private val _children = mutableListOf<Component<*>>()
    val children: List<Component<*>> get() = _children

    val boundViews: Set<T> get() = _boundViews
    private val _boundViews = mutableSetOf<T>()

    open fun update() {
        println("update $key bound views ${_boundViews.size}")
        _boundViews.forEach { bindView(it) }
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
        return createView(container)
    }

    open fun bindView(view: T) {
        println("bind view $key $view")
        _boundViews.add(view)
        view.component = this
    }

    open fun unbindView(view: T) {
        println("unbind view $key $view")
        _boundViews.remove(view)
        view.component = null
    }

}

abstract class ViewGroupComponent<T : ViewGroup> : Component<T>() {

    override fun performCreateView(container: ViewGroup): T {
        val view = super.performCreateView(container)
        val childViews = children.map { child ->
            child.performCreateView(view)
                .also { (child as Component<View>).bindView(it) }
        }
        view.getViewManager().rebind(childViews)
        return view
    }

    override fun bindView(view: T) {
        super.bindView(view)

        val childViews = children
            .map { child ->
                val childView = view.children()
                    .firstOrNull { it.component == child }
                    ?: child.performCreateView(view)
                (child as Component<View>).bindView(childView)
                childView
            }

        view.getViewManager()
            .setViews(childViews, childViews.lastOrNull()?.component?.wasPush ?: true)
    }

    override fun unbindView(view: T) {
        super.unbindView(view)
        children
            .forEach { child ->
                val childView = view.children()
                    .first { it.component == child }
                (child as Component<View>).unbindView(childView)
            }
    }

}