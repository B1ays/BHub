package ru.blays.hub.core.ui.elements.gradientIcon

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun GradientIcon(
    painter: Painter,
    brush: Brush,
    contentDescription: String? = null,
    modifier: Modifier
) {
    Icon(
        modifier = Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = brush,
                        blendMode = BlendMode.SrcAtop
                    )
                }
            }
            .then(modifier),
        painter = painter,
        contentDescription = contentDescription,
    )
}

@Composable
fun GradientIcon(
    imageVector: ImageVector,
    brush: Brush,
    contentDescription: String? = null,
    modifier: Modifier
) = GradientIcon(
    painter = rememberVectorPainter(imageVector),
    brush = brush,
    contentDescription = contentDescription,
    modifier = modifier
)

