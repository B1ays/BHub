package ru.blays.hub.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.LocalDynamicMaterialThemeSeed
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import ru.blays.hub.core.preferences.proto.ThemeSettings
import ru.blays.hub.core.preferences.proto.ThemeType
import ru.blays.hub.core.ui.values.LocalDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
@Composable
fun BHubTheme(
    themeSettings: ThemeSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when(themeSettings.themeType) {
        ThemeType.SYSTEM -> isSystemInDarkTheme
        ThemeType.DARK -> true
        ThemeType.LIGHT -> false
        ThemeType.UNRECOGNIZED -> isSystemInDarkTheme
    }

    val primaryColor = remember(themeSettings.accentColorIndex) {
        defaultAccentColors.getOrElse(themeSettings.accentColorIndex) {
            defaultAccentColors[1]
        }
    }

    val animationSpec: AnimationSpec<Color> = spring(stiffness = 300F, dampingRatio = .6F)

    val colorScheme by remember(
        primaryColor,
        darkTheme,
        themeSettings.amoledTheme,
        themeSettings.monetColors
    ) {
        derivedStateOf {
            val scheme = when {
                themeSettings.monetColors && darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    dynamicDarkColorScheme(context)
                }
                themeSettings.monetColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    dynamicLightColorScheme(context)
                }
                else -> dynamicColorScheme(
                    seedColor = primaryColor,
                    isDark = darkTheme,
                    style = PaletteStyle.Content,
                    isAmoled = false
                )
            }
            scheme.let {
                if(themeSettings.amoledTheme && darkTheme) it.copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color.Black,
                    surfaceContainer = Color.Black,
                    surfaceTint = Color.Black
                ) else it
            }
        }
    }

    val animatedColorScheme = ColorScheme(
        primary = colorScheme.primary.animate(animationSpec),
        primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
        secondary = colorScheme.secondary.animate(animationSpec),
        secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
        tertiary = colorScheme.tertiary.animate(animationSpec),
        tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
        background = colorScheme.background.animate(animationSpec),
        surface = colorScheme.surface.animate(animationSpec),
        surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
        surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
        surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
        surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
        surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
        surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
        surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
        surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec),
        surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
        error = colorScheme.error.animate(animationSpec),
        errorContainer = colorScheme.errorContainer.animate(animationSpec),
        onPrimary = colorScheme.onPrimary.animate(animationSpec),
        onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
        onSecondary = colorScheme.onSecondary.animate(animationSpec),
        onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
        onTertiary = colorScheme.onTertiary.animate(animationSpec),
        onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
        onBackground = colorScheme.onBackground.animate(animationSpec),
        onSurface = colorScheme.onSurface.animate(animationSpec),
        onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
        onError = colorScheme.onError.animate(animationSpec),
        onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
        inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
        inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
        inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
        outline = colorScheme.outline.animate(animationSpec),
        outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
        scrim = colorScheme.scrim.animate(animationSpec),
    )

    val rippleConfiguration = remember(animatedColorScheme.primary) {
        expressiveRippleConfiguration(
            primaryColor = animatedColorScheme.primary,
            darkTheme = darkTheme
        )
    }

    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    val insetsController = remember(window) {
        window?.let { window ->
            WindowCompat.getInsetsController(window, view)
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            window?.navigationBarColor = android.graphics.Color.TRANSPARENT
            window?.statusBarColor = android.graphics.Color.TRANSPARENT
            /*animatedColorScheme.background.toArgb().let { backgroundColor ->
                window?.navigationBarColor = backgroundColor
                window?.statusBarColor = backgroundColor
            }*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window?.isNavigationBarContrastEnforced = false
            }
            insetsController?.isAppearanceLightStatusBars = !darkTheme
            insetsController?.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = animatedColorScheme,
        content = {
            CompositionLocalProvider(
                LocalRippleConfiguration provides rippleConfiguration,
                LocalDynamicMaterialThemeSeed provides primaryColor,
                LocalDarkTheme provides darkTheme,
                content = content
            )
        },
        typography = Typography
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun expressiveRippleConfiguration(
    primaryColor: Color,
    darkTheme: Boolean
) = RippleConfiguration(
    color = primaryColor,
    rippleAlpha = when {
        darkTheme -> DarkThemeRippleAlpha
        else -> {
            if (primaryColor.luminance() > 0.5) {
                LightThemeHighContrastRippleAlpha
            } else {
                LightThemeLowContrastRippleAlpha
            }
        }
    }
)

@OptIn(ExperimentalMaterial3Api::class)
val disableRippleConfig: RippleConfiguration? = null

private val LightThemeHighContrastRippleAlpha = RippleAlpha(
    pressedAlpha = 0.64f,
    focusedAlpha = 0.64f,
    draggedAlpha = 0.56f,
    hoveredAlpha = 0.48f
)

private val LightThemeLowContrastRippleAlpha = RippleAlpha(
    pressedAlpha = 0.52f,
    focusedAlpha = 0.52f,
    draggedAlpha = 0.48f,
    hoveredAlpha = 0.44f
)

private val DarkThemeRippleAlpha = RippleAlpha(
    pressedAlpha = 0.60f,
    focusedAlpha = 0.62f,
    draggedAlpha = 0.58f,
    hoveredAlpha = 0.54f
)

@Suppress("AnimateAsStateLabel")
@Composable
internal fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec).value
}