package org.ethereumphone.dgenlibrary.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ethereumphone.dgenlibrary.theme.dgenOcean
import org.ethereumphone.dgenlibrary.theme.dgenTurqoise
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun Modifier.verticalLazyListScrollbar(
    lazyListState: LazyListState,
    width: Dp = 6.dp,
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color = dgenOcean,
    scrollBarColor: Color = dgenTurqoise,
    scrollBarCornerRadius: Float = 4f,
    trackTopInset: Dp = 32.dp,
    trackBottomInset: Dp = 32.dp,
    endPadding: Dp = 12.dp,
    minThumbHeight: Dp = 40.dp,
    verticalOffset: Dp = 0.dp,
    autoHide: Boolean = false,
    fadeInDuration: Int = 250,
    fadeOutDuration: Int = 250,
    hideDelay: Long = 1000L
): Modifier {
    // Animation state for auto-hide functionality
    var targetAlpha by remember { mutableStateOf(if (autoHide) 0f else 1f) }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(
            durationMillis = if (targetAlpha == 1f) fadeInDuration else fadeOutDuration,
            easing = LinearEasing
        ),
        label = "scrollbar_alpha"
    )

    // Handle auto-hide based on scroll state
    if (autoHide) {
        LaunchedEffect(lazyListState.isScrollInProgress) {
            if (lazyListState.isScrollInProgress) {
                targetAlpha = 1f
            } else {
                delay(hideDelay)
                targetAlpha = 0f
            }
        }
    }

    val layoutInfo = lazyListState.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset
    val visibleItemsInfo = layoutInfo.visibleItemsInfo
    val viewportHeightPx = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
        .toFloat()
        .coerceAtLeast(1f)

    // Smooth, stable per-item height estimate to avoid jumping thumb size/position
    var smoothedItemHeightPx by remember(totalItemsCount) { mutableStateOf<Float?>(null) }
    val currentVisibleAverage = if (visibleItemsInfo.isNotEmpty()) {
        visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
    } else null
    if (currentVisibleAverage != null) {
        smoothedItemHeightPx = when (val prev = smoothedItemHeightPx) {
            null -> currentVisibleAverage
            else -> prev + (currentVisibleAverage - prev) * 0.15f
        }
    }
    val itemHeightEstimatePx = smoothedItemHeightPx ?: viewportHeightPx

    val contentHeightEstimatePx = (itemHeightEstimatePx * totalItemsCount)
        .coerceAtLeast(viewportHeightPx)
    val scrolledOffsetPx = (firstVisibleItemIndex * itemHeightEstimatePx) + firstVisibleItemScrollOffset
    val maxScrollPx = (contentHeightEstimatePx - viewportHeightPx).coerceAtLeast(1f)
    var progressFraction = (scrolledOffsetPx / maxScrollPx).coerceIn(0f, 1f)

    // Force exact ends when at the start or when the last item is fully visible
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset <= 0) {
        progressFraction = 0f
    } else if (visibleItemsInfo.isNotEmpty()) {
        val lastVisible = visibleItemsInfo.last()
        val lastVisibleBottom = (lastVisible.offset + lastVisible.size).toFloat()
        if (lastVisible.index >= totalItemsCount - 1 && lastVisibleBottom <= viewportHeightPx + 0.5f) {
            progressFraction = 1f
        }
    }

    return this.then(
        Modifier.drawWithContent {
            drawContent()

            if (totalItemsCount == 0) return@drawWithContent

            // Track geometry (right side with end padding)
            val widthPx = width.toPx()
            val endPaddingPx = endPadding.toPx()
            val xRight = size.width - endPaddingPx - widthPx
            val yShiftPx = verticalOffset.toPx()
            val trackStartY = trackTopInset.toPx() + yShiftPx
            val trackEndY = (size.height - trackBottomInset.toPx() + yShiftPx)
                .coerceAtLeast(trackStartY)
            val trackHeight = (trackEndY - trackStartY).coerceAtLeast(0f)

            // Pill shape: ensure fully rounded ends regardless of provided corner radius
            val effectiveCorner = max(scrollBarCornerRadius, widthPx / 2f)
            val cornerRadius = CornerRadius(effectiveCorner)

            // Thumb size: proportional to viewport vs content; clamped to min height
            val thumbFractionOfTrack = (viewportHeightPx / contentHeightEstimatePx)
                .coerceIn(0.06f, 1f)
            val thumbHeightPx = (trackHeight * thumbFractionOfTrack)
                .coerceAtLeast(minThumbHeight.toPx())
                .coerceAtMost(trackHeight)

            // Thumb position mapped smoothly to the full track range
            val maxThumbOffset = (trackHeight - thumbHeightPx).coerceAtLeast(0f)
            val thumbOffsetYPx = (progressFraction * maxThumbOffset)
                .coerceIn(0f, maxThumbOffset)

            if (showScrollBarTrack) {
                drawRoundRect(
                    color = scrollBarTrackColor.copy(alpha = alpha * scrollBarTrackColor.alpha),
                    cornerRadius = cornerRadius,
                    topLeft = Offset(xRight, trackStartY),
                    size = Size(widthPx, trackHeight)
                )
            }

            drawRoundRect(
                color = scrollBarColor.copy(alpha = alpha * scrollBarColor.alpha),
                cornerRadius = cornerRadius,
                topLeft = Offset(xRight, trackStartY + thumbOffsetYPx),
                size = Size(widthPx, thumbHeightPx)
            )
        }
    )
}

@Composable
fun Modifier.verticalScrollBarForLazyGrid(
    gridState: LazyGridState,
    width: Dp = 6.dp,
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color,
    scrollBarColor: Color,
    scrollBarCornerRadius: Float = 4f,
    /* Space before the track begins */
    topPadding: Dp = 32.dp,
    /* Space after the track ends */
    bottomPadding: Dp = 32.dp,
    endPadding: Dp = 16.dp
): Modifier {
    var targetAlpha by remember { mutableStateOf(0f) }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing)
    )

    LaunchedEffect(gridState.isScrollInProgress) {
        if (gridState.isScrollInProgress) {
            targetAlpha = 1f
        } else {
            delay(1000)
            targetAlpha = 0f
        }
    }

    return this.then(
        Modifier.drawWithContent {
            drawContent()

            val layoutInfo = gridState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItemsCount = layoutInfo.totalItemsCount

            if (visibleItems.isEmpty() || totalItemsCount == 0) return@drawWithContent

            val trackHeight = (size.height - topPadding.toPx() - bottomPadding.toPx()).coerceAtLeast(0f)

            val visibleItemCount = visibleItems.size.toFloat()
            val minThumbHeight = 40.dp.toPx()
            val thumbHeight = if (trackHeight < minThumbHeight) {
                trackHeight
            } else {
                ((visibleItemCount / totalItemsCount) * trackHeight).coerceIn(minThumbHeight, trackHeight)
            }

            val firstVisibleItem = gridState.firstVisibleItemIndex
            val firstItemOffset = gridState.firstVisibleItemScrollOffset

            val averageItemHeight = visibleItems.sumOf { it.size.height }.toFloat() / visibleItems.size
            val maxScrollOffset = (totalItemsCount - visibleItemCount) * averageItemHeight
            val scrolledOffset = (firstVisibleItem * averageItemHeight) + firstItemOffset

            val thumbOffset = ((scrolledOffset / maxScrollOffset) * (trackHeight - thumbHeight))
                .coerceIn(0f, trackHeight - thumbHeight)

            if (showScrollBarTrack) {
                drawRoundRect(
                    color = scrollBarTrackColor.copy(alpha = alpha),
                    cornerRadius = CornerRadius(scrollBarCornerRadius),
                    topLeft = Offset(size.width - endPadding.toPx(), topPadding.toPx()),
                    size = Size(width.toPx(), trackHeight)
                )
            }

            drawRoundRect(
                color = scrollBarColor.copy(alpha = alpha),
                cornerRadius = CornerRadius(scrollBarCornerRadius),
                topLeft = Offset(size.width - endPadding.toPx(), topPadding.toPx() + thumbOffset),
                size = Size(width.toPx(), thumbHeight)
            )
        }
    )
}

