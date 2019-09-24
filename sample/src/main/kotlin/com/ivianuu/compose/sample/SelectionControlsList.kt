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

import android.graphics.Color
import android.widget.CompoundButton
import android.widget.ImageView
import com.ivianuu.compose.*
import com.ivianuu.compose.common.RecyclerView
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.VerticalChangeHandler
import com.ivianuu.compose.sample.common.ListItem
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.sample.common.Text

fun SelectionControlsList() =
    Route(key = "SelectionControlsList", handler = VerticalChangeHandler()) {
    Scaffold(
        appBar = { AppBar("Selection Controls") },
        content = {
            RecyclerView {
                (1..100).forEach { index ->
                    key(index) {
                        val (checked, setChecked) = state { false }

                        ListItem(
                            title = { Text(text = "Title $index") },
                            text = { Text(text = "Text: $index") },
                            onClick = { setChecked(!checked) },
                            leadingAction = { ColorAvatar(color = Color.RED) },
                            trailingAction = {
                                val selectionControlType = memo {
                                    SelectionControl.values()
                                        .toList()
                                        .shuffled()
                                        .first()
                                }

                                selectionControlType.compose(this, checked, setChecked)
                            }
                        )
                    }
                }
            }
        }
    )
}

private enum class SelectionControl(
    val compose: ComponentComposition.(
        checked: Boolean,
        onCheckedChanged: (Boolean) -> Unit
    ) -> Unit
) {
    CheckBox(
        compose = { checked, onCheckedChanged ->
            CheckBox(
                checked = checked,
                onCheckedChanged = onCheckedChanged
            )
        }
    ),
    RadioButton(
        compose = { checked, onCheckedChanged ->
            RadioButton(
                checked = checked,
                onCheckedChanged = onCheckedChanged
            )
        }
    ),
    Switch(
        compose = { checked, onCheckedChanged ->
            Switch(
                checked = checked,
                onCheckedChanged = onCheckedChanged
            )
        }
    )
}

private fun ComponentComposition.ColorAvatar(color: Int) {
    ViewByLayoutRes<ImageView>(layoutRes = R.layout.avatar) {
        set(color) { setBackgroundColor(it) }
    }
}

private fun ComponentComposition.CheckBox(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    CompoundButton(
        layoutRes = R.layout.checkbox,
        checked = checked,
        onCheckedChanged = onCheckedChanged
    )
}

private fun ComponentComposition.RadioButton(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    CompoundButton(
        layoutRes = R.layout.radio_button,
        checked = checked,
        onCheckedChanged = onCheckedChanged
    )
}

private fun ComponentComposition.Switch(
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    CompoundButton(
        layoutRes = R.layout.switch_compat,
        checked = checked,
        onCheckedChanged = onCheckedChanged
    )
}

private fun ComponentComposition.CompoundButton(
    layoutRes: Int,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit
) {
    ViewByLayoutRes<CompoundButton>(layoutRes = layoutRes) {
        set(checked) { isChecked = it }
        setBy(checked, onCheckedChanged) { setOnClickListener { onCheckedChanged(!checked) } }
    }
}