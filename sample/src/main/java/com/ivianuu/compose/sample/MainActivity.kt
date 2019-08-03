package com.ivianuu.compose.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ambient
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.disposeComposition
import com.ivianuu.compose.setViewContent
import com.ivianuu.compose.transition.HorizontalViewTransition
import com.ivianuu.compose.transition.Transitions
import kotlinx.android.synthetic.main.counter.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewContent {
            CraneWrapper(key = "crane") {
                Scaffold(
                    key = "scaffold",
                    appBar = {
                        InflateView<Toolbar>(
                            key = "toolbar",
                            layoutRes = R.layout.app_bar,
                            update = {
                            title = "Compose sample"
                        })
                    },
                    content = {
                        Navigator(
                            startRoute = Route {
                                Transitions(transition = HorizontalViewTransition()) {
                                    Counter(1)
                                }
                            },
                            onExit = { finish() }
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        disposeComposition()
        super.onDestroy()
    }
}

private fun ViewComposition.Counter(count: Int) {
    val navigator = +ambient(NavigatorAmbient)
    InflateView<View>("counter", R.layout.counter) {
        node.title.text = "Count: $count"
        node.inc.setOnClickListener {
            navigator.push(Route {
                Transitions(HorizontalViewTransition()) {
                    Counter(count + 1)
                }
            })
        }
        node.dec.setOnClickListener { navigator.pop() }
    }
}