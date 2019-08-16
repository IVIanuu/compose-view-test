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

package com.ivianuu.compose.sample

import android.view.Gravity.CENTER
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout.VERTICAL
import com.google.android.material.button.MaterialButton
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.View
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.HorizontalChangeHandler
import com.ivianuu.compose.common.dsl.LinearLayout
import com.ivianuu.compose.common.dsl.TextView
import com.ivianuu.compose.common.dsl.dpInt
import com.ivianuu.compose.common.dsl.gravity
import com.ivianuu.compose.common.dsl.layoutMargin
import com.ivianuu.compose.common.dsl.layoutSize
import com.ivianuu.compose.common.dsl.onClick
import com.ivianuu.compose.common.dsl.orientation
import com.ivianuu.compose.common.dsl.text
import com.ivianuu.compose.common.dsl.textAppearance
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.state

fun CounterRoute() = Route {
    ChangeHandlers(handler = HorizontalChangeHandler()) {
        Scaffold(
            appBar = { AppBar("Counter") },
            content = {
                val (count, setCount) = state { 0 }

                LinearLayout {
                    layoutSize(MATCH_PARENT)
                    orientation(VERTICAL)
                    gravity(CENTER)

                    TextView {
                        layoutSize(WRAP_CONTENT)
                        textAppearance(R.style.TextAppearance_MaterialComponents_Headline3)
                        text("Count: $count")
                    }

                    View<MaterialButton> {
                        layoutSize(WRAP_CONTENT)
                        layoutMargin(top = dpInt(8))
                        text("Inc")
                        onClick { setCount(count + 1) }
                    }

                    View<MaterialButton> {
                        layoutSize(WRAP_CONTENT)
                        layoutMargin(top = dpInt(8))
                        text("Dec")
                        onClick { setCount(count - 1) }
                    }
                }
            }
        )
    }
}