package com.ivianuu.compose.sample.handler

import android.content.res.ColorStateList
import android.view.View
import androidx.compose.memo
import androidx.ui.graphics.Color
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewChangeHandler
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.common.FadeChangeHandler
import com.ivianuu.compose.common.HorizontalChangeHandler
import com.ivianuu.compose.common.VerticalChangeHandler
import com.ivianuu.compose.sample.R
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.sample.common.navigator
import kotlinx.android.synthetic.main.transition_demo.view.*

fun ViewComposition.TransitionDemos() = TransitionDemo(TransitionDemo.values().first())

private fun ViewComposition.TransitionDemo(transitionDemo: TransitionDemo): Route =
    Route(key = transitionDemo) {
        val transition = +memo { transitionDemo.getTransition() }

        Transitions(changeHandler = transition) {
            val navigator = navigator()

            InflateView<View>(
                key = transitionDemo,
                layoutRes = transitionDemo.layoutRes,
                updateView = {
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
                })
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
        override fun getTransition(): ViewChangeHandler = VerticalChangeHandler()
    },
    CIRCULAR(
        "Circular Reveal Animation (on Lollipop and above, else Fade)",
        R.layout.transition_demo,
        Color.Red
    ) {
        override fun getTransition(): ViewChangeHandler = CircularRevealChangeHandler(0, 0) // todo
    },
    FADE("Fade Animation", R.layout.transition_demo, Color.Blue) {
        override fun getTransition(): ViewChangeHandler = FadeChangeHandler()
    },
    FLIP("Flip Animation", R.layout.transition_demo, Color.Yellow) {
        override fun getTransition(): ViewChangeHandler = FlipChangeHandler()
    },
    HORIZONTAL(
        "Horizontal Slide Animation",
        R.layout.transition_demo,
        Color.Green
    ) {
        override fun getTransition(): ViewChangeHandler = HorizontalChangeHandler()
    },
    ARC_FADE(
        "Arc/Fade Shared Element Transition (on Lollipop and above, else Fade)",
        R.layout.transition_demo_shared,
        Color.Transparent
    ) {
        override fun getTransition(): ViewChangeHandler =
            ArcFadeMoveChangeHandler(listOf("title", "dot"))
    },
    ARC_FADE_RESET(
        "Arc/Fade Shared Element Transition (on Lollipop and above, else Fade)",
        R.layout.transition_demo,
        Color.Fuchsia
    ) {
        override fun getTransition(): ViewChangeHandler =
            ArcFadeMoveChangeHandler(listOf("title", "dot"))
    };

    abstract fun getTransition(): ViewChangeHandler

}