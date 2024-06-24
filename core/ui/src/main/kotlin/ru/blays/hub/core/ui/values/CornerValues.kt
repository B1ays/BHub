package ru.blays.hub.core.ui.values

import androidx.compose.foundation.shape.CornerSize

private var cachedZeroSize: CornerSize? = null
val ZeroCornerSize: CornerSize
    get() = cachedZeroSize
        ?: CornerSize(0f)
        .also { cachedZeroSize = it }