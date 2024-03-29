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

package com.ivianuu.compose

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.Ambient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

val ActivityAmbient = Ambient.of<Activity> { error("No activity found") }
val ContextAmbient = ActivityAmbient as Ambient<Context>

fun ComponentActivity.setContent(
    container: ViewGroup = findViewById(android.R.id.content),
    composable: ComponentComposition.() -> Unit
) {
    val holder = ViewModelProvider(this, ContextHolder).get(ContextHolder::class.java)

    val context = holder.context

    context.setContainer(container)

    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                if (isChangingConfigurations) {
                    context.removeContainer()
                } else {
                    context.dispose()
                }
            }
        }
    })

    context.setComposable {
        ActivityAmbient.Provider(this@setContent) {
            composable()
        }
    }

    context.compose()
}

private class ContextHolder : ViewModel() {
    val context = CompositionContext()

    companion object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = ContextHolder() as T
    }
}