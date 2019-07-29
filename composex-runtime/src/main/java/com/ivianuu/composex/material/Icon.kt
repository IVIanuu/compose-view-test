package com.ivianuu.composex.material

import androidx.compose.ViewComposition
import androidx.ui.core.currentTextStyle
import androidx.ui.core.dp
import com.ivianuu.composex.sourceLocation
import com.ivianuu.composex.view.Image
import com.ivianuu.composex.view.ImageView
import com.ivianuu.composex.view.image
import com.ivianuu.composex.view.imageColor
import com.ivianuu.composex.view.size

inline fun ViewComposition.Icon(image: Image) = Icon(sourceLocation(), image)

fun ViewComposition.Icon(key: Any, image: Image) = ImageView(key) {
    size(24.dp)
    image(image)
    (+currentTextStyle()).color?.let { imageColor(it) }
}