package me.tatarka.android.collapsable

import androidx.compose.foundation.background
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.CollapsableBottomBehavior
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.rememberCollapsableBottomBehavior

@Composable
fun PinnedTabsBottomAppBar(
    collapsableBehavior: CollapsableBottomBehavior,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    CollapsableColumn(
        behavior = collapsableBehavior,
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BottomAppBarDefaults.containerColor,
            divider = {},
        ) {
            for (i in 0 until 3) {
                Tab(
                    selected = i == selectedTab,
                    onClick = { selectedTab = i },
                    text = { Text("Tab ${i + 1}") }
                )
            }
        }
        BottomAppBar(
            modifier = Modifier.collapse(),
            actions = {
                NavigateBackButton(onClick = onNavigateBack)
            }
        )
    }
}

@Preview
@Composable
private fun PinnedTabsBottomAppBarPreview() {
    CollapsableTheme {
        val collapsableBehavior = rememberCollapsableBottomBehavior()
        Page(
            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
            bottomBar = {
                PinnedTabsBottomAppBar(
                    collapsableBehavior = collapsableBehavior,
                    onNavigateBack = {})
            }
        )
    }
}