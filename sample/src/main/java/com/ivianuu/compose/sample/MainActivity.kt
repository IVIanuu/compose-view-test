package com.ivianuu.compose.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ivianuu.compose.InflateView
import com.ivianuu.compose.disposeComposition
import com.ivianuu.compose.setViewContent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewContent {
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
                            startRoute = Counter(1),
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