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
package me.tatarka.android.collapsable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import me.tatarka.compose.collapsable.CollapsableBottomBehavior
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.rememberCollapsableBottomBehavior
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollapsableBottomBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun scrolled_positioning() {
        lateinit var scrollBehavior: CollapsableBottomBehavior
        val scrollHeightOffsetDp = 20.dp
        var scrollHeightOffsetPx = 0f

        rule.setContent {
            scrollBehavior = rememberCollapsableBottomBehavior()
            scrollHeightOffsetPx = with(LocalDensity.current) { scrollHeightOffsetDp.toPx() }
            Column {
                Spacer(modifier = Modifier.weight(1f))
                SimpleBottomAppBar(
                    modifier = Modifier.testTag(BottomAppBarTestTag),
                    behavior = scrollBehavior
                )
            }
        }

        // Simulate scrolled content.
        rule.runOnIdle {
            scrollBehavior.state.heightOffset = scrollHeightOffsetPx
        }
        rule.waitForIdle()
        rule
            .onNodeWithTag(BottomAppBarTestTag)
            .assertHeightIsEqualTo(BottomAppBarHeight - scrollHeightOffsetDp)
    }

    @Test
    fun allowHorizontalScroll() {
        lateinit var state: LazyListState
        rule.setContent {
            state = rememberLazyListState()
            MultiPageContent(rememberCollapsableBottomBehavior(), state)
        }

        rule.onNodeWithTag(LazyListTag).performTouchInput { swipeLeft() }
        rule.runOnIdle { assertThat(state.firstVisibleItemIndex).isEqualTo(1) }

        rule.onNodeWithTag(LazyListTag).performTouchInput { swipeRight() }
        rule.runOnIdle { assertThat(state.firstVisibleItemIndex).isEqualTo(0) }
    }

    @Composable
    private fun MultiPageContent(scrollBehavior: CollapsableBottomBehavior, state: LazyListState) {
        Column(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag(LazyListTag),
                state = state
            ) {
                items(2) { page ->
                    LazyColumn(
                        modifier = Modifier.fillParentMaxSize()
                    ) {
                        items(50) {
                            BasicText(
                                modifier = Modifier.fillParentMaxWidth(),
                                text = "Item #$page x $it"
                            )
                        }
                    }
                }
            }
            SimpleBottomAppBar(
                modifier = Modifier.testTag(BottomAppBarTestTag),
                behavior = scrollBehavior
            )
        }
    }

    @Composable
    fun SimpleBottomAppBar(
        behavior: CollapsableBottomBehavior,
        modifier: Modifier = Modifier,
        expanded: Dp = BottomAppBarHeight,
    ) {
        CollapsableColumn(behavior, modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .collapse(expanded = expanded)
            )
        }
    }

    private val BottomAppBarHeight = 40.dp
    private val LazyListTag = "lazyList"
    private val BottomAppBarTestTag = "bottomAppBar"
}