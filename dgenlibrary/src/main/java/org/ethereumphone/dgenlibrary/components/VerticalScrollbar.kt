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

@Composable
fun Modifier.verticalLazyListScrollbar(
    lazyListState: LazyListState,
    width: Dp = 6.dp,
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color,
    scrollBarColor: Color,
    scrollBarCornerRadius: Float = 4f,
    /* Space to leave at the top of the list before the scrollbar begins */
    topPadding: Dp = 32.dp,
    /* Space to leave at the bottom of the list after the scrollbar ends */
    bottomPadding: Dp = 32.dp,
    endPadding: Float = 32f,
    fixed: Boolean = false
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    var isScrolling by remember { mutableStateOf(false) }
    var targetAlpha by remember { mutableStateOf(0f) } // Start hidden
    var targetScrollBarOffset by remember { mutableStateOf(0f) } // Thumb Y position

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 250, easing = LinearEasing)
    )

    val fixedalpha = 1f

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (lazyListState.isScrollInProgress) {
            isScrolling = true
            targetAlpha = 1f // Fade in
        } else {
            delay(1000) // Wait 1 second before fading out
            targetAlpha = 0f // Fade out smoothly
        }
    }

    return this.then(
        Modifier.drawWithContent {
            drawContent()

            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            val totalItemsCount = layoutInfo.totalItemsCount

            if (visibleItemsInfo.isEmpty() || totalItemsCount == 0) return@drawWithContent

            // 1️⃣ Compute scrollbar track height from the provided top & bottom padding
            val topPaddingPx = topPadding.toPx()
            val bottomPaddingPx = bottomPadding.toPx()
            val trackHeight = (size.height - topPaddingPx - bottomPaddingPx).coerceAtLeast(0f)

            // 2️⃣ Compute thumb height proportionally
            val visibleItemCount = visibleItemsInfo.size.toFloat()
            val minThumbHeight = 40.dp.toPx()
            // When trackHeight is smaller than the minimum thumb size, clamp to trackHeight to avoid invalid range
            val thumbHeight = if (trackHeight < minThumbHeight) {
                trackHeight
            } else {
                ((visibleItemCount / totalItemsCount) * trackHeight).coerceIn(minThumbHeight, trackHeight)
            }

            // 3️⃣ Compute scrollbar thumb position based on scroll progress
            val firstVisibleItem = lazyListState.firstVisibleItemIndex
            val firstItemOffset = lazyListState.firstVisibleItemScrollOffset

            // Estimate total scrollable distance
            val averageItemHeight = visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItemsInfo.size
            val maxScrollOffset = (totalItemsCount - visibleItemCount) * averageItemHeight
            val scrolledOffset = (firstVisibleItem * averageItemHeight) + firstItemOffset

            // Compute scrollbar thumb position and update animated target
            val availableOffset = (trackHeight - thumbHeight).coerceAtLeast(0f)
            val normalizedScroll = if (maxScrollOffset > 0) (scrolledOffset / maxScrollOffset) else 0f
            targetScrollBarOffset = (normalizedScroll * availableOffset)
                .coerceIn(0f, availableOffset)

            // 4️⃣ Draw the scrollbar track
            if (showScrollBarTrack) {
                drawRoundRect(
                    color = scrollBarTrackColor.copy(alpha = if (fixed) fixedalpha else alpha),
                    cornerRadius = CornerRadius(scrollBarCornerRadius),
                    topLeft = Offset(size.width - endPadding, topPaddingPx),
                    size = Size(width.toPx(), trackHeight)
                )
            }

            // 5️⃣ Draw the scrollbar thumb
            drawRoundRect(
                color = scrollBarColor.copy(alpha = if (fixed) fixedalpha else alpha),
                cornerRadius = CornerRadius(scrollBarCornerRadius),
                topLeft = Offset(size.width - endPadding, topPaddingPx + targetScrollBarOffset),
                size = Size(width.toPx(), thumbHeight)
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

