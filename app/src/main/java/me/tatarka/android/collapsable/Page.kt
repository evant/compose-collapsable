@file:OptIn(ExperimentalMaterial3Api::class)

package me.tatarka.android.collapsable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import me.tatarka.compose.collapsable.CollapsableBehavior

@Composable
fun Page(
    collapsableBehavior: CollapsableBehavior,
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(topBar = topBar, modifier = modifier) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(
                    collapsableBehavior.nestedScrollConnection,
                    collapsableBehavior.nestedScrollDispatcher
                )
        ) {
            items(30) { i ->
                Text(
                    text = "Item ${i + 1}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun NavigateBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(Icons.Default.ArrowBack, "Navigate Back")
    }
}