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
        val view = createView(container)
        view.component = this
        bindView(view)
        return view
    }

    open fun bindView(view: T) {
        println("bind view $key $view")
        _boundViews.add(view)
    }

    open fun unbindView(view: T) {
        println("unbind view $key $view")
        _boundViews.remove(view)
    }

}

abstract class ViewGroupComponent<T : ViewGroup> : Component<T>() {

    private var isCreatingView = false

    override fun performCreateView(container: ViewGroup): T {
        isCreatingView = true
        val view = super.performCreateView(container)
        isCreatingView = false
        return view
    }

    override fun bindView(view: T) {
        super.bindView(view)

        if (isCreatingView) {
            val childViews = children.map { child -> child.performCreateView(view) }
            view.getViewManager().rebind(childViews)
        } else {
            val childViews = children
                .map { child ->
                    view.children()
                        .firstOrNull { it.component == child }
                        ?.also { (child as Component<View>).bindView(it) }
                        ?: child.performCreateView(view)
                }

            view.getViewManager()
                .setViews(childViews, childViews.lastOrNull()?.component?.wasPush ?: true)
        }
    }

    override fun unbindView(view: T) {
        super.unbindView(view)
        children
            .forEach { child ->
                val childView = view.children()
                    .firstOrNull { it.component == child }
                    ?: return@forEach
                (child as Component<View>).unbindView(childView)
            }
    }

}