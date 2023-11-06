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
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A state object that can be hoisted to control and observe the collapsable state. The state is
 * read and updated by [CollapsableBehavior].
 *
 * In most cases, this state will be created via [rememberCollapsableState].
 *
 * To update your view from this state you should do two things:
 * 1. Set [heightOffsetLimit] either by passing in the initial value ahead of time or calculating
 * it on layout.
 * 2. Read [heightOffset] and [collapsedFraction] to update your ui based on how far it has
 * collapsed.
 *
 * You can also optionally apply [Modifier.draggable] to allow dragging on the view itself to
 * expand and collapse it.
 *
 * @param initialHeightOffsetLimit the initial value for [heightOffsetLimit]
 * @param initialHeightOffset the initial value for [heightOffset]
 */
@Stable
class CollapsableState(
    initialHeightOffsetLimit: Float,
    initialHeightOffset: Float,
) {
    /**
     * The height offset limit in pixels, which represents the limit it is allowed to collapse to.
     * Note: This value is typically negative, where 0 means no collapsing will take place and a
     * negative value will be the distance it will collapse.
     *
     * Use this limit to coerce the [heightOffset] value when it's updated.
     */
    var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)

    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)

    /**
     * The current height offset in pixels. This height offset is applied to the fixed
     * height to control the displayed height when content is being scrolled.
     *
     * Updates to the [heightOffset] value are coerced between zero and [heightOffsetLimit].
     */
    var heightOffset: Float
        get() = _heightOffset.floatValue
        set(newOffset) {
            _heightOffset.floatValue = newOffset.coerceIn(
                minimumValue = heightOffsetLimit,
                maximumValue = 0f
            )
        }

    /**
     * A value that represents the collapsed height percentage.
     *
     * A `0.0` represents fully expanded, and `1.0` represents fully collapsed (computed * as
     * [heightOffset] / [heightOffsetLimit]).
     */
    val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) {
            heightOffset / heightOffsetLimit
        } else {
            0f
        }

    companion object {
        /**
         * The default [Saver] implementation for [CollapsableState].
         */
        val Saver: Saver<CollapsableState, *> = listSaver(
            save = { listOf(it.heightOffsetLimit, it.heightOffset) },
            restore = {
                CollapsableState(
                    initialHeightOffsetLimit = it[0],
                    initialHeightOffset = it[1],
                )
            }
        )
    }
}

/**
 * Remembers a [CollapsableState].
 */
@Composable
fun rememberCollapsableState(): CollapsableState {
    return rememberSaveable(saver = CollapsableState.Saver) {
        CollapsableState(initialHeightOffset = 0f, initialHeightOffsetLimit = 0f)
    }
}

/**
 * Remembers a [CollapsableState].
 *
 * @param initialHeightOffsetLimit the initial [CollapsableState.heightOffsetLimit] in pixels. This
 * is useful for cases where it is know ahead of time instead of calculated on layout.
 */
@Composable
fun rememberCollapsableState(initialHeightOffsetLimit: Float = 0f): CollapsableState {
    return rememberSaveable(saver = CollapsableState.Saver) {
        CollapsableState(
            initialHeightOffset = 0f,
            initialHeightOffsetLimit = initialHeightOffsetLimit
        )
    }
}

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
class CollapsableBehavior(
    val state: CollapsableState,
    private val snapAnimationSpec: AnimationSpec<Float>?,
    private val flingAnimationSpec: DecayAnimationSpec<Float>?,
) {
    /**
     * Pass this connection to [Modifier.nestedScroll] to response to nested scrolling events.
     */
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Don't intercept if scrolling down.
            if (available.y > 0f) return Offset.Zero

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
 * Remembers a [CollapsableBehavior] that can be used to control collapsing. You should use
 *
 * @param state the [CollapsableState]
 * @param snapAnimationSpec animates snapping to the collapsed or expanded state at the end of a
 * drag or nested scroll. Enabled by default, passing null will disable it and
 * leave your view in a partially collapsed state.
 * @param flingAnimationSpec animates flinging the view at the end of a drag or nested scroll.
 * Enabled by default, passing null will disable reacting to flings.
 */
@Composable
fun rememberCollapsableBehavior(
    state: CollapsableState = rememberCollapsableState(),
    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
): CollapsableBehavior {
    return CollapsableBehavior(
        state = state,
        snapAnimationSpec = snapAnimationSpec,
        flingAnimationSpec = flingAnimationSpec,
    )
}

/**
 * A hooks up a [CollapsableBehavior] so that dragging the view will expand and collapse it.
 *
 * @param behavior the collapsable behavior
 * @param enabled whether or not drag is enabled
 */
fun Modifier.draggable(behavior: CollapsableBehavior, enabled: Boolean = true): Modifier =
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