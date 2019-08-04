package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup

abstract class Component<T : View> {

    internal var _key: Any? = null
    val key: Any get() = _key ?: error("Not mounted")

    internal var _parent: Component<out View>? = null
    val parent: Component<out View> get() = _parent ?: error("Not mounted")

    private val _children = mutableListOf<Component<out View>>()
    val children: List<Component<out View>> get() = _children

    open fun begin() {
    }

    open fun insertChild(index: Int, child: Component<out View>) {
        _children.add(index, child)
        child._parent = this
    }

    open fun moveChild(from: Int, to: Int) {
        _children.add(to, _children.removeAt(from))
    }

    open fun removeChild(index: Int) {
        _children.removeAt(index).also {
            it._parent = null
        }
    }

    open fun end() {
    }

    abstract fun createView(container: ViewGroup): T

    open fun updateView(view: T) {
    }

    open fun destroyView(view: T) {
    }

}