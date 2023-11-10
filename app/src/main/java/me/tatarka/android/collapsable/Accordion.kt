package me.tatarka.android.collapsable

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.tatarka.compose.collapsable.CollapsableColumn
import me.tatarka.compose.collapsable.CollapsableState
import me.tatarka.compose.collapsable.rememberCollapsableState

@Stable
class AccordionState(val collapsableState: CollapsableState) {

    private var animationJob: Job? = null

    val expanded: Boolean
        get() = collapsableState.collapsedFraction < 0.5f

    suspend fun collapse() {
        animationJob?.cancel()
        animationJob = coroutineScope {
            launch {
                AnimationState(initialValue = collapsableState.heightOffset)
                    .animateTo(collapsableState.heightOffsetLimit) {
                        collapsableState.heightOffset = value
                    }
            }
        }
    }

    suspend fun expand() {
        animationJob?.cancel()
        animationJob = coroutineScope {
            launch {
                AnimationState(initialValue = collapsableState.heightOffset)
                    .animateTo(0f) { collapsableState.heightOffset = value }
            }
        }
    }

    suspend fun toggle() {
        if (expanded) {
            collapse()
        } else {
            expand()
        }
    }
}

@Composable
fun rememberAccordionState(): AccordionState {
    val collapsableState = rememberCollapsableState()
    return remember { AccordionState(collapsableState) }
}

@Composable
fun Accordion(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val state = rememberAccordionState()
    CollapsableColumn(modifier = modifier, state = state.collapsableState) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            ProvideTextStyle(MaterialTheme.typography.titleLarge) {
                Text("Title")
            }
            IconButton(onClick = { scope.launch { state.toggle() } }) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = 180 * (1 - state.collapsableState.collapsedFraction)
                    },
                    contentDescription = if (state.expanded) {
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