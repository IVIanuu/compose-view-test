package com.ivianuu.compose.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ivianuu.compose.ViewComposition
import com.ivianuu.compose.sample.common.CraneWrapper
import com.ivianuu.compose.sample.common.Navigator
import com.ivianuu.compose.sample.common.setContent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

private fun ViewComposition.App() {
    CraneWrapper {
        Navigator { Home2() }
    }
}