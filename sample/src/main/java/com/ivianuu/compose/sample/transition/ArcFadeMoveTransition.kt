package com.ivianuu.compose.sample.transition

import android.transition.ArcMotion
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import com.ivianuu.compose.common.SharedElementViewTransition
import com.ivianuu.compose.common.findNamedView

class ArcFadeMoveTransition(private val sharedElementNames: List<String>) :
    SharedElementViewTransition() {

    override fun getExitTransition(changeData: ChangeData): Transition? {
        return Fade(Fade.OUT)
    }

    override fun getSharedElementTransition(changeData: ChangeData): Transition? {
        val transition =
            TransitionSet()
                .addTransition(ChangeBounds()).addTransition(ChangeClipBounds())
                .addTransition(ChangeTransform())

        transition.pathMotion = ArcMotion()

        // The framework doesn't totally fade out the "from" shared element, so we'll hide it manually once it's safe.
        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
                if (changeData.from != null) {
                    sharedElementNames
                        .mapNotNull { changeData.from!!.findNamedView(it) }
                        .forEach { it.visibility = View.INVISIBLE }
                }
            }

            override fun onTransitionEnd(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }
        })

        return transition
    }

    override fun getEnterTransition(changeData: ChangeData): Transition? {
        return Fade(Fade.IN)
    }

    override fun configureSharedElements(changeData: ChangeData) {
        sharedElementNames.forEach { addSharedElement(it) }
    }

    override fun allowTransitionOverlap(isPush: Boolean): Boolean = false

    override fun copy() =
        ArcFadeMoveTransition(sharedElementNames)

}
