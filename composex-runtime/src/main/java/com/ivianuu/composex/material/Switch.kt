package com.ivianuu.composex.material

import android.content.res.ColorStateList
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.ViewComposition
import androidx.compose.unaryPlus
import androidx.ui.graphics.Color
import androidx.ui.material.themeColor
import com.ivianuu.composex.sourceLocation
import com.ivianuu.composex.view.View
import com.ivianuu.composex.view.ViewDsl
import com.ivianuu.composex.view.onChange
import com.ivianuu.composex.view.set
import com.ivianuu.composex.view.value
import com.ivianuu.composex.view.wrapContent

// todo add disabled color

inline fun ViewComposition.Switch(checked: Boolean, noinline onCheckedChange: (Boolean) -> Unit) =
    Switch(sourceLocation(), checked, onCheckedChange)

fun ViewComposition.Switch(
    key: Any,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(key) {
        value(checked)
        onChange(onCheckedChange)
    }
}

inline fun ViewComposition.Switch(noinline block: ViewDsl<SwitchCompat>.() -> Unit) =
    Switch(sourceLocation(), block)

fun ViewComposition.Switch(key: Any, block: ViewDsl<SwitchCompat>.() -> Unit) =
    View(key, { SwitchCompat(it) }) {
        wrapContent()
        rippleBackground(false)
        color(+themeColor { secondaryVariant })
        block()
    }

fun <T : SwitchCompat> ViewDsl<T>.color(color: Color) {
    val surfaceColor = +themeColor { surface }
    val onSurfaceColor = +themeColor { onSurface }
    set(color) {
        thumbTintList = createColorStateList(
            color,
            surfaceColor
        )
        trackTintList = createColorStateList(
            color.copy(alpha = CheckedTrackOpacity),
            onSurfaceColor.copy(alpha = UncheckedTrackOpacity)
        )
    }
}

private fun createColorStateList(
    activeColor: Color,
    unselectedColor: Color
): ColorStateList {
    return ColorStateList(
        ENABLED_CHECKED_STATES,
        intArrayOf(
            activeColor.toArgb(),
            unselectedColor.toArgb()
        )
    )
}


private val ENABLED_CHECKED_STATES = arrayOf(
    intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),
    intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked)/*,
    intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_checked),
    intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked)*/
)

private val CheckedTrackOpacity = 0.54f
private val UncheckedTrackOpacity = 0.38f