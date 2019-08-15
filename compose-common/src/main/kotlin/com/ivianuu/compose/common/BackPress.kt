/*
 * Copyright 2019 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.compose.common

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import com.ivianuu.compose.ActivityAmbient
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ambient
import com.ivianuu.compose.internal.log
import com.ivianuu.compose.key
import com.ivianuu.compose.onActive

fun ComponentComposition.observeBackPress(onBackPressed: () -> Unit) {
    observeBackPressImpl(onBackPressed)
}

fun ComponentComposition.observeBackPress(
    vararg inputs: Any?,
    onBackPressed: () -> Unit
) {
    key(inputs = *inputs) {
        observeBackPressImpl(onBackPressed)
    }
}

private fun ComponentComposition.observeBackPressImpl(onBackPressed: () -> Unit) {
    val activity = ambient(ActivityAmbient)
    onActive {
        val backPressedDispatcher =
            (activity as OnBackPressedDispatcherOwner).onBackPressedDispatcher

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }

        backPressedDispatcher.addCallback(onBackPressedCallback)

        log { "back press: on active" }

        onDispose {
            log { "back press on inactive" }
            onBackPressedCallback.remove()
        }
    }
}