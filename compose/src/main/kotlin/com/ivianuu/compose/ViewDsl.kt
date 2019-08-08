package com.ivianuu.compose

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates
import kotlin.reflect.KClass

inline fun <reified T : View> ViewComposition.View(
    key: Any = sourceLocation(),
    noinline block: ViewDsl<T>.() -> Unit
) {
    View(type = T::class, key = key) {
        createView()
        block()
    }
}

fun <T : View> ViewComposition.View(
    type: KClass<T>,
    key: Any,
    block: ViewDsl<T>.() -> Unit
) {
    if (ViewGroup::class.java.isAssignableFrom(type.java)) {
        emit<ViewGroupDslComponent<ViewGroup>>(
            key = key,
            ctor = { ViewGroupDslComponent() },
            update = {
                val dsl = ViewDsl<ViewGroup>().apply(block as ViewDsl<ViewGroup>.() -> Unit)
                createView = dsl.createView
                updateViewBlocks = dsl.updateViewBlocks
                unbindViewBlocks = dsl.unbindViewBlocks
            }
        )
    } else {
        emit<ViewDslComponent<T>>(
            key = key,
            ctor = { ViewDslComponent() },
            update = {
                val dsl = ViewDsl<T>().apply(block)
                createView = dsl.createView
                bindViewBlocks = dsl.updateViewBlocks
                unbindViewBlocks = dsl.unbindViewBlocks
            }
        )
    }
}

class ViewDsl<T : View> {

    internal var createView: (ViewGroup) -> T by Delegates.notNull()
    internal var updateViewBlocks: MutableList<T.() -> Unit>? = null
    internal var unbindViewBlocks: MutableList<T.() -> Unit>? = null

    fun createView(createView: (ViewGroup) -> T) {
        this.createView = createView
    }

    fun updateView(block: T.() -> Unit) {
        if (updateViewBlocks == null) updateViewBlocks = mutableListOf()
        updateViewBlocks!! += block
    }

    fun destroyView(block: T.() -> Unit) {
        if (unbindViewBlocks == null) unbindViewBlocks = mutableListOf()
        unbindViewBlocks!! += block
    }

}

inline fun <reified T : View> ViewDsl<T>.createView() {
    createView(T::class)
}

fun <T : View> ViewDsl<T>.createView(type: KClass<T>) {
    createView {
        type.java.getConstructor(Context::class.java).newInstance(it.context)
    }
}

fun <T : View> ViewDsl<T>.layoutRes(layoutRes: Int) {
    createView {
        LayoutInflater.from(it.context)
            .inflate(layoutRes, it, false) as T
    }
}

fun <T : View> ViewDsl<T>.byId(id: Int) {
    createView { container ->
        container.findViewById<T>(id)
            .also { it.byId = true }
    }
}

private class ViewDslComponent<T : View> : Component<T>() {
    lateinit var createView: (ViewGroup) -> T
    var bindViewBlocks: List<T.() -> Unit>? = null
    var unbindViewBlocks: List<T.() -> Unit>? = null

    override fun createView(container: ViewGroup): T =
        createView.invoke(container)

    override fun bindView(view: T) {
        super.bindView(view)
        bindViewBlocks?.forEach { it(view) }
    }

    override fun unbindView(view: T) {
        super.unbindView(view)
        unbindViewBlocks?.forEach { it(view) }
    }
}

private class ViewGroupDslComponent<T : ViewGroup> : ViewGroupComponent<T>() {
    lateinit var createView: (ViewGroup) -> T
    var updateViewBlocks: List<T.() -> Unit>? = null
    var unbindViewBlocks: List<T.() -> Unit>? = null

    override fun createViewGroup(container: ViewGroup): T =
        createView.invoke(container)

    override fun bindView(view: T) {
        super.bindView(view)
        updateViewBlocks?.forEach { it(view) }
    }

    override fun unbindView(view: T) {
        super.unbindView(view)
        unbindViewBlocks?.forEach { it(view) }
    }
}