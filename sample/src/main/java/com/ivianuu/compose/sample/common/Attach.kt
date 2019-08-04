package com.ivianuu.compose.sample.common

import android.app.Activity
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ivianuu.compose.CompositionContext
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.MainActivity

fun MainActivity.setContent(
    containerProvider: () -> ViewGroup = { findViewById(android.R.id.content) },
    composable: ViewComposition.() -> Unit
) {
    val holder = ViewModelProvider(
        this,
        ContextHolder.Factory(composable)
    ).get(ContextHolder::class.java)

    val context = holder.context
    val activityRef = holder.activityRef

    activityRef.value = this
    context.setContainer(containerProvider())

    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                activityRef.value = null
                if (isChangingConfigurations) {
                    context.removeContainer()
                } else {
                    context.dispose()
                }
            }
        }
    })
}

private class ContextHolder(composable: ViewComposition.() -> Unit) : ViewModel() {

    val activityRef = Ref<Activity?>(null)

    val context = CompositionContext {
        ActivityRefAmbient.Provider(activityRef) {
            composable()
        }
    }

    class Factory(val composable: ViewComposition.() -> Unit) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            ContextHolder(composable) as T
    }
}