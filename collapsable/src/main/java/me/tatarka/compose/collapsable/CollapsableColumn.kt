package me.tatarka.compose.collapsable

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import kotlin.math.roundToInt

/**
 * A Column where some of the children may collapse. Add [Modifier.collapse] to the children that
 * should collapse. Responds to drags and nested scrolling using [Modifier.draggable].
 *
 * example:
 * ```
 * val behavior = rememberCollapsableBehavior()
 * Column(modifier = Modifier.nestedScroll(behavior.nestedScrollConnection)) {
 *   CollapsableColumn(behavior = behavior) {
 *     TopAppBar(modifier = Modifier.collapse(), title = { Title("Title") })
 *     TabRow { ... }
 *   }
 *   LazyColumn { ... }
 * }
 * ```
 *
 * @param modifier modifiers to be applied to the layout
 * @param state the state to manage collapsing the content
 * @param content the content of the column
 **/
@Composable
fun CollapsableColumn(
    modifier: Modifier = Modifier,
    behavior: CollapsableBehavior = rememberCollapsableBehavior(),
    content: @Composable CollapsableColumnScope.() -> Unit
) {
    CollapsableColumn(
        state = behavior.state,
        modifier = modifier.draggable(behavior),
        content = content,
    )
}

/**
 * A Column where some of the children may collapse. Add [Modifier.collapse] to the children that
 * should collapse.
 *
 * example:
 * ```
 * CollapsableColumn {
 *    TopAppBar(modifier = Modifier.collapse(), title = { Title("Title") })
 *    TabRow { ... }
 * }
 * ```
 *
 * @param modifier modifiers to be applied to the layout
 * @param state the state to manage collapsing the content
 * @param content the content of the column
 **/
@Composable
fun CollapsableColumn(
    modifier: Modifier = Modifier,
    state: CollapsableState = rememberCollapsableState(),
    content: @Composable CollapsableColumnScope.() -> Unit
) {
    Layout(
        content = { CollapsableColumnScopeInstance.content() },
        modifier = modifier,
        measurePolicy = { measureables, constraints ->
            var currentConstraints = constraints.copy(minHeight = 0)
            val placeables = ArrayList<Placeable>(measureables.size)
            var width = constraints.minWidth
            var collapsedHeight = constraints.minHeight
            var expandedHeight = 0

            for (measurable in measureables) {
                val collapse = measurable.parentData as? CollapseChild
                val childConstraints =
                    if (collapse != null && collapse.expandedHeight.isSpecified) {
                        currentConstraints.copy(
                            minHeight = collapse.expandedHeight.roundToPx()
                                .coerceIn(0, currentConstraints.maxHeight),
                        )
                    } else {
                        currentConstraints
                    }

                val placeable = measurable.measure(childConstraints)
                currentConstraints = if (currentConstraints.hasBoundedHeight) {
                    currentConstraints.copy(maxHeight = currentConstraints.maxHeight - placeable.height)
                } else {
                    currentConstraints
                }
                width = maxOf(width, placeable.width)
                expandedHeight += placeable.height
                collapsedHeight += if (collapse != null) {
                    if (collapse.collapsedHeight.isSpecified) {
                        collapse.collapsedHeight.roundToPx()
                    } else if (collapse.expandedHeight.isSpecified) {
                        // was measured with expanded constraints, use min constraints for collapsed height
                        measurable.minIntrinsicHeight(width)
                    } else {
                        placeable.height
                    }
                } else {
                    placeable.height
                }
                placeables.add(placeable)
            }

            state.heightOffsetLimit = (collapsedHeight - expandedHeight).toFloat()

            layout(
                width = width,
                height = expandedHeight + state.heightOffset.roundToInt()
            ) {
                var y = 0
                var clipStart = -1
                for (placeable in placeables) {
                    val collapse = placeable.parentData as? CollapseChild
                    var offset = y + state.heightOffset

                    if (collapse != null) {
                        if (clipStart == -1) {
                            clipStart = y
                        }
                    } else {
                        offset = offset.coerceAtLeast(0f)
                    }

                    val clipShape = if (collapse?.clip == true) {
                        VerticalClipShape(y - clipStart + state.heightOffset)
                    } else {
                        null
                    }

                    placeable.placeWithLayer(x = 0, y = offset.roundToInt()) {
                        if (clipShape != null) {
                            clip = true
                            shape = clipShape
                        }
                    }

                    y += placeable.height
                }
            }
        }
    )
}

private val MaxSupportedElevation = 30.dp

private class VerticalClipShape(val offset: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val inflateSize = with(density) { MaxSupportedElevation.roundToPx().toFloat() }
        return Outline.Rectangle(
            Rect(
                left = -inflateSize,
                top = -offset,
                right = size.width + inflateSize,
                bottom = size.height
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerticalClipShape) return false

        if (offset != other.offset) return false

        return true
    }

    override fun hashCode(): Int {
        return offset.hashCode()
    }
}

@Immutable
@LayoutScopeMarker
interface CollapsableColumnScope {
    /**
     * The default height of a child, can be passed to [Modifier.collapse] to denote it should
     * expand or collapse to the default height.
     */
    val Default get() = Dp.Unspecified

    /**
     * Collapses the child.
     *
     * You can optionally pass in an expanded and collapsed height to transition to. Otherwise it'll
     * collapse from it's default size to 0.
     *
     * @param collapsed the size of the child when it's completely collapsed, defaults to 0
     * @param expanded the size of the child when it's completely expanded, defaults to it's
     * measured size
     * @param clip if the child should be clipped when it collapses to it's original bounds,
     * defaults to true
     */
    @Stable
    fun Modifier.collapse(
        collapsed: Dp = 0.dp,
        expanded: Dp = Default,
        clip: Boolean = true,
    ): Modifier
}

private data class CollapseChild(val collapsedHeight: Dp, val expandedHeight: Dp, val clip: Boolean)

private class ChildBehaviorModifier(private val collapse: CollapseChild) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = collapse
}

private object CollapsableColumnScopeInstance : CollapsableColumnScope {
    override fun Modifier.collapse(collapsed: Dp, expanded: Dp, clip: Boolean): Modifier =
        then(ChildBehaviorModifier(CollapseChild(collapsed, expanded, clip)))
}