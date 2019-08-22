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
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.CompoundButton
import android.widget.ImageView
import com.ivianuu.compose.ChangeHandlers
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ViewById
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.common.RecyclerView
import com.ivianuu.compose.common.Route
import com.ivianuu.compose.common.changehandler.VerticalChangeHandler
import com.ivianuu.compose.distinct
import com.ivianuu.compose.key
import com.ivianuu.compose.memo
import com.ivianuu.compose.sample.common.Scaffold
import com.ivianuu.compose.set
import com.ivianuu.compose.setBy
import com.ivianuu.compose.state
import kotlinx.android.synthetic.main.list_item.view.*

fun SelectionControlsList() = Route {
    ChangeHandlers(handler = VerticalChangeHandler()) {
        Scaffold(
            appBar = { AppBar("Selection Controls") },
            content = {
                RecyclerView {
                    (0..100).forEach {
                        key(it) {
                            val (checked, setChecked) = state { false }
                            distinct(checked) {
                                ListItem(
                                    title = "Title $it",
                                    text = "Text: $it",
                                    onClick = { setChecked(!checked) },
                                    leadingAction = {
                                        ColorAvatar(color = Color.RED)
                                    },
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
            }
        )
    }
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

private inline fun ComponentComposition.CompoundButton(
    layoutRes: Int,
    checked: Boolean,
    noinline onCheckedChanged: (Boolean) -> Unit
) {
    ViewByLayoutRes<CompoundButton>(layoutRes = layoutRes) {
        set(checked) { isChecked = it }
        setBy(checked, onCheckedChanged) { setOnClickListener { onCheckedChanged(!checked) } }
    }
}

private fun ComponentComposition.ListItem(
    title: String? = null,
    titleRes: Int? = null,

    text: String? = null,
    textRes: Int? = null,

    leadingAction: (ComponentComposition.() -> Unit)? = null,
    trailingAction: (ComponentComposition.() -> Unit)? = null,

    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,

    enabled: Boolean = true
) {
    ViewByLayoutRes<View>(layoutRes = R.layout.list_item) {
        setBy(title, titleRes) {
            when {
                title != null -> {
                    list_title.text = title
                    list_text.visibility = VISIBLE
                }
                titleRes != null -> {
                    list_title.setText(titleRes)
                    list_text.visibility = VISIBLE
                }
                else -> {
                    list_title.text = null
                    list_text.visibility = GONE
                }
            }
        }
        setBy(text, textRes) {
            when {
                text != null -> {
                    list_text.text = text
                    list_text.visibility = VISIBLE
                }
                textRes != null -> {
                    list_text.setText(textRes)
                    list_text.visibility = VISIBLE
                }
                else -> {
                    list_text.text = null
                    list_text.visibility = GONE
                }
            }
        }

        set(onClick) {
            if (onClick != null) {
                setOnClickListener { onClick() }
            } else {
                setOnClickListener(null)
            }
        }

        set(onLongClick) {
            if (onLongClick != null) {
                setOnLongClickListener { onLongClick(); true }
            } else {
                setOnLongClickListener(null)
            }
        }

        set(enabled) { enabled ->
            list_title.isEnabled = enabled
            list_text.isEnabled = enabled

            if (list_leading_action != null) {
                list_leading_action.isEnabled = enabled
                (0 until list_leading_action.childCount)
                    .map { list_leading_action.getChildAt(it) }
                    .forEach { it.isEnabled = enabled }
            }

            if (list_trailing_action != null) {
                list_trailing_action.isEnabled = enabled
                (0 until list_trailing_action.childCount)
                    .map { list_trailing_action.getChildAt(it) }
                    .forEach { it.isEnabled = enabled }
            }

            isEnabled = enabled
        }

        ViewById<View>(id = R.id.list_leading_action) {
            leadingAction?.invoke(composition)
        }

        ViewById<View>(id = R.id.list_trailing_action) {
            trailingAction?.invoke(composition)
        }

    }
}