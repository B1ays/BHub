package ru.blays.hub.core.downloader.utils

import okhttp3.OkHttpClient
import okhttp3.Request

fun OkHttpClient.getContentLengthOrThrow(url: String): Long {
    val request = Request.Builder()
        .url(url)
        .build()
    return newCall(request).execute().use { response ->
        response.body.contentLength()
    }
}

val Long.isNotZero: Boolean
    get() = this != 0L