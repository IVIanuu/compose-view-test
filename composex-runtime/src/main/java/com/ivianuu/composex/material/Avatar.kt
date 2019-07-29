package com.ivianuu.composex.material

import androidx.compose.ViewComposition
import androidx.ui.core.dp
import com.ivianuu.composex.sourceLocation
import com.ivianuu.composex.view.Image
import com.ivianuu.composex.view.ImageView
import com.ivianuu.composex.view.image
import com.ivianuu.composex.view.size

inline fun ViewComposition.Avatar(image: Image) = Avatar(sourceLocation(), image)

fun ViewComposition.Avatar(key: Any, image: Image) = ImageView(key) {
    size(40.dp)
    image(image)
}