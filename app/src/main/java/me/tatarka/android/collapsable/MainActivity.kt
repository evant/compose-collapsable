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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.rememberCollapsableBottomBehavior
import me.tatarka.compose.collapsable.rememberCollapsableState
import me.tatarka.compose.collapsable.rememberCollapsableTopBehavior
import java.io.Serializable

enum class Examples : Serializable {
    PinnedTopTabs, PinnedBottomTabs, ComplexColumn, MotionLayout, CustomTopBar, CustomBottomBar, Accordion
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentExample by rememberSaveable { mutableStateOf<Examples?>(null) }
            BackHandler(enabled = currentExample != null) {
                currentExample = null
            }
            CollapsableTheme {
                when (currentExample) {
                    null -> {
                        MainPage(onSelectExample = { currentExample = it })
                    }

                    Examples.PinnedTopTabs -> {
                        PinnedTabsTopAppBarPage(onNavigateBack = { currentExample = null })
                    }

                    Examples.PinnedBottomTabs -> {
                        val collapsableBehavior = rememberCollapsableBottomBehavior()
                        Page(
                            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
                            bottomBar = {
                                PinnedTabsBottomAppBar(
                                    collapsableBehavior = collapsableBehavior,
                                    onNavigateBack = { currentExample = null }
                                )
                            }
                        )
                    }

                    Examples.ComplexColumn -> {
                        val collapsableBehavior =
                            rememberCollapsableTopBehavior(snapAnimationSpec = null)
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
                            rememberCollapsableState(initialHeightOffsetLimit = offsetLimit)
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

                    Examples.CustomTopBar -> {
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

                    Examples.CustomBottomBar -> {
                        val collapsableBehavior = rememberCollapsableBottomBehavior()
                        Page(
                            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
                            bottomBar = {
                                CustomLayoutBottomAppBar(
                                    collapsableBehavior = collapsableBehavior,
                                    onNavigateBack = { currentExample = null },
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
        TextButton(onClick = { onSelectExample(Examples.PinnedTopTabs) }) {
            Text("Pinned Top Tabs")
        }
        TextButton(onClick = { onSelectExample(Examples.PinnedBottomTabs) }) {
            Text("Pinned Bottom Tabs")
        }
        TextButton(onClick = { onSelectExample(Examples.ComplexColumn) }) {
            Text("Complex Collapsable Column")
        }
        TextButton(onClick = { onSelectExample(Examples.MotionLayout) }) {
            Text("Motion Layout")
        }
        TextButton(onClick = { onSelectExample(Examples.CustomTopBar) }) {
            Text("Custom Top Bar")
        }
        TextButton(onClick = { onSelectExample(Examples.CustomBottomBar) }) {
            Text("Custom Bottom Bar")
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