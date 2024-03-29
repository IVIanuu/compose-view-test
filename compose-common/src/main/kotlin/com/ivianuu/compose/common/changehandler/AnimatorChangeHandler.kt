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

package com.ivianuu.compose.common.changehandler

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.ivianuu.compose.ComponentChangeHandler
import com.ivianuu.compose.common.OnReadyOrAbortedListener
import kotlin.time.Duration
import kotlin.time.milliseconds

fun AnimatorChangeHandler(
    duration: Duration = AnimatorChangeHandler.NO_DURATION,
    getAnimator: (ComponentChangeHandler.ChangeData) -> Animator
): AnimatorChangeHandler =
    SimpleAnimatorChangeHandler(duration, getAnimator)

private class SimpleAnimatorChangeHandler(
    duration: Duration = NO_DURATION,
    val getAnimator: (ChangeData) -> Animator
) : AnimatorChangeHandler(duration) {
    override fun getAnimator(changeData: ChangeData): Animator =
        getAnimator.invoke(changeData)

    override fun copy() =
        SimpleAnimatorChangeHandler(duration, getAnimator)
}

abstract class AnimatorChangeHandler(val duration: Duration = NO_DURATION) :
    ComponentChangeHandler() {

    private var animator: Animator? = null
    private var onReadyOrAbortedListener: OnReadyOrAbortedListener? = null
    private var changeData: ChangeData? = null

    private var canceled = false
    private var completed = false

    override fun execute(changeData: ChangeData) {
        this.changeData = changeData
        with(changeData) {
            callback.addToView()

            if (to != null
                && to!!.width <= 0
                && to!!.height <= 0
            ) {
                onReadyOrAbortedListener =
                    OnReadyOrAbortedListener(to!!) {
                        performAnimation(changeData)
                    }
            } else {
                performAnimation(changeData)
            }
        }
    }

    override fun cancel() {
        canceled = true
        when {
            animator != null -> animator?.cancel()
            onReadyOrAbortedListener != null -> onReadyOrAbortedListener?.onReadyOrAborted()
            changeData != null -> complete()
        }
    }

    protected abstract fun getAnimator(changeData: ChangeData): Animator

    private fun performAnimation(changeData: ChangeData) {
        if (canceled) {
            complete()
            return
        }

        animator = getAnimator(changeData).apply {
            if (this@AnimatorChangeHandler.duration != NO_DURATION) {
                this.duration = this@AnimatorChangeHandler.duration.toLongMilliseconds()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    complete()
                }
            })
        }

        animator?.start()
    }

    private fun complete() {
        if (completed) return
        completed = true

        val callback = changeData!!.callback

        callback.removeFromView()
        callback.onComplete()

        animator = null
        onReadyOrAbortedListener = null
        changeData = null
    }

    companion object {
        val NO_DURATION = (-1L).milliseconds
    }

}