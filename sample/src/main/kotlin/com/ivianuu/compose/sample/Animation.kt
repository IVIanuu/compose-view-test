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
import android.view.View
import androidx.compose.memo
import androidx.compose.onActive
import androidx.compose.state
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.changehandler.FadeChangeHandler
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.Scaffold
import kotlinx.android.synthetic.main.animation.view.*

fun ViewComposition.Animation() = Route {
    val handler = +memo { FadeChangeHandler() }
    ChangeHandlers(handler = handler) {
        Scaffold(
            appBar = { AppBar("Animation") },
            content = {
                val (value, setValue) = +state { 0f }

                +onActive {
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

                View<View> {
                    layoutRes(R.layout.animation)
                    bindView {
                        animation_view.scaleX = value
                        animation_view.scaleY = value
                    }
                }
            }
        )
    }
}