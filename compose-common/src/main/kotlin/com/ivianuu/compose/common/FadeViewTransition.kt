package com.ivianuu.compose.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.ViewTransition

class FadeViewTransition : AnimatorViewTransition() {

    private var addedToView = false

    override fun execute(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onComplete: () -> Unit
    ) {
        addedToView = to != null && to.parent == null
        super.execute(container, from, to, isPush, onComplete)
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

    override fun copy(): ViewTransition = FadeViewTransition()

}