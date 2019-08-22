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
import android.view.View
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentTest {

    @Test
    fun testLifecycle() {
        val scenario = ActivityScenario.launch(TestActivity::class.java)

        var bindViewCalls = 0
        var unbindViewCalls = 0

        scenario.withActivity {
            setContent {
                View<View> {
                    onBindView { bindViewCalls += 1 }
                    onUnbindView { unbindViewCalls += 1 }
                }
            }
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)

        assertEquals(1, bindViewCalls)
        assertEquals(1, unbindViewCalls)
    }

}

fun <A : Activity> ActivityScenario<A>.withActivity(block: A.() -> Unit) {
    onActivity(block)
}

class TestActivity : ComponentActivity()