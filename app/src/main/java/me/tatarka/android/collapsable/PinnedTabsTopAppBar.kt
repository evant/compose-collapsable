@file:OptIn(ExperimentalMaterial3Api::class)

package me.tatarka.android.collapsable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.CollapsableBehavior
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.rememberCollapsableBehavior

@Composable
fun PinnedTabsTopAppBar(
    collapsableBehavior: CollapsableBehavior,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    CollapsableColumn(
        behavior = collapsableBehavior,
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = { Text("Title") },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                NavigateBackButton(onClick = onNavigateBack)
            },
        )
        Text(
            text = "Here's some content between the app bar title and tabs that should collapse out of the way.",
            modifier = Modifier
                .padding(16.dp)
                .collapse()
        )
        TabRow(selectedTabIndex = selectedTab) {
            for (i in 0 until 3) {
                Tab(
                    selected = i == selectedTab,
                    onClick = { selectedTab = i },
                    text = { Text("Tab ${i + 1}") }
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PinnedTabsTopAppBarPreview() {
    CollapsableTheme {
        val collapsableBehavior = rememberCollapsableBehavior()
        Page(
            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
            topBar = { PinnedTabsTopAppBar(collapsableBehavior, onNavigateBack = {}) }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun MultipleCollapsableChildrenClippingPreview() {
    CollapsableTheme {
        val collapsableBehavior = rememberCollapsableBehavior()
        Page(
            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
                    Text("Content above the collapsable that should not be overlapped")
                    CollapsableColumn(behavior = collapsableBehavior) {
                        Text("This should also not overlap")
                        Text("This should collapse and be clipped", modifier = Modifier.collapse())
                        Text(
                            "This should also collapse but not be clipped",
                            modifier = Modifier.collapse(clip = false)
                        )
                        Text("This should stick around")
                    }
                }
            }
        )
    }
}