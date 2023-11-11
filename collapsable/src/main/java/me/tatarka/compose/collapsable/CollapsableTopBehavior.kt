package me.tatarka.compose.collapsable

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

@Deprecated(
    "renamed to CollapsableTopBehavior",
    replaceWith = ReplaceWith("CollapsableTopBehavior")
)
typealias CollapsableBehavior = CollapsableTopBehavior

/**
 * Drives the [CollapsableState] with dragging or nested scrolling.
 *
 * @param state the [CollapsableState]
 * @param snapAnimationSpec Animates snapping to the collapsed or expanded state at the end of a
 * drag or nested scroll. Enabled by default, passing null will disable it and
 * leave your view in a partially collapsed state.
 * @param flingAnimationSpec Animates flinging the view at the end of a drag or nested scroll.
 * Enabled by default, passing null will disable reacting to flings.
 */
@Stable
class CollapsableTopBehavior(
    val state: CollapsableState,
    private val snapAnimationSpec: AnimationSpec<Float>?,
    private val flingAnimationSpec: DecayAnimationSpec<Float>?,
    private val enterAlways: Boolean = false,
) {
    /**
     * Pass this connection to [Modifier.nestedScroll] to response to nested scrolling events.
     */
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Don't intercept if scrolling down.
            if (!enterAlways && available.y > 0f) return Offset.Zero

            val prevHeightOffset = state.heightOffset
            state.heightOffset += available.y
            return if (prevHeightOffset != state.heightOffset) {
                // We're in the middle of top app bar collapse or expand.
                // Consume only the scroll on the Y axis.
                available.copy(x = 0f)
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (enterAlways) {
                state.heightOffset += consumed.y
            } else {
                if (available.y < 0f || consumed.y < 0f) {
                    // When scrolling up, just update the state's height offset.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset += consumed.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }

                if (available.y > 0f) {
                    // Adjust the height offset in case the consumed delta Y is less than what was
                    // recorded as available delta Y in the pre-scroll.
                    val oldHeightOffset = state.heightOffset
                    state.heightOffset += available.y
                    return Offset(0f, state.heightOffset - oldHeightOffset)
                }
            }
            return Offset.Zero
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            return Velocity(
                x = 0f,
                y = settle(flingAnimationSpec, snapAnimationSpec, available.y)
            )
        }
    }

    private suspend fun settle(
        flingAnimationSpec: DecayAnimationSpec<Float>? = null,
        snapAnimationSpec: AnimationSpec<Float>? = null,
        velocity: Float
    ): Float {
        // Check if completely collapsed/expanded. If so, no need to settle and just return
        // Zero Velocity.
        if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
            return 0f
        }
        var remainingVelocity = velocity
        // In case there is an initial velocity that was left after a previous user fling, animate to
        // continue the motion to expand or collapse.
        if (flingAnimationSpec != null) {
            if (abs(velocity) > 1f) {
                var lastValue = 0f
                AnimationState(
                    initialValue = 0f,
                    initialVelocity = velocity,
                )
                    .animateDecay(flingAnimationSpec) {
                        val delta = value - lastValue
                        val initialHeightOffset = state.heightOffset
                        state.heightOffset = initialHeightOffset + delta
                        val consumed = state.heightOffset - initialHeightOffset
                        lastValue = value
                        remainingVelocity = this.velocity
                        // avoid rounding errors and stop if anything is unconsumed
                        if (abs(delta - consumed) > 0.5f) {
                            this.cancelAnimation()
                        }
                    }
            }
        }
        // Snap if animation specs were provided.
        if (snapAnimationSpec != null) {
            if (state.heightOffset < 0 &&
                state.heightOffset > state.heightOffsetLimit
            ) {
                AnimationState(initialValue = state.heightOffset).animateTo(
                    if (state.collapsedFraction < 0.5f) {
                        0f
                    } else {
                        state.heightOffsetLimit
                    },
                    animationSpec = snapAnimationSpec
                ) { state.heightOffset = value }
            }
        }
        return velocity - remainingVelocity
    }


}

