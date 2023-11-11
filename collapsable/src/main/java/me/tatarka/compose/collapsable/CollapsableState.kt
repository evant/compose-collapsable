/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.tatarka.compose.collapsable

import androidx.compose.foundation.gestures.draggable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * A state object that can be hoisted to control and observe the collapsable state. The state is
 * read and updated by [CollapsableTopBehavior].
 *
 * In most cases, this state will be created via [rememberCollapsableState].
 *
 * To update your view from this state you should do two things:
 * 1. Set [heightOffsetLimit] either by passing in the initial value ahead of time or calculating
 * it on layout.
 * 2. Read [heightOffset] and [collapsedFraction] to update your ui based on how far it has
 * collapsed.
 *
 * You can also optionally apply [Modifier.draggable] to allow dragging on the view itself to
 * expand and collapse it.
 *
 * @param initialHeightOffsetLimit the initial value for [heightOffsetLimit]
 * @param initialHeightOffset the initial value for [heightOffset]
 */
@Stable
class CollapsableState(
    initialHeightOffsetLimit: Float,
    initialHeightOffset: Float,
) {
    /**
     * The height offset limit in pixels, which represents the limit it is allowed to collapse to.
     * Note: This value is typically negative, where 0 means no collapsing will take place and a
     * negative value will be the distance it will collapse.
     *
     * Use this limit to coerce the [heightOffset] value when it's updated.
     */
    var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)

    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)

    /**
     * The current height offset in pixels. This height offset is applied to the fixed
     * height to control the displayed height when content is being scrolled.
     *
     * Updates to the [heightOffset] value are coerced between zero and [heightOffsetLimit].
     */
    var heightOffset: Float
        get() = _heightOffset.floatValue
        set(newOffset) {
            _heightOffset.floatValue = newOffset.coerceIn(
                minimumValue = heightOffsetLimit,
                maximumValue = 0f
            )
        }

    /**
     * A value that represents the collapsed height percentage.
     *
     * A `0.0` represents fully expanded, and `1.0` represents fully collapsed (computed * as
     * [heightOffset] / [heightOffsetLimit]).
     */
    val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            heightOffset / heightOffsetLimit
        } else {
            0f
        }

    companion object {
        /**
         * The default [Saver] implementation for [CollapsableState].
         */
        val Saver: Saver<CollapsableState, *> = listSaver(
            save = { listOf(it.heightOffsetLimit, it.heightOffset) },
            restore = {
                CollapsableState(
                    initialHeightOffsetLimit = it[0],
                    initialHeightOffset = it[1],
                )
            }
        )
    }
}

/**
 * Remembers a [CollapsableState].
 */
@Composable
fun rememberCollapsableState(): CollapsableState {
    return rememberSaveable(saver = CollapsableState.Saver) {
        CollapsableState(initialHeightOffset = 0f, initialHeightOffsetLimit = 0f)
    }
}

/**
 * Remembers a [CollapsableState].
 *
 * @param initialHeightOffsetLimit the initial [CollapsableState.heightOffsetLimit] in pixels. This
 * is useful for cases where it is know ahead of time instead of calculated on layout.
 */
@Composable
fun rememberCollapsableState(initialHeightOffsetLimit: Float = 0f): CollapsableState {
    return rememberSaveable(saver = CollapsableState.Saver) {
        CollapsableState(
            initialHeightOffset = 0f,
            initialHeightOffsetLimit = initialHeightOffsetLimit
        )
    }
}

