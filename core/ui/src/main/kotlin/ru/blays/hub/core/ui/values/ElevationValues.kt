package ru.blays.hub.core.ui.values

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private var cachedZeroElevation: CardElevation? = null
val CardDefaults.ZeroCardElevation: CardElevation
    @Composable get() = cachedZeroElevation
        ?: cardElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
        .also { cachedZeroElevation = it }