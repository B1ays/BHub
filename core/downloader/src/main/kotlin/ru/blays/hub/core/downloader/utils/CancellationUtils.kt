package ru.blays.hub.core.downloader.utils

import kotlinx.coroutines.CancellationException

internal fun rethrowCancellationException(
    throwable: Throwable,
    onCancellation: (() -> Unit)? = null
) {
    if (throwable is CancellationException) {
        onCancellation?.invoke()
        throw throwable
    }
}