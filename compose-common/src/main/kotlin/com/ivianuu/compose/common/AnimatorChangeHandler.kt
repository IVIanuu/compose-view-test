package com.ivianuu.compose.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.ivianuu.compose.ComponentChangeHandler

fun AnimatorChangeHandler(
    duration: Long = AnimatorChangeHandler.NO_DURATION,
    getAnimator: (ComponentChangeHandler.ChangeData) -> Animator
): AnimatorChangeHandler = SimpleAnimatorChangeHandler(duration, getAnimator)

private class SimpleAnimatorChangeHandler(
    duration: Long = NO_DURATION,
    val getAnimator: (ChangeData) -> Animator
) : AnimatorChangeHandler(duration) {
    override fun getAnimator(changeData: ChangeData): Animator =
        getAnimator.invoke(changeData)

    override fun copy() = SimpleAnimatorChangeHandler(duration, getAnimator)
}

abstract class AnimatorChangeHandler(val duration: Long = NO_DURATION) : ComponentChangeHandler() {

    private var animator: Animator? = null
    private var onReadyOrAbortedListener: OnReadyOrAbortedListener? = null
    private var changeData: ChangeData? = null

    private var canceled = false
    private var completed = false

    override fun execute(changeData: ChangeData) {
        this.changeData = changeData
        with(changeData) {
            if (isPush || from == null) {
                if (to != null) container.addView(to)
            } else if (to != null && to!!.parent == null) {
                container.addView(to, container.indexOfChild(from))
            }

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
                this.duration = this@AnimatorChangeHandler.duration
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

        val (container, from, _, _, onComplete) = changeData!!

        if (from != null) {
            container.removeView(from)
        }

        onComplete()

        animator = null
        onReadyOrAbortedListener = null
        changeData = null
    }

    companion object {
        const val NO_DURATION = -1L
    }

}