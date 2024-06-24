package ru.blays.hub.core.ui.elements.indicators

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun DotIndicator(
    modifier: Modifier = Modifier,
    size: Dp,
    color: Color,
    style: DrawStyle = Fill
) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(
            color = color,
            radius = when(style) {
                is Stroke -> (size.toPx() - style.width) / 2
                else -> size.toPx() / 2
            },
            style = style
        )
    }
}

@Composable
fun DotIndicator(
    modifier: Modifier = Modifier,
    size: Dp,
    brush: Brush,
    style: DrawStyle = Fill
) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = brush,
            radius = when(style) {
                is Stroke -> (size.toPx() - style.width) / 2
                else -> size.toPx() / 2
            },
            style = style
        )
    }
}