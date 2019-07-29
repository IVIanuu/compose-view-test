package com.ivianuu.composex.material

/**
import androidx.compose.ViewComposition
import androidx.compose.ambient
import androidx.ui.graphics.Color
import androidx.ui.material.ripple.CurrentRippleTheme
import androidx.ui.material.themeColor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ivianuu.composex.sourceLocation
import com.ivianuu.composex.view.View
import com.ivianuu.composex.view.ViewDsl
import com.ivianuu.composex.view.backgroundColor
import com.ivianuu.composex.view.imageColor
import com.ivianuu.composex.view.set
import com.ivianuu.composex.view.wrapContent

inline fun ViewComposition.FloatingActionButton(noinline block: ViewDsl<FloatingActionButton>.() -> Unit) =
    FloatingActionButton(sourceLocation(), block)

fun ViewComposition.FloatingActionButton(
    key: Any,
    block: ViewDsl<FloatingActionButton>.() -> Unit
) =
    View(key, { FloatingActionButton(it) }) {
        wrapContent()

        val secondaryColor = +themeColor { secondary }
        backgroundColor(secondaryColor)
        imageColor(+themeColor { onSecondary })
        val rippleColor = (+ambient(CurrentRippleTheme)).colorCallback(secondaryColor)
        rippleColor(rippleColor)
        block()
    }

enum class FabSize {
    Normal, Mini;

    fun toSizeInt() = when (this) {
        Normal -> FloatingActionButton.SIZE_NORMAL
        Mini -> FloatingActionButton.SIZE_MINI
    }
}

fun <T : FloatingActionButton> ViewDsl<T>.size(size: FabSize) {
    set(size) { this.size = it.toSizeInt() }
}

fun <T : FloatingActionButton> ViewDsl<T>.rippleColor(color: Color) {
    set(color) { this.rippleColor = color.toArgb() }
}*/