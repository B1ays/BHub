package ru.blays.hub.core.ui.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

fun ColorScheme.surfaceColorAtAlpha(
    alpha: Float,
): Color = surfaceTint.copy(alpha = alpha).compositeOver(surface)

fun ColorScheme.primaryColorAtAlpha(
    alpha: Float,
): Color = primary.copy(alpha = alpha).compositeOver(surface)

fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha)

@Composable
fun Color.withElevation(
    elevation: Dp,
    background: Color = MaterialTheme.colorScheme.surface
): Color {
    if (elevation == 0.dp) return this
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return this.copy(alpha = alpha).compositeOver(background)
}

internal fun ColorScheme.print() {
    println(
        "ColorScheme(" +
        "   primary=${primary.toHexString()}\n" +
        "   onPrimary=${onPrimary.toHexString()}\n" +
        "   primaryContainer=${primaryContainer.toHexString()}\n" +
        "   onPrimaryContainer=${onPrimaryContainer.toHexString()}\n" +
        "   inversePrimary=${inversePrimary.toHexString()}\n" +
        "   secondary=${secondary.toHexString()}\n" +
        "   onSecondary=${onSecondary.toHexString()}\n" +
        "   secondaryContainer=${secondaryContainer.toHexString()}\n" +
        "   onSecondaryContainer=${onSecondaryContainer.toHexString()}\n" +
        "   tertiary=${tertiary.toHexString()}\n" +
        "   onTertiary=${onTertiary.toHexString()}\n" +
        "   tertiaryContainer=${tertiaryContainer.toHexString()}\n" +
        "   onTertiaryContainer=${onTertiaryContainer.toHexString()}\n" +
        "   background=${background.toHexString()}\n" +
        "   onBackground=${onBackground.toHexString()}\n" +
        "   surface=${surface.toHexString()}\n" +
        "   onSurface=${onSurface.toHexString()}\n" +
        "   surfaceVariant=${surfaceVariant.toHexString()}\n" +
        "   onSurfaceVariant=${onSurfaceVariant.toHexString()}\n" +
        "   surfaceTint=${surfaceTint.toHexString()}\n" +
        "   inverseSurface=${inverseSurface.toHexString()}\n" +
        "   inverseOnSurface=${inverseOnSurface.toHexString()}\n" +
        "   error=${error.toHexString()}\n" +
        "   onError=${onError.toHexString()}\n" +
        "   errorContainer=${errorContainer.toHexString()}\n" +
        "   onErrorContainer=${onErrorContainer.toHexString()}\n" +
        "   outline=${outline.toHexString()}\n" +
        "   outlineVariant=${outlineVariant.toHexString()}\n" +
        "   scrim=${scrim.toHexString()}\n" +
        "   surfaceBright=${surfaceBright.toHexString()}\n" +
        "   surfaceDim=${surfaceDim.toHexString()}\n" +
        "   surfaceContainer=${surfaceContainer.toHexString()}\n" +
        "   surfaceContainerHigh=${surfaceContainerHigh.toHexString()}\n" +
        "   surfaceContainerHighest=${surfaceContainerHighest.toHexString()}\n" +
        "   surfaceContainerLow=${surfaceContainerLow.toHexString()}\n" +
        "   surfaceContainerLowest=${surfaceContainerLowest.toHexString()}\n" +
        ")"
    )
}

private fun Color.toHexString() = String.format("#%08X", (0xFFFFFF and toArgb()))

fun Color.toAndroidColor(): android.graphics.Color = android.graphics.Color.valueOf(
    red,
    green,
    blue,
    alpha
)
