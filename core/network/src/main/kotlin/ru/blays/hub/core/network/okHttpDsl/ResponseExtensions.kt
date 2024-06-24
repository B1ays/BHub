package ru.blays.hub.core.network.okHttpDsl

import okhttp3.ResponseBody
import okio.IOException

internal fun ResponseBody?.stringOrThrow(): String {
    return this?.string() ?: throw IOException("No response body")
}