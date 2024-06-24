package ru.blays.hub.core.ui.values

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale

val LocalStackAnimator = staticCompositionLocalOf { fade() + scale() }

val LocalDarkTheme = compositionLocalOf<Boolean> { throw IllegalStateException("LocalDarkMode not provided") }