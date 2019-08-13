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

import android.content.res.ColorStateList
import android.view.View
import androidx.compose.memo
import androidx.ui.graphics.Color
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.ComponentChangeHandler
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.changehandler.CircularRevealChangeHandler
import com.ivianuu.compose.common.changehandler.FadeChangeHandler
import com.ivianuu.compose.common.changehandler.HorizontalChangeHandler
import com.ivianuu.compose.common.changehandler.VerticalChangeHandler
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.navigator
import com.ivianuu.compose.sample.handler.ArcFadeMoveChangeHandler
import com.ivianuu.compose.sample.handler.FlipChangeHandler
import kotlinx.android.synthetic.main.transition_demo.view.*

fun ViewComposition.TransitionDemos() = TransitionDemo(TransitionDemo.values().first())

private fun ViewComposition.TransitionDemo(
    transitionDemo: TransitionDemo
): Route = Route(key = transitionDemo) {
    val transition = +memo { transitionDemo.getTransition() }

    ChangeHandlers(handler = transition) {
        val navigator = +navigator()

        View<View>(key = transitionDemo) {
            layoutRes(transitionDemo.layoutRes)
            bindView {
                if (transitionDemo.color != Color.Transparent && transition_bg != null) {
                    transition_bg.setBackgroundColor(transitionDemo.color.toArgb())
                }

                val nextIndex = transitionDemo.ordinal + 1
                var buttonColor = Color.Transparent
                if (nextIndex < TransitionDemo.values().size) {
                    buttonColor = TransitionDemo.values()[nextIndex].color
                }
                if (buttonColor == Color.Transparent) {
                    buttonColor = TransitionDemo.values()[0].color
                }

                next_button.backgroundTintList = ColorStateList.valueOf(buttonColor.toArgb())
                transition_title.text = transitionDemo.title

                next_button.setOnClickListener {
                    if (nextIndex < TransitionDemo.values().size) {
                        navigator.push(TransitionDemo(TransitionDemo.values()[nextIndex]))
                    } else {
                        navigator.popToRoot()
                    }
                }
            }
        }
    }
}

private enum class TransitionDemo(
    val title: String,
    val layoutRes: Int,
    val color: Color
) {
    VERTICAL(
        "Vertical Slide Animation",
        R.layout.transition_demo,
        Color.Gray
    ) {
        override fun getTransition(): ComponentChangeHandler =
            VerticalChangeHandler()
    },
    CIRCULAR(
        "Circular Reveal Animation (on Lollipop and above, else Fade)",
        R.layout.transition_demo,
        Color.Red
    ) {
        override fun getTransition(): ComponentChangeHandler =
            CircularRevealChangeHandler(R.id.next_button)
    },
    FADE("Fade Animation", R.layout.transition_demo, Color.Blue) {
        override fun getTransition(): ComponentChangeHandler =
            FadeChangeHandler()
    },
    FLIP("Flip Animation", R.layout.transition_demo, Color.Yellow) {
        override fun getTransition(): ComponentChangeHandler =
            FlipChangeHandler()
    },
    HORIZONTAL(
        "Horizontal Slide Animation",
        R.layout.transition_demo,
        Color.Green
    ) {
        override fun getTransition(): ComponentChangeHandler =
            HorizontalChangeHandler()
    },
    ARC_FADE(
        "Arc/Fade Shared Element Transition (on Lollipop and above, else Fade)",
        R.layout.transition_demo_shared,
        Color.Transparent
    ) {
        override fun getTransition(): ComponentChangeHandler =
            ArcFadeMoveChangeHandler(listOf("title", "dot"))
    },
    ARC_FADE_RESET(
        "Arc/Fade Shared Element Transition (on Lollipop and above, else Fade)",
        R.layout.transition_demo,
        Color.Fuchsia
    ) {
        override fun getTransition(): ComponentChangeHandler =
            ArcFadeMoveChangeHandler(listOf("title", "dot"))
    };

    abstract fun getTransition(): ComponentChangeHandler

}