package ru.blays.hub.core.logic.utils

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

inline val String.isValidUrl: Boolean
    get() = toHttpUrlOrNull() != null