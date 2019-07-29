package com.ivianuu.compose.material

import androidx.compose.ViewComposition
import androidx.ui.core.dp
import com.ivianuu.compose.sourceLocation
import com.ivianuu.compose.view.Image
import com.ivianuu.compose.view.ImageView
import com.ivianuu.compose.view.image
import com.ivianuu.compose.view.size

inline fun ViewComposition.Avatar(image: Image) = Avatar(sourceLocation(), image)

fun ViewComposition.Avatar(key: Any, image: Image) = ImageView(key) {
    size(40.dp)
    image(image)
}