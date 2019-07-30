package com.ivianuu.compose.sample

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.composer
import androidx.compose.setViewContent
import androidx.ui.graphics.Color
import com.ivianuu.compose.ViewAttribute
import com.ivianuu.compose.view.View
import com.ivianuu.compose.view.set

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setViewContent {
            with(composer) {
                View<FrameLayout> {
                    set(Color.Black) { setBackgroundColor(it.toArgb()) }
                }
            }
        }
    }
}

@ViewAttribute
fun TextView.setSomething(value: String) {

}