package com.ivianuu.compose.sample

import android.animation.ValueAnimator
import android.view.View
import androidx.compose.Recompose
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.layoutRes
import com.ivianuu.compose.onActive
import com.ivianuu.compose.sample.common.Route
import com.ivianuu.compose.state
import kotlinx.android.synthetic.main.animation.view.*

fun ViewComposition.Animation() = Route {
    Recompose { recompose ->
        var value by state { 0f }
        onActive {
            val animation = ValueAnimator()
            animation.setFloatValues(0f, 1f)
            animation.repeatMode = ValueAnimator.REVERSE
            animation.repeatCount = ValueAnimator.INFINITE

            animation.addUpdateListener {
                value = it.animatedFraction
                recompose()
            }

            animation.start()

            println("anim: on active")

            onDispose {
                println("anim: on dispose")
                animation.cancel()
            }
        }

        println("anim: rebuild $value")

        View<View> {
            layoutRes(R.layout.animation)
            updateView {
                println("anim: update view $value")
                animation_view.scaleX = value
                animation_view.scaleY = value
            }
        }
    }
}