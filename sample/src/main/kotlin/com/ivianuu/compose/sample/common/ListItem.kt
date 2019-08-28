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

package com.ivianuu.compose.sample.common

import android.view.View
import com.ivianuu.compose.ComponentComposition
import com.ivianuu.compose.ViewById
import com.ivianuu.compose.ViewByLayoutRes
import com.ivianuu.compose.sample.R
import com.ivianuu.compose.set
import kotlinx.android.synthetic.main.list_item.view.*

fun ComponentComposition.ListItem(
    title: (ComponentComposition.() -> Unit)? = null,
    text: (ComponentComposition.() -> Unit)? = null,

    leadingAction: (ComponentComposition.() -> Unit)? = null,
    trailingAction: (ComponentComposition.() -> Unit)? = null,

    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,

    enabled: Boolean = true
) {
    ViewByLayoutRes<View>(layoutRes = R.layout.list_item) {
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
            list_text_container.isEnabled = enabled
            (0 until list_text_container.childCount)
                .map { list_text_container.getChildAt(it) }
                .forEach { it.isEnabled = enabled }

            list_leading.isEnabled = enabled
            (0 until list_leading.childCount)
                .map { list_leading.getChildAt(it) }
                .forEach { it.isEnabled = enabled }

            list_trailing.isEnabled = enabled
            (0 until list_trailing.childCount)
                .map { list_trailing.getChildAt(it) }
                .forEach { it.isEnabled = enabled }

            isEnabled = enabled
        }

        ViewById<View>(id = R.id.list_text_container) {
            if (title != null) {
                TextStyle(textAppearance = R.style.TextAppearance_MaterialComponents_Subtitle1) {
                    title()
                }
            }

            if (text != null) {
                TextStyle(textAppearance = R.style.TextAppearance_AppCompat_Body2) {
                    text()
                }
            }
        }

        ViewById<View>(id = R.id.list_leading) {
            leadingAction?.invoke(composition)
        }

        ViewById<View>(id = R.id.list_trailing) {
            trailingAction?.invoke(composition)
        }

    }
}