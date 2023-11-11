@file:OptIn(ExperimentalMaterial3Api::class)

package me.tatarka.android.collapsable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.rememberCollapsableTopBehavior
import me.tatarka.compose.collapsable.rememberCollapsableState

enum class Examples {
    PinnedTabs, ComplexColumn, MotionLayout, CustomLayout, Accordion
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentExample by remember { mutableStateOf<Examples?>(null) }
            BackHandler(enabled = currentExample != null) {
                currentExample = null
            }
            CollapsableTheme {
                when (currentExample) {
                    null -> {
                        MainPage(onSelectExample = { currentExample = it })
                    }

                    Examples.PinnedTabs -> {
                        PinnedTabsTopAppBarPage(onNavigateBack = { currentExample = null })
                    }

                    Examples.ComplexColumn -> {
                        val collapsableBehavior = rememberCollapsableTopBehavior()
                        Page(
                            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
                            topBar = {
                                ComplexCollapsableColumn(
                                    collapsableBehavior,
                                    onNavigateBack = { currentExample = null })
                            }
                        )
                    }

                    Examples.MotionLayout -> {
                        val offsetLimit = with(LocalDensity.current) {
                            (CollapsedHeight - ExpandedHeight).toPx()
                        }
                        val collapsableBehavior = rememberCollapsableTopBehavior(
                            rememberCollapsableState(offsetLimit)
                        )
                        Page(
                            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
                            topBar = {
                                ParallaxBackgroundTopAppBar(
                                    collapsableBehavior = collapsableBehavior,
                                    onNavigateBack = { currentExample = null }
                                )
                            }
                        )
                    }

                    Examples.CustomLayout -> {
                        val collapsableBehavior = rememberCollapsableTopBehavior()
                        Page(
                            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
                            topBar = {
                                CustomLayoutTopAppBar(
                                    collapsableBehavior = collapsableBehavior,
                                    onNavigateBack = { currentExample = null }
                                )
                            }
                        )
                    }

                    Examples.Accordion -> {
                        Column(modifier = Modifier.systemBarsPadding()) {
                            NavigateBackButton(onClick = { currentExample = null })
                            Accordion(modifier = Modifier.fillMaxWidth())
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun MainPage(
    onSelectExample: (Examples) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TopAppBar(title = { Text("Collapsable") })
        TextButton(onClick = { onSelectExample(Examples.PinnedTabs) }) {
            Text("Pinned Tabs")
        }
        TextButton(onClick = { onSelectExample(Examples.ComplexColumn) }) {
            Text("Complex Collapsable Column")
        }
        TextButton(onClick = { onSelectExample(Examples.MotionLayout) }) {
            Text("Motion Layout")
        }
        TextButton(onClick = { onSelectExample(Examples.CustomLayout) }) {
            Text("Custom Layout")
        }
        TextButton(onClick = { onSelectExample(Examples.Accordion) }) {
            Text("Accordion")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainPagePreview() {
    MainPage(onSelectExample = {})
}