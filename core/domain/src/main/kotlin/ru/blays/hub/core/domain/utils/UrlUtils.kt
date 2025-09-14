package ru.blays.hub.core.domain.utils

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

inline val String.isValidUrl: Boolean
    get() = toHttpUrlOrNull() != null