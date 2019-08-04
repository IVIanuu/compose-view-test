package com.ivianuu.compose.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

open class HorizontalViewTransition : AnimatorViewTransition() {

    override fun getAnimator(changeData: ChangeData): Animator {
        val (_, from, to, isPush) = changeData

        val animatorSet = AnimatorSet()

        if (isPush) {
            if (from != null) {
                animatorSet.play(
                    ObjectAnimator.ofFloat(
                        from,
                        View.TRANSLATION_X,
                        -from.width.toFloat()
                    )
                )
            }
            if (to != null) {
                animatorSet.play(
                    ObjectAnimator.ofFloat(
                        to,
                        View.TRANSLATION_X,
                        to.width.toFloat(),
                        0f
                    )
                )
            }
        } else {
            if (from != null) {
                animatorSet.play(
                    ObjectAnimator.ofFloat(
                        from,
                        View.TRANSLATION_X,
                        from.width.toFloat()
                    )
                )
            }
            if (to != null) {
                animatorSet.play(
                    ObjectAnimator.ofFloat(
                        to,
                        View.TRANSLATION_X,
                        -to.width.toFloat(),
                        0f
                    )
                )
            }
        }

        return animatorSet
    }

    override fun copy() = HorizontalViewTransition()

}