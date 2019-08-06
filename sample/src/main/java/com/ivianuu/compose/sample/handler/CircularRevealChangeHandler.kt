package com.ivianuu.compose.sample.handler

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import com.ivianuu.compose.common.AnimatorChangeHandler
import kotlin.math.hypot

open class CircularRevealChangeHandler(private val id: Int? = null) : AnimatorChangeHandler() {

    override fun getAnimator(changeData: ChangeData): Animator {
        val (_, from, to, isPush) = changeData
        return if (from != null && to != null) {
            if (isPush) {
                val view = id?.let { from.findViewById(it) } ?: from
                val (cx, cy) = getCenter(changeData.container, view)
                val radius = hypot(to.width.toFloat(), to.height.toFloat())
                ViewAnimationUtils.createCircularReveal(to, cx, cy, 0f, radius)
            } else {
                val view = id?.let { to.findViewById(it) } ?: to
                val (cx, cy) = getCenter(changeData.container, view)
                val radius = hypot(from.width.toFloat(), from.height.toFloat())
                ViewAnimationUtils.createCircularReveal(from, cx, cy, radius, 0f)
            }
        } else {
            ObjectAnimator()
        }
    }

    override fun copy() = CircularRevealChangeHandler(id)

    private fun getCenter(
        container: ViewGroup,
        view: View
    ): Pair<Int, Int> {
        val fromLocation = IntArray(2)
        view.getLocationInWindow(fromLocation)

        val containerLocation = IntArray(2)
        container.getLocationInWindow(containerLocation)

        val relativeLeft = fromLocation[0] - containerLocation[0]
        val relativeTop = fromLocation[1] - containerLocation[1]

        return view.width / 2 + relativeLeft to view.height / 2 + relativeTop
    }
}
