package com.ivianuu.compose.material

import androidx.compose.ViewComposition
import androidx.ui.core.currentTextStyle
import androidx.ui.core.dp
import com.ivianuu.compose.sourceLocation
import com.ivianuu.compose.view.Image
import com.ivianuu.compose.view.ImageView
import com.ivianuu.compose.view.image
import com.ivianuu.compose.view.imageColor
import com.ivianuu.compose.view.size

inline fun ViewComposition.Icon(image: Image) = Icon(sourceLocation(), image)

fun ViewComposition.Icon(key: Any, image: Image) = ImageView(key) {
    size(24.dp)
    image(image)
    (+currentTextStyle()).color?.let { imageColor(it) }
}