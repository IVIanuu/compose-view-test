package com.ivianuu.compose.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ivianuu.compose.CompositionContext
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.sample.common.CraneWrapper
import com.ivianuu.compose.sample.common.Navigator
import com.ivianuu.compose.sample.common.Scaffold

class ContextHolder : ViewModel() {
    val context = CompositionContext {
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var context: CompositionContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context = ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(ContextHolder::class.java).context

        context.setComposable {
            CraneWrapper {
                Scaffold(
                    appBar = {
                        InflateView<Toolbar>(
                            layoutRes = R.layout.app_bar,
                            updateView = {
                                title = "Compose sample"
                            })
                    },
                    content = {
                        Navigator(
                            startRoute = Counter(1, nextColor()),
                            onExit = { finish() }
                        )
                    }
                )
            }
        }

        if (savedInstanceState == null) {
            context.compose()
        }

        context.setContainer(findViewById(android.R.id.content))
    }

    override fun onDestroy() {
        if (isChangingConfigurations) {
            context.removeContainer()
        } else {
            context.dispose()
        }
        super.onDestroy()
    }
}