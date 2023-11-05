package me.tatarka.android.collapsable

import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.rememberCollapsableBehavior

@Composable
fun Accordian(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val collapsableBehavior = rememberCollapsableBehavior()
    CollapsableColumn(modifier = modifier, behavior = collapsableBehavior, canDrag = false) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 16.dp)
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                Text("Title")
            }
            IconButton(
                onClick = {
                    scope.launch {
                        if (collapsableBehavior.state.collapsedFraction < 0.5f) {
                            collapsableBehavior.collapse()
                        } else {
                            collapsableBehavior.expand()
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = 180 * (1 - collapsableBehavior.state.collapsedFraction)
                    },
                    contentDescription = if (collapsableBehavior.state.collapsedFraction == 0f) {
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
fun AccordianPreview() {
    Surface {
        Accordian(modifier = Modifier.fillMaxWidth())
    }
}