package ru.blays.hub.core.network.utils

import kotlinx.coroutines.CancellationException

internal fun rethrowCancellationException(throwable: Throwable) {
    if (throwable is CancellationException) throw throwable
}