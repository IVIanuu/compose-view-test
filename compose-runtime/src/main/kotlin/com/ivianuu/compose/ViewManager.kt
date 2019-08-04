package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.transition.ViewTransition
import com.ivianuu.compose.util.tagKey

private val viewManagerKey = tagKey("viewManager")

internal fun ViewGroup.getViewManager(): ViewManager {
    var viewManager = getTag(viewManagerKey) as? ViewManager
    if (viewManager == null) {
        viewManager = ViewManager(this)
        setTag(viewManagerKey, viewManager)
    }

    return viewManager

}

internal class ViewManager(val container: ViewGroup) {

    val views = mutableListOf<View>()
    private val runningTransitions = mutableMapOf<View, ViewTransition>()

    fun addView(index: Int, view: View) {
        if (view.parent == null) {
            container.addView(view, index)
        }
    }

    fun moveViews(from: Int, to: Int, count: Int) {
        if (from > to) {
            var currentFrom = from
            var currentTo = to
            repeat(count) {
                val view = container.getChildAt(currentFrom)
                container.removeViewAt(currentFrom)
                container.addView(view, currentTo)
                currentFrom++
                currentTo++
            }
        } else {
            repeat(count) {
                val view = container.getChildAt(from)
                container.removeViewAt(from)
                container.addView(view, to - 1)
            }
        }
    }

    fun removeViews(index: Int, count: Int) {
        container.removeViews(index, count)
    }

}