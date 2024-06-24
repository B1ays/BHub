package ru.blays.hub.core.ui.elements.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun JumpDotIndicator(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    dotSize: Dp = 10.dp,
    spacing: Dp = 10.dp,
) {
    Box(
        modifier = Modifier.padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pagerState.pageCount) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }
        }

        Box(
            Modifier
                .jumpingDotTransition(pagerState, 0.8F, spacing)
                .size(dotSize)
                .background(
                    color = activeColor,
                    shape = CircleShape,
                )
        )
    }
}

private fun Modifier.jumpingDotTransition(
    pagerState: PagerState,
    jumpScale: Float,
    spacing: Dp,
) = graphicsLayer {
    val pageOffset = pagerState.currentPageOffsetFraction
    val scrollPosition = pagerState.currentPage + pageOffset
    translationX = scrollPosition * (size.width + spacing.roundToPx()) // 8.dp - spacing between dots

    val scale: Float
    val targetScale = jumpScale - 1f

    scale = if (pageOffset.absoluteValue < 0.5) {
        1.0f + (pageOffset.absoluteValue * 2) * targetScale;
    } else {
        jumpScale + ((1 - (pageOffset.absoluteValue * 2)) * targetScale);
    }

    scaleX = scale
    scaleY = scale
}