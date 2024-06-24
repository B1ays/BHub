package ru.blays.hub.core.ui.elements.progressIndicator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: () -> Float,
    gradientStart: Color,
    gradientEnd: Color,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularDeterminateTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
    gapSize: Dp = ProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
) {
    val coercedProgress = { progress().coerceIn(0f, 1f) }
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = strokeCap)
    }
    Canvas(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress(), 0f..1f)
            }
            .size(CircularIndicatorDiameter)
    ) {
        // Start at 12 o'clock
        val startAngle = 270f
        val sweep = coercedProgress() * 360f
        val adjustedGapSize = if (strokeCap == StrokeCap.Butt || size.height > size.width) {
            gapSize
        } else {
            gapSize + strokeWidth
        }
        val gapSizeSweep =
            (adjustedGapSize.value / (Math.PI * CircularIndicatorDiameter.value).toFloat()) * 360f

        drawCircularIndicator(
            startAngle + sweep + min(sweep, gapSizeSweep),
            360f - sweep - min(sweep, gapSizeSweep) * 2,
            SolidColor(trackColor),
            stroke
        )
        val brush = Brush.sweepGradient(
            colorStops = listOf(
                0.0f to gradientStart,
                sweep / 360 to gradientEnd,
            ).toTypedArray()
        )
        drawDeterminateCircularIndicator(startAngle, sweep, brush, stroke)
    }
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    brush: Brush,
    stroke: Stroke
) {
    // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
    // To do this we need to remove half the stroke width from the total diameter for both sides.
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    rotate(-98f) {
        drawArc(
            brush = brush,
            startAngle = startAngle + 98F,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(diameterOffset, diameterOffset),
            size = Size(arcDimen, arcDimen),
            style = stroke
        )
    }
}

private fun DrawScope.drawDeterminateCircularIndicator(
    startAngle: Float,
    sweep: Float,
    brush: Brush,
    stroke: Stroke
) = drawCircularIndicator(startAngle, sweep, brush, stroke)

private val CircularIndicatorDiameter = 48.dp