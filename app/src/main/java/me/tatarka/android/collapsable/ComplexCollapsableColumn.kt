package me.tatarka.android.collapsable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.CollapsableTopBehavior
import me.tatarka.compose.collapsable.rememberCollapsableTopBehavior

@Composable
fun ComplexCollapsableColumn(
    collapsableBehavior: CollapsableTopBehavior,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    CollapsableColumn(
        behavior = collapsableBehavior,
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        NavigateBackButton(onClick = onNavigateBack)

        Text(
            "Complex Collapsable Column",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "Collapse Section 1",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(16.dp)
                .collapse()
        )

        Text(
            text = "Here's the first section of content that collapses",
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .collapse()
        )

        Text(
            text = "This part pins", Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                .fillMaxWidth()
                .padding(16.dp)
        )

        Text(
            text = "Collapse Section 2",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .collapse()
        )

        Text(
            text = "Here's the second section of content that collapses",
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
fun ComplexCollapsableColumnPreview() {
    CollapsableTheme {
        val collapsableBehavior = rememberCollapsableTopBehavior()
        Page(
            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
            topBar = { ComplexCollapsableColumn(collapsableBehavior, onNavigateBack = {}) }
        )
    }
}
