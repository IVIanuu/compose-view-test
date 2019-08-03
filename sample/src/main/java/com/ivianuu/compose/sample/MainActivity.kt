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
import com.ivianuu.compose.View
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.ViewGroup
import com.ivianuu.compose.disposeComposition
import com.ivianuu.compose.setViewContent
import com.ivianuu.compose.sourceLocation
import kotlinx.android.synthetic.main.counter.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewContent {
            ViewGroup(
                key = sourceLocation(),
                ctor = {
                    FrameLayout(this@MainActivity).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            MATCH_PARENT,
                            MATCH_PARENT
                        )
                    }
                },
                children = {
                    Transitions(transition = HorizontalViewTransition()) {
                        Navigator(
                            startRoute = Route {
                                Counter(1)
                            },
                            onExit = { finish() }
                        )
                    }
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
    InflateView<View>(
        key = sourceLocation() + count,
        layoutRes = R.layout.counter
    ) {
        node.title.text = "Count: $count"
        node.inc.setOnClickListener {
            navigator.push(Route { Counter(count + 1) })
        }
        node.dec.setOnClickListener { navigator.pop() }
    }
}