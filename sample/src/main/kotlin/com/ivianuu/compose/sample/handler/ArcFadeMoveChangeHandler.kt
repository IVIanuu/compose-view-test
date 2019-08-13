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

package com.ivianuu.compose.sample.handler

import android.transition.ArcMotion
import android.transition.ChangeBounds
import android.transition.ChangeClipBounds
import android.transition.ChangeTransform
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionSet
import android.view.View
import com.ivianuu.compose.common.changehandler.SharedElementChangeHandler
import com.ivianuu.compose.common.findNamedView

class ArcFadeMoveChangeHandler(private val sharedElementNames: List<String>) :
    SharedElementChangeHandler() {

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
        ArcFadeMoveChangeHandler(sharedElementNames)

}
