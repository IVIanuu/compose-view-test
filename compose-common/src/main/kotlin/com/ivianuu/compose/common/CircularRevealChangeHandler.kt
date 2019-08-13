/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose.common

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import kotlin.math.hypot

open class CircularRevealChangeHandler(private val id: Int? = null) : AnimatorChangeHandler() {

    override fun getAnimator(changeData: ChangeData): Animator {
        val (_, from, to, isPush) = changeData
        return if (from != null && to != null) {
            if (isPush) {
                val view = id?.let { from.findViewById<View>(it) } ?: from
                val (cx, cy) = getCenter(changeData.container, view)
                val radius = hypot(to.width.toFloat(), to.height.toFloat())
                ViewAnimationUtils.createCircularReveal(to, cx, cy, 0f, radius)
            } else {
                val view = id?.let { to.findViewById<View>(it) } ?: to
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
