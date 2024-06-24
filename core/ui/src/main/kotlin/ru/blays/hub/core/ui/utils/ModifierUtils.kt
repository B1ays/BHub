package ru.blays.hub.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.thenIf(
    condition: Boolean,
    factory: @Composable Modifier.() -> Modifier
): Modifier {
    return if (condition) factory.invoke(this) else this
}