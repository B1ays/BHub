package ru.blays.hub.core.network

import kotlinx.serialization.json.Json

internal val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}