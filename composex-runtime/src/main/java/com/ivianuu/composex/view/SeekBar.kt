package com.ivianuu.composex.view

import android.content.res.ColorStateList
import android.widget.SeekBar
import androidx.compose.ViewComposition
import androidx.ui.graphics.Color
import androidx.ui.material.themeColor
import com.ivianuu.composex.sourceLocation

inline fun ViewComposition.SeekBar(noinline block: ViewDsl<SeekBar>.() -> Unit) =
    SeekBar(sourceLocation(), block)

fun ViewComposition.SeekBar(key: Any, block: ViewDsl<SeekBar>.() -> Unit) =
    View(key, { SeekBar(it) }) {
        progressColor(+themeColor { secondary })
        block()
    }

/*
fun <T : SeekBar> ViewDsl<T>.min(min: Int) {
    set(min) { this.min = min }
}

fun <T : SeekBar> ViewDsl<T>.max(max: Int) {
    set(max) { this.max = max }
}

fun <T : SeekBar> ViewDsl<T>.incValue(incValue: Int) {
    set(incValue) { in }
}

fun <T : SeekBar> ViewDsl<T>.range(range: IntRange) {
    set(range) {  }
}
// todo
*/

fun <T : SeekBar> ViewDsl<T>.progressColor(color: Color) {
    set(color) {
        val secondaryColorStateList = ColorStateList.valueOf(color.toArgb())
        indeterminateTintList = secondaryColorStateList
        progressTintList = secondaryColorStateList
        progressBackgroundTintList =
            ColorStateList.valueOf(color.copy(alpha = BackgroundOpacity).toArgb())
        thumbTintList = secondaryColorStateList
    }
}

fun <T : SeekBar> ViewDsl<T>.value(value: Int) {
    set(value) { this.progress = value }
}

fun <T : SeekBar> ViewDsl<T>.onValueChange(onValueChange: (Int) -> Unit) {
    set(onValueChange) {
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onValueChange(seekBar.progress)
            }
        })
    }
}

// The opacity applied to the primary color to create the background color
private const val BackgroundOpacity = 0.24f