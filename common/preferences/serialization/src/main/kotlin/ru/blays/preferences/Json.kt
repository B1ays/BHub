package ru.blays.preferences

import kotlinx.serialization.json.Json

/**
 * Default [Json] instance.
 */
@PublishedApi
internal val DefaultJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}