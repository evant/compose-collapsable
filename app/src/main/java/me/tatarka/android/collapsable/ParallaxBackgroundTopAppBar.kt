@file:OptIn(ExperimentalMotionApi::class)

package me.tatarka.android.collapsable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.lerp
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import me.tatarka.android.collapsable.ui.theme.CollapsableTheme
import me.tatarka.compose.collapsable.CollapsableBehavior
import me.tatarka.compose.collapsable.CollapsableState
import me.tatarka.compose.collapsable.draggable
import me.tatarka.compose.collapsable.rememberCollapsableBehavior
import me.tatarka.compose.collapsable.rememberCollapsableState

private val LightBlue = Color(91, 206, 250)
private val LightPink = Color(245, 169, 184)

// due to MotionLayout limitations we need to know the heights in advanced.
val CollapsedHeight = 64.dp
val ExpandedHeight = 280.dp

private enum class PBLayoutId {
    Background, Navigation, Title
}

@Composable
fun ParallaxBackgroundTopAppBar(
    collapsableBehavior: CollapsableBehavior,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ParallaxBackgroundTopAppBarContent(
        state = collapsableBehavior.state,
        onNavigateBack = onNavigateBack,
        modifier = modifier
            .fillMaxWidth()
            .draggable(collapsableBehavior)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
    )
}

@Composable
private fun ParallaxBackgroundTopAppBarContent(
    state: CollapsableState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textStyle = lerp(
        MaterialTheme.typography.displaySmall,
        MaterialTheme.typography.titleLarge,
        state.collapsedFraction
    ).copy(textMotion = TextMotion.Animated)

    MotionLayout(
        start = ConstraintSet {
            val background = createRefFor(PBLayoutId.Background)
            val navigation = createRefFor(PBLayoutId.Navigation)
            val title = createRefFor(PBLayoutId.Title)

            constrain(background) {
                top.linkTo(parent.top)
                centerHorizontallyTo(parent)
                height = Dimension.value(ExpandedHeight)
            }

            constrain(navigation) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }

            constrain(title) {
                start.linkTo(parent.start, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 16.dp)
            }
        },
        end = ConstraintSet {
            val background = createRefFor(PBLayoutId.Background)
            val navigation = createRefFor(PBLayoutId.Navigation)
            val title = createRefFor(PBLayoutId.Title)

            constrain(background) {
                top.linkTo(parent.top)
                centerHorizontallyTo(parent)
                height = Dimension.value(CollapsedHeight)
            }

            constrain(navigation) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }

            constrain(title) {
                start.linkTo(navigation.end, margin = 8.dp)
                centerVerticallyTo(navigation)
            }
        },
        progress = state.collapsedFraction,
        modifier = modifier,
    ) {
        Box(modifier = Modifier
            .layoutId(PBLayoutId.Background)
            .fillMaxWidth()
            .height(ExpandedHeight)
            .graphicsLayer {
                alpha = 1 - state.collapsedFraction
            }
            .drawBehind {
                val height = ExpandedHeight.toPx()
                listOf(
                    LightBlue,
                    LightPink,
                    Color.White,
                    LightPink,
                    LightBlue
                ).forEachIndexed { i, color ->
                    drawRect(
                        color = color,
                        topLeft = Offset(0f, state.heightOffset * 0.25f + height * i / 5),
                        size = Size(size.width, height / 5)
                    )
                }
            }
        )

        NavigateBackButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .layoutId(PBLayoutId.Navigation)
                .heightIn(min = CollapsedHeight)
        )

        Text(
            text = "Title",
            style = textStyle,
            modifier = Modifier.layoutId(PBLayoutId.Title)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun ParallaxBackgroundTopAppBarPreview() {
    CollapsableTheme {
        val offsetLimit = with(LocalDensity.current) {
            (CollapsedHeight - ExpandedHeight).toPx()
        }
        val collapsableBehavior = rememberCollapsableBehavior(
            rememberCollapsableState(offsetLimit)
        )
        Page(
            collapsableBehavior = collapsableBehavior,
            topBar = { ParallaxBackgroundTopAppBar(collapsableBehavior, onNavigateBack = {}) }
        )
    }
}
