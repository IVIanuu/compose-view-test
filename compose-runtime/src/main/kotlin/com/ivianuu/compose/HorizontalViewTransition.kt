package com.ivianuu.compose

import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator

open class HorizontalViewTransition : ViewTransition() {

    private var canceled = false
    private var changeData: ChangeData? = null
    private var animator: ViewPropertyAnimator? = null
    private var onReadyOrAbortedListener: OnReadyOrAbortedListener? = null

    override fun execute(
        container: ViewGroup,
        view: View,
        direction: Direction,
        index: Int?,
        onComplete: () -> Unit
    ) {
        changeData = ChangeData(container, view, direction, index, onComplete)

        when(direction) {
            Direction.In -> {
                container.addView(view, index!!)
                onReadyOrAbortedListener = OnReadyOrAbortedListener(view) {
                    if (!canceled) {
                        view.translationX = -view.width.toFloat()
                        view.animate()
                            .translationX(0f)
                            .withEndAction { onComplete() }
                            .start()
                    } else {
                        onComplete()
                    }
                }
            }
            Direction.Out -> {
                onReadyOrAbortedListener = OnReadyOrAbortedListener(view) {
                    if (!canceled) {
                        view.animate()
                            .translationX(-view.width.toFloat())
                            .withEndAction {
                                container.removeView(view)
                                onComplete()
                            }
                            .start()
                    } else {
                        container.removeView(view)
                        onComplete()
                    }
                }
            }
        }
    }

    override fun cancel() {
        canceled = true
        when {
            animator != null -> {
                animator?.cancel()
                changeData!!.container.removeView(changeData!!.view)
                changeData?.onComplete?.invoke()
            }
            onReadyOrAbortedListener != null -> {
                onReadyOrAbortedListener?.onReadyOrAborted()
            }
        }
        changeData?.let {

        }
        changeData = null
    }

    override fun copy() = HorizontalViewTransition()

}