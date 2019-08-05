package com.ivianuu.compose.common

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

fun FadeChangeHandler(duration: Long = AnimatorChangeHandler.NO_DURATION): AnimatorChangeHandler {
    return AnimatorChangeHandler(duration) { changeData ->
        val (_, from, to, _) = changeData

        val animator = AnimatorSet()

        if (to != null) {
            val start = if (changeData.addedToView) 0f else to.alpha
            animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, start, 1f))
        }

        if (from != null) {
            animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0f))
        }

        return@AnimatorChangeHandler animator
    }
}