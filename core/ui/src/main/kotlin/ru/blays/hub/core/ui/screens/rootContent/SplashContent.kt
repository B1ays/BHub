package ru.blays.hub.core.ui.screens.rootContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.blays.hub.core.ui.elements.brush.largeRadialGradientBrush

@Composable
fun SplashContent(modifier: Modifier = Modifier) {
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.primary.copy(0.3F)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                largeRadialGradientBrush(
                    0F to color1,
                    0.95F to color2
                )
            )
    )
}