/**
 * Remembers a [CollapsableTopBehavior] that can be used to control collapsing. You should use
 *
 * @param state the [CollapsableState]
 * @param snapAnimationSpec animates snapping to the collapsed or expanded state at the end of a
 * drag or nested scroll. Enabled by default, passing null will disable it and
 * leave your view in a partially collapsed state.
 * @param flingAnimationSpec animates flinging the view at the end of a drag or nested scroll.
 * Enabled by default, passing null will disable reacting to flings.
 */
@Composable
@Deprecated(
    "renamed to rememberCollapsableTopBehavior",
    replaceWith = ReplaceWith("rememberCollapsableTopBehavior(state = state, snapAnimationSpec = snapAnimationSpec, flingAnimationSpec = flingAnimationSpec)")
)
fun rememberCollapsableBehavior(
    state: CollapsableState = rememberCollapsableState(),
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
): CollapsableTopBehavior {
    return rememberCollapsableTopBehavior(state, snapAnimationSpec, flingAnimationSpec)
}

/**
 * Remembers a [CollapsableTopBehavior] that can be used to control collapsing. You should use
 *
 * @param state the [CollapsableState]
 * @param snapAnimationSpec animates snapping to the collapsed or expanded state at the end of a
 * drag or nested scroll. Enabled by default, passing null will disable it and
 * leave your view in a partially collapsed state.
 * @param flingAnimationSpec animates flinging the view at the end of a drag or nested scroll.
 * Enabled by default, passing null will disable reacting to flings.
 * @param enterAlways If true the view will start to expand as soon as you start scrolling up,
 * otherwise it will only start expanding when you reach the top of the scrolling view.
 */
@Composable
fun rememberCollapsableTopBehavior(
    state: CollapsableState = rememberCollapsableState(),
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay(),
    enterAlways: Boolean = false,
): CollapsableTopBehavior {
    return CollapsableTopBehavior(
        state = state,
        snapAnimationSpec = snapAnimationSpec,
        flingAnimationSpec = flingAnimationSpec,
        enterAlways = enterAlways,
    )
}

/**
 * A hooks up a [CollapsableTopBehavior] so that dragging the view will expand and collapse it.
 *
 * @param behavior the collapsable behavior
 * @param enabled whether or not drag is enabled
 */
fun Modifier.draggable(behavior: CollapsableTopBehavior, enabled: Boolean = true): Modifier =
    composed {
        val dragLogic = remember { DragLogic(behavior.nestedScrollConnection) }
        draggable(
            rememberDraggableState(onDelta = { dragLogic.drag(it) }),
            onDragStopped = { dragLogic.fling(it) },
            orientation = Orientation.Vertical,
            enabled = enabled,
        ).nestedScroll(behavior.nestedScrollConnection, dragLogic.dispatcher)
    }

private class DragLogic(private val connection: NestedScrollConnection) {
    val dispatcher: NestedScrollDispatcher = NestedScrollDispatcher()

    fun drag(delta: Float) {
        val deltaOffset = Offset(x = 0f, y = delta)
        val preScrollConsumed = dispatcher.dispatchPreScroll(
            available = deltaOffset,
            source = NestedScrollSource.Drag,
        )
        val onPreScrollConsumed = connection.onPreScroll(
            available = deltaOffset - preScrollConsumed,
            source = NestedScrollSource.Drag,
        )
        val onPostScrollConsumed = connection.onPostScroll(
            consumed = Offset.Zero,
            available = deltaOffset - onPreScrollConsumed,
            source = NestedScrollSource.Drag,
        )
        dispatcher.dispatchPostScroll(
            consumed = onPostScrollConsumed,
            available = deltaOffset - onPostScrollConsumed,
            source = NestedScrollSource.Drag,
        )
    }

    suspend fun fling(velocity: Float) {
        val velocityV = Velocity(x = 0f, y = velocity)
        val preFlingConsumed = dispatcher.dispatchPreFling(available = velocityV)
        val onPreFlingConsumed = connection.onPreFling(
            available = velocityV - preFlingConsumed
        )
        val onPostFlingConsumed = connection.onPostFling(
            consumed = Velocity.Zero,
            available = velocityV - onPreFlingConsumed
        )
        dispatcher.dispatchPostFling(
            consumed = onPostFlingConsumed,
            available = velocityV - onPostFlingConsumed
        )
    }
}