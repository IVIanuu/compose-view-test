package com.ivianuu.compose.common

import android.transition.Transition
import android.transition.TransitionManager
import com.ivianuu.compose.ViewChangeHandler

abstract class TransitionChangeHandler(val duration: Long = NO_DURATION) : ViewChangeHandler() {

    private var canceled = false

    override fun execute(changeData: ChangeData) {
        val transition = getTransition(changeData)

        if (duration != NO_DURATION) {
            transition.duration = duration
        }

        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
                changeData.onComplete()
            }

            override fun onTransitionEnd(transition: Transition) {
                changeData.onComplete()
            }
        })

        prepareForTransition(changeData, transition) {
            // todo transition manager needs a fully attached container
            if (changeData.container.isLaidOut) {
                TransitionManager.beginDelayedTransition(changeData.container, transition)
                executePropertyChanges(changeData, transition)
            } else {
                executePropertyChanges(changeData, transition)
                changeData.onComplete()
            }
        }
    }

    override fun cancel() {
        canceled = true
    }

    protected abstract fun getTransition(changeData: ChangeData): Transition

    protected open fun prepareForTransition(
        changeData: ChangeData,
        transition: Transition,
        onTransitionPrepared: () -> Unit
    ) {
        onTransitionPrepared()
    }

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