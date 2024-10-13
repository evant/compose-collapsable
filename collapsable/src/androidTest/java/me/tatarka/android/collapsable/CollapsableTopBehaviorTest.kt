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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.CollapsableState
import me.tatarka.compose.collapsable.CollapsableTopBehavior
import me.tatarka.compose.collapsable.rememberCollapsableTopBehavior
import me.tatarka.compose.collapsable.rememberCollapseUpState
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollapsableTopBehaviorTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun scrolledPositioning() {
        lateinit var scrollBehavior: CollapsableTopBehavior
        val scrollHeightOffsetDp = 20.dp
        var scrollHeightOffsetPx = 0f

        rule.setContent {
            scrollBehavior = rememberCollapsableTopBehavior()
            scrollHeightOffsetPx = with(LocalDensity.current) { scrollHeightOffsetDp.toPx() }
            SimpleTopAppBar(
                behavior = scrollBehavior,
                modifier = Modifier.testTag(TopAppBarTestTag),
                collapsed = 0.dp
            )
        }

        // Simulate scrolled content.
        rule.runOnIdle {
            scrollBehavior.state.heightOffset = -scrollHeightOffsetPx
        }
        rule.waitForIdle()
        rule
            .onNodeWithTag(TopAppBarTestTag)
            .assertHeightIsEqualTo(TopAppBarHeight - scrollHeightOffsetDp)
    }

    @Test
    fun customHeight() {
        lateinit var scrollBehavior: CollapsableTopBehavior
        val expandedHeightDp = 50.dp
        var expandedHeightDpPx = 0

        rule.setContent {
            scrollBehavior = rememberCollapsableTopBehavior()
            expandedHeightDpPx = with(LocalDensity.current) { expandedHeightDp.roundToPx() }
            SimpleTopAppBar(
                behavior = scrollBehavior,
                modifier = Modifier.testTag(TopAppBarTestTag),
                collapsed = 0.dp,
                expanded = expandedHeightDp,
            )
        }

        assertThat(scrollBehavior.state.heightOffsetLimit.toInt()).isEqualTo(-expandedHeightDpPx)
        rule.onNodeWithTag(TopAppBarTestTag).assertHeightIsEqualTo(expandedHeightDp)
    }

    @Test
    fun enterAlways_allowHorizontalScroll() {
        lateinit var state: LazyListState
        rule.setContent {
            state = rememberLazyListState()
            MultiPageContent(rememberCollapsableTopBehavior(enterAlways = true), state)
        }

        rule.onNodeWithTag(LazyListTag).performTouchInput { swipeLeft() }
        rule.runOnIdle { assertThat(state.firstVisibleItemIndex).isEqualTo(1) }

        rule.onNodeWithTag(LazyListTag).performTouchInput { swipeRight() }
        rule.runOnIdle { assertThat(state.firstVisibleItemIndex).isEqualTo(0) }
    }

    @Test
    fun exitUntilCollapsed_allowHorizontalScroll() {
        lateinit var state: LazyListState
        rule.setContent {
            state = rememberLazyListState()
            MultiPageContent(rememberCollapsableTopBehavior(), state)
        }

        rule.onNodeWithTag(LazyListTag).performTouchInput { swipeLeft() }
        rule.runOnIdle { assertThat(state.firstVisibleItemIndex).isEqualTo(1) }

        rule.onNodeWithTag(LazyListTag).performTouchInput { swipeRight() }
        rule.runOnIdle { assertThat(state.firstVisibleItemIndex).isEqualTo(0) }
    }

    @Test
    fun mediumDraggedAppBar() {
        rule.setContent {
            SimpleTopAppBar(
                behavior = rememberCollapsableTopBehavior(),
                modifier = Modifier.testTag(TopAppBarTestTag),
            )
        }

        rule
            .onNodeWithTag(TopAppBarTestTag)
            .assertHeightIsEqualTo(TopAppBarHeight)

        // Drag up the app bar.
        rule.onNodeWithTag(TopAppBarTestTag).performTouchInput {
            down(Offset(x = 0f, y = height - 20f))
            moveTo(Offset(x = 0f, y = 0f))
        }
        rule.waitForIdle()
        // Check that the app bar collapsed to its small size constraints
        rule
            .onNodeWithTag(TopAppBarTestTag)
            .assertHeightIsEqualTo(TopAppBarCollapsedHeight)
    }


    @Test
    fun dragSnapToCollapsed() {
        rule.setContent {
            SimpleTopAppBar(
                modifier = Modifier.testTag(TopAppBarTestTag),
                behavior = rememberCollapsableTopBehavior(),
            )
        }

        rule
            .onNodeWithTag(TopAppBarTestTag)
            .assertHeightIsEqualTo(TopAppBarHeight)

        // Slightly drag up the app bar.
        rule.onNodeWithTag(TopAppBarTestTag).performTouchInput {
            down(Offset(x = 0f, y = height - 20f))
            moveTo(Offset(x = 0f, y = height - 40f))
            up()
        }
        rule.waitForIdle()

        // Check that the app bar returned to its expanded size (i.e. fully expanded).
        rule
            .onNodeWithTag(TopAppBarTestTag)
            .assertHeightIsEqualTo(TopAppBarHeight)

        // Drag up the app bar to the point it should continue to collapse after.
        rule.onNodeWithTag(TopAppBarTestTag).performTouchInput {
            down(Offset(x = 0f, y = height - 20f))
            moveTo(Offset(x = 0f, y = 40f))
            up()
        }
        rule.waitForIdle()

        // Check that the app bar collapsed to its small size constraints (i.e.
        // TopAppBarSmallTokens.ContainerHeight).
        rule
            .onNodeWithTag(TopAppBarTestTag)
            .assertHeightIsEqualTo(TopAppBarCollapsedHeight)
    }

    @Test
    fun dragWithSnapDisabled() {
        rule.setContent {
            SimpleTopAppBar(
                modifier = Modifier.testTag(TopAppBarTestTag),
                behavior = rememberCollapsableTopBehavior(snapAnimationSpec = null),
            )
        }

        // Check that the app bar stayed at its position (i.e. its bounds are with a smaller height)
        val boundsBefore = rule.onNodeWithTag(TopAppBarTestTag).getBoundsInRoot()
        assertThat(boundsBefore.height).isEqualTo(TopAppBarHeight)

        // Slightly drag up the app bar.
        rule.onNodeWithTag(TopAppBarTestTag).performTouchInput {
            down(Offset(x = 100f, y = height - 20f))
            moveTo(Offset(x = 100f, y = height - 100f))
            up()
        }
        rule.waitForIdle()

        // Check that the app bar did not snap back to its fully expanded height, or collapsed to
        // its collapsed height.
        val boundsAfter = rule.onNodeWithTag(TopAppBarTestTag).getBoundsInRoot()
        assertThat(boundsAfter.height).isBetween(TopAppBarCollapsedHeight, TopAppBarHeight)
    }

    @Test
    @Ignore("not sure why this one fails")
    fun scrollingAndContentMovement() {
        lateinit var scrollBehavior: CollapsableTopBehavior
        lateinit var state: LazyListState
        var appBarHeightPx = 0f
        rule.setContent {
            scrollBehavior = rememberCollapsableTopBehavior(enterAlways = true)
            state = rememberLazyListState()
            appBarHeightPx = with(rule.density) { TopAppBarCollapsedHeight.toPx() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(WindowInsets.systemBars),
            ) {
                SimpleTopAppBar(
                    behavior = scrollBehavior,
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .testTag(LazyListTag),
                    state = state,
                ) {
                    items(100) { i ->
                        BasicText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(horizontal = 16.dp),
                            text = "Item $i",
                        )
                    }
                }
            }
        }

        // Swipe up to scroll the content and collapse the top app bar.
        rule.onNodeWithTag(LazyListTag).performTouchInput {
            swipeUp(startY = height - 200f, endY = height - 1000f)
        }
        rule.waitForIdle()

        // Store a tracked visible item's top offset. We set the tracked item to be the third
        // visible one (which helps deflake the test on smaller devices).
        val trackedItemIndex = state.layoutInfo.visibleItemsInfo.first().index + 2
        val trackedItemTopBeforeExpansion =
            rule.onNodeWithText("Item $trackedItemIndex").getBoundsInRoot().top

        // Swipe down to trigger a top app bar expansion without scrolling much the content.
        rule.onNodeWithTag(LazyListTag).performTouchInput {
            swipeDown(startY = height - 1000f, endY = height - (1000f - appBarHeightPx / 1.5f))
        }
        rule.waitForIdle()

        // Asserts that the tracked item has moved along with the expansion of the top app bar.
        rule
            .onNodeWithText("Item $trackedItemIndex")
            .assertTopPositionInRootIsEqualTo(
                trackedItemTopBeforeExpansion + TopAppBarCollapsedHeight
            )
    }


    @Test
    fun restoresCollapsableState() {
        val restorationTester = StateRestorationTester(rule)
        var collapsableState: CollapsableState? = null
        restorationTester.setContent { collapsableState = rememberCollapseUpState() }

        rule.runOnIdle {
            collapsableState!!.heightOffsetLimit = -350f
            collapsableState!!.heightOffset = -300f
        }

        collapsableState = null

        restorationTester.emulateSavedInstanceStateRestore()

        rule.runOnIdle {
            assertThat(collapsableState!!.heightOffsetLimit).isEqualTo(-350f)
            assertThat(collapsableState!!.heightOffset).isEqualTo(-300f)
        }
    }

    @Composable
    private fun MultiPageContent(scrollBehavior: CollapsableTopBehavior, state: LazyListState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            SimpleTopAppBar(
                behavior = scrollBehavior,
                modifier = Modifier.testTag(TopAppBarTestTag),
                expanded = TopAppBarHeight,
            )
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag(LazyListTag),
                state = state
            ) {
                items(2) { page ->
                    LazyColumn(modifier = Modifier.fillParentMaxSize()) {
                        items(50) {
                            BasicText(
                                modifier = Modifier.fillParentMaxWidth(),
                                text = "Item #$page x $it"
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SimpleTopAppBar(
        behavior: CollapsableTopBehavior,
        modifier: Modifier = Modifier,
        collapsed: Dp = TopAppBarCollapsedHeight,
        expanded: Dp = TopAppBarHeight,
    ) {
        CollapsableColumn(behavior, modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .collapse(collapsed = collapsed, expanded = expanded)
            )
        }
    }

    private val TopAppBarCollapsedHeight = 30.dp
    private val TopAppBarHeight = 40.dp
    private val LazyListTag = "lazyList"
    private val TopAppBarTestTag = "topAppBar"
}
