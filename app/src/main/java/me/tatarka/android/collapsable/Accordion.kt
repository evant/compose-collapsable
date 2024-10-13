package me.tatarka.android.collapsable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.rememberCollapseUpState

@Composable
fun Accordion(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val state = rememberCollapseUpState()
    val expanded by remember { derivedStateOf { state.collapsedFraction < 0.5f }}
    CollapsableColumn(modifier = modifier, state = state) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                Text("Title")
            }
            IconButton(onClick = {
                scope.launch {
                    if (expanded) {
                        state.animateCollapse()
                    } else {
                        state.animateExpand()
                    }
                }
            }) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = 180 * (1 - state.collapsedFraction)
                    },
                    contentDescription = if (expanded) {
                        "Collapse"
                    } else {
                        "Expand"
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .collapse()
                .padding(16.dp)
        ) {
            Text("Content that should hide when the user collapses the accordion")
        }
    }
}

@Preview
@Composable
fun AccordionPreview() {
    Surface {
        Accordion(modifier = Modifier.fillMaxWidth())
    }
}