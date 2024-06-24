package androidx.compose.material3

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp

/**
 * <a href="https://m3.material.io/components/divider/overview" class="external" target="_blank">Material Design divider</a>.
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Divider image](https://developer.android.com/images/reference/androidx/compose/material3/divider.png)
 *
 * @param modifier the [Modifier] to be applied to this divider line.
 * @param thickness thickness of this divider line. Using [Dp.Hairline] will produce a single pixel
 * divider regardless of screen density.
 * @param color color of this divider line.
 */
@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    pathEffect: PathEffect? = null,
    color: Color = DividerDefaults.color,
) = Canvas(modifier.fillMaxWidth().height(thickness)) {
    drawLine(
        color = color,
        strokeWidth = thickness.toPx(),
        pathEffect = pathEffect,
        start = Offset(0f, thickness.toPx() / 2),
        end = Offset(size.width, thickness.toPx() / 2),
    )
}

/**
 * <a href="https://m3.material.io/components/divider/overview" class="external" target="_blank">Material Design divider</a>.
 *
 * A divider is a thin line that groups content in lists and layouts.
 *
 * ![Divider image](https://developer.android.com/images/reference/androidx/compose/material3/divider.png)
 *
 * @param modifier the [Modifier] to be applied to this divider line.
 * @param thickness thickness of this divider line. Using [Dp.Hairline] will produce a single pixel
 * divider regardless of screen density.
 * @param color color of this divider line.
 */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    pathEffect: PathEffect? = null,
    color: Color = DividerDefaults.color,
) = Canvas(modifier.fillMaxHeight().width(thickness)) {
    drawLine(
        color = color,
        strokeWidth = thickness.toPx(),
        pathEffect = pathEffect,
        start = Offset(thickness.toPx() / 2, 0f),
        end = Offset(thickness.toPx() / 2, size.height),
    )
}