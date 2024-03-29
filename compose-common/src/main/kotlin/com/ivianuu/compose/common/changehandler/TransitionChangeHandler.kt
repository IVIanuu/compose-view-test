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

import android.transition.Transition
import android.transition.TransitionManager
import com.ivianuu.compose.ComponentChangeHandler
import kotlin.time.Duration
import kotlin.time.milliseconds

abstract class TransitionChangeHandler(val duration: Duration = NO_DURATION) :
    ComponentChangeHandler() {

    private var canceled = false

    override fun execute(changeData: ChangeData) {
        val transition = getTransition(changeData)

        if (duration != NO_DURATION) {
            transition.duration = duration.toLongMilliseconds()
        }

        transition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
                changeData.callback.onComplete()
            }

            override fun onTransitionEnd(transition: Transition) {
                changeData.callback.onComplete()
            }
        })

        prepareForTransition(changeData, transition) {
            // todo transition manager needs a fully attached container
            if (changeData.container.isLaidOut) {
                TransitionManager.beginDelayedTransition(changeData.container, transition)
                executePropertyChanges(changeData, transition)
            } else {
                executePropertyChanges(changeData, transition)
                changeData.callback.onComplete()
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
        changeData.callback.removeFromView()
        changeData.callback.addToView()
    }

    companion object {
        val NO_DURATION = (-1).milliseconds
    }
}