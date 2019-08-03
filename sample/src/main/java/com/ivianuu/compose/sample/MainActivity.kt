package com.ivianuu.compose.sample

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ambient
import com.ivianuu.compose.HorizontalViewTransition
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.Transitions
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroup
import com.ivianuu.compose.disposeComposition
import com.ivianuu.compose.setViewContent
import kotlinx.android.synthetic.main.counter.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewContent {
            ViewGroup(
                ctor = {
                    FrameLayout(this@MainActivity).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            MATCH_PARENT,
                            MATCH_PARENT
                        )
                    }
                },
                children = {
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

    override fun onDestroy() {
        disposeComposition()
        super.onDestroy()
    }
}

private fun ViewComposition.Counter(count: Int) {
    val navigator = +ambient(NavigatorAmbient)
    InflateView<View>(R.layout.counter) {
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