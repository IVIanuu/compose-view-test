package com.ivianuu.compose.sample.common

import android.app.Activity
import androidx.compose.Ambient

val ActivityRefAmbient = Ambient.of<Ref<Activity?>>()