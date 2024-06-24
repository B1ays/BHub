package ru.blays.hub.core.ui.elements.bubbleTabBar

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object BubbleTabBarDefaults {
    @Composable
    fun tabColors() = MaterialTheme.colorScheme.bubbleTabColors

    @Composable
    fun tabColors(
        activeContainerColor: Color = Color.Unspecified,
        activeContentColor: Color = Color.Unspecified
    ) = MaterialTheme.colorScheme.bubbleTabColors.copy(
        activeContainerColor = activeContainerColor,
        activeContentColor = activeContentColor
    )
}

private val ColorScheme.bubbleTabColors
    get() = bubbleTabColorsCached
        ?.takeIf { it.first == primary }
        ?.second
        ?: BubbleTabColors(
            activeContainerColor = surfaceColorAtElevation(14.dp),
            activeContentColor = primary
        ).also {
            bubbleTabColorsCached = primary to it
        }

private var bubbleTabColorsCached: Pair<Color, BubbleTabColors>? = null