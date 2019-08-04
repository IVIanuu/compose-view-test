package com.ivianuu.compose.common

import android.transition.Transition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import com.ivianuu.compose.ViewTransition

abstract class TransitionViewTransition : ViewTransition() {

    private var canceled = false

    override fun execute(
        container: ViewGroup,
        from: View?,
        to: View?,
        isPush: Boolean,
        onComplete: () -> Unit
    ) {
        val changeData = ChangeData(container, from, to, isPush, onComplete)

        val transition = getTransition(changeData)

        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
                onComplete()
            }

            override fun onTransitionEnd(transition: Transition) {
                onComplete()
            }
        })

        prepareForTransition(changeData, transition) {
            // todo transition manager needs a fully attached container
            if (container.isLaidOut) {
                TransitionManager.beginDelayedTransition(container, transition)
                executePropertyChanges(changeData, transition)
            } else {
                executePropertyChanges(changeData, transition)
                onComplete()
            }
        }
    }

    override fun cancel() {
        canceled = true
    }

    /**
     * Returns the [Transition] to use to swap views
     */
    protected abstract fun getTransition(changeData: ChangeData): Transition

    /**
     * Called before starting the [transition]
     */
    protected open fun prepareForTransition(
        changeData: ChangeData,
        transition: Transition,
        onTransitionPrepared: () -> Unit
    ) {
        onTransitionPrepared()
    }

    /**N
     * This should set all view properties needed for the transition to work properly.
     * By default it removes the [from] view
     * and adds the [to] view.
     */
    protected open fun executePropertyChanges(
        changeData: ChangeData,
        transition: Transition?
    ) {
        if (changeData.from != null) {
            changeData.container.removeView(changeData.from!!)
        }

        if (changeData.to != null && changeData.to!!.parent == null) {
            changeData.container.addView(changeData.to!!)
        }
    }

    companion object {
        const val NO_DURATION = -1L
    }
}