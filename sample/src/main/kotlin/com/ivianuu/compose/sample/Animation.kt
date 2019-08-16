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

package com.ivianuu.compose.sample

import android.animation.ValueAnimator
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.ContextAmbient
import com.ivianuu.compose.View
import com.ivianuu.compose.ambient
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.FadeChangeHandler
import com.ivianuu.compose.common.dsl.background
import com.ivianuu.compose.common.dsl.dpInt
import com.ivianuu.compose.common.dsl.layoutGravity
import com.ivianuu.compose.common.dsl.layoutSize
import com.ivianuu.compose.onActive
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.set
import com.ivianuu.compose.state

fun AnimationRoute() = Route {
    ChangeHandlers(handler = FadeChangeHandler()) {
        Scaffold(
            appBar = { AppBar("Animation") },
            content = {
                val (value, setValue) = state { 0f }

                onActive {
                    val animation = ValueAnimator()
                    animation.setFloatValues(0f, 1f)
                    animation.repeatMode = ValueAnimator.REVERSE
                    animation.repeatCount = ValueAnimator.INFINITE

                    animation.addUpdateListener {
                        setValue(it.animatedFraction)
                    }

                    animation.start()

                    onDispose { animation.cancel() }
                }

                View<FrameLayout> {
                    layoutSize(MATCH_PARENT)

                    View<View> {
                        layoutSize(dpInt(100))
                        layoutGravity(CENTER)
                        background(color = ambient(ContextAmbient).resources.getColor(R.color.colorPrimary))

                        set(value) {
                            scaleX = it
                            scaleY = it
                        }
                    }
                }
            }
        )
    }
}