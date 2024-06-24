package ru.blays.hub.core.network

import kotlin.time.Duration

sealed class CacheUpdatePolicy {
    /** [maxAge] - in milliseconds*/
    data class IfOutOfDate(val maxAge: Duration): CacheUpdatePolicy()
    data object Always : CacheUpdatePolicy()
    data object Never : CacheUpdatePolicy()
}