package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

abstract class Component<T : View> {

    internal var inChangeHandler: ComponentChangeHandler? = null
    internal var outChangeHandler: ComponentChangeHandler? = null
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
        log { "update $key bound views ${_boundViews.size}" }
        _boundViews.forEach { bindView(it) }
    }

    open fun updateChildren(newChildren: List<Component<*>>) {
        log { "update children $key ${newChildren.map { it.key }}" }

        _children
            .filter { it !in newChildren }
            .forEach { it._parent = null }

        newChildren
            .filter { it !in _children }
            .forEach { it._parent = this }

        _children.clear()
        _children += newChildren

        update()
    }

    abstract fun createView(container: ViewGroup): T

    open fun bindView(view: T) {
        log { "bind view $key $view" }
        _boundViews += view
        view.component = this
    }

    open fun unbindView(view: T) {
        log { "unbind view $key $view" }
        _boundViews -= view
        view.component = null
    }

}

abstract class ViewGroupComponent<T : ViewGroup> : Component<T>() {

    override fun createView(container: ViewGroup): T {
        val view = createViewGroup(container)
        view.getViewManager().init(children)
        return view
    }

    protected abstract fun createViewGroup(container: ViewGroup): T

    override fun bindView(view: T) {
        super.bindView(view)
        view.getViewManager().update(children, children.lastOrNull()?.wasPush ?: true)
    }

    override fun unbindView(view: T) {
        view.getViewManager().clear()
        super.unbindView(view)
    }

}