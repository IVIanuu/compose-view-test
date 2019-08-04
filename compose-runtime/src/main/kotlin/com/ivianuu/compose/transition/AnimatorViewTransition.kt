package com.ivianuu.compose.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup

abstract class AnimatorViewTransition : ViewTransition() {

    private var animator: Animator? = null
    private var onReadyOrAbortedListener: OnReadyOrAbortedListener? = null
    private var changeData: ChangeData? = null

    private var canceled = false
    private var completed = false

    override fun execute(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onComplete: () -> Unit
    ) {
        changeData = ChangeData(
            container,
            from,
            to,
            isPush,
            onComplete
        )

        if (to != null && to.parent == null) {
            container.addView(to)
        }

        if (to != null
            && to.width <= 0
            && to.height <= 0
        ) {
            onReadyOrAbortedListener =
                OnReadyOrAbortedListener(to) {
                    performAnimation(changeData!!)
                }
        } else {
            performAnimation(changeData!!)
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

}