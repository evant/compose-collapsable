package me.tatarka.android.collapsable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.CollapsableBottomBehavior
import me.tatarka.compose.collapsable.CollapsableState
import me.tatarka.compose.collapsable.draggable
import me.tatarka.compose.collapsable.rememberCollapsableBottomBehavior
import kotlin.math.roundToInt

private enum class BottomLayoutId {
    Navigation, Title, Action
}

@Composable
fun CustomLayoutBottomAppBar(
    collapsableBehavior: CollapsableBottomBehavior,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
) {
    CustomLayoutBottomAppBarContent(
        state = collapsableBehavior.state,
        onNavigateBack = onNavigateBack,
        contentPadding = windowInsets.asPaddingValues(),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .draggable(collapsableBehavior)
    )
}

@Composable
private fun CustomLayoutBottomAppBarContent(
    state: CollapsableState,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            NavigateBackButton(
                onClick = onNavigateBack,
                modifier = Modifier.layoutId(BottomLayoutId.Navigation)
            )

            Text(
                text = "Title",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .layoutId(BottomLayoutId.Title)
            )

            Button(onClick = {}, modifier = Modifier.layoutId(BottomLayoutId.Action)) {
                Text("Action")
            }
        },
        measurePolicy = { measurables, constraints ->
            val navigationPlaceable = measurables.first { it.layoutId == BottomLayoutId.Navigation }
                .measure(
                    Constraints(
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight
                    )
                )

            val titlePlaceable = measurables.first { it.layoutId == BottomLayoutId.Title }
                .measure(
                    Constraints(
                        maxWidth = constraints.maxWidth - navigationPlaceable.width,
                        maxHeight = constraints.maxHeight
                    )
                )

            val actionPlaceable = measurables.first { it.layoutId == BottomLayoutId.Action }
                .measure(
                    Constraints(
                        maxWidth = constraints.maxWidth,
                        maxHeight = constraints.maxHeight
                    )
                )

            val topPadding = contentPadding.calculateTopPadding().roundToPx()
            val bottomPadding = contentPadding.calculateBottomPadding().roundToPx()

            val expandedHeight = maxOf(navigationPlaceable.height, titlePlaceable.height) +
                    topPadding + bottomPadding

            state.heightOffsetLimit = -expandedHeight.toFloat()

            val width = constraints.minWidth
            val offset = state.heightOffset.roundToInt()
            val height = expandedHeight + offset
            layout(width, height) {
                navigationPlaceable.place(
                    x = 0,
                    y = (expandedHeight - bottomPadding - navigationPlaceable.height) / 2
                )
                titlePlaceable.place(
                    x = navigationPlaceable.width,
                    y = (expandedHeight - bottomPadding - titlePlaceable.height) / 2
                )
                actionPlaceable.place(
                    x = width - actionPlaceable.width - 16.dp.roundToPx(),
                    y = lerp(
                        (-actionPlaceable.height / 2).toDp(),
                        offset.toDp(),
                        state.collapsedFraction
                    ).roundToPx()
                )
            }
        }
    )
}

@Preview(showSystemUi = true)
@Composable
fun CustomLayoutBottomAppBarPreview() {
    CollapsableTheme {
        val collapsableBehavior = rememberCollapsableBottomBehavior()
        Page(
            modifier = Modifier.nestedScroll(collapsableBehavior.nestedScrollConnection),
            bottomBar = { CustomLayoutBottomAppBar(collapsableBehavior, onNavigateBack = {}) }
        )
    }
}
