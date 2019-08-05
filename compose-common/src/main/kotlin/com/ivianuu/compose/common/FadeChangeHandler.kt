package com.ivianuu.compose.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import com.ivianuu.compose.ViewChangeHandler

class FadeChangeHandler : AnimatorChangeHandler() {

    private var addedToView = false

    override fun execute(changeData: ChangeData) {
        addedToView = changeData.to != null && changeData.to!!.parent == null
        super.execute(changeData)
    }

    override fun getAnimator(changeData: ChangeData): Animator {
        val (_, from, to, isPush) = changeData

        val animator = AnimatorSet()

        if (to != null) {
            val start = if (addedToView) 0f else to.alpha
            animator.play(ObjectAnimator.ofFloat(to, View.ALPHA, start, 1f))
        }

        if (from != null) {
            animator.play(ObjectAnimator.ofFloat(from, View.ALPHA, 0f))
        }

        return animator
    }

    override fun copy(): ViewChangeHandler = FadeChangeHandler()

}