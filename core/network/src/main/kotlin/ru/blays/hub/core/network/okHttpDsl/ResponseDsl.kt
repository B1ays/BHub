@file:Suppress("RedundantSuspendModifier")
@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.blays.hub.core.network.okHttpDsl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.coroutines.executeAsync

internal suspend inline fun OkHttpClient.get(
    url: HttpUrl
): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .build()
    ).executeAsync()
}

internal suspend inline fun OkHttpClient.get(
    url: String
): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .build()
    ).executeAsync()
}

internal suspend inline fun OkHttpClient.get(
    block: Request.Builder.() -> Unit
): Response {
    return newCall(
        request = Request.Builder()
            .apply(block)
            .build()
    ).executeAsync()
}

internal suspend inline fun OkHttpClient.get(
    url: HttpUrl,
    block: Request.Builder.() -> Unit
): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .apply(block)
            .build()
    ).executeAsync()
}

internal suspend inline fun OkHttpClient.get(
    url: String,
    block: Request.Builder.() -> Unit
): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .apply(block)
            .build()
    ).executeAsync()
}

internal suspend fun OkHttpClient.post(
    url: HttpUrl,
    body: RequestBody
): Response {
    return newCall(
        request = Request.Builder()
            .url(url)
            .post(body)
            .build()
    ).executeAsync()
}

internal suspend inline fun OkHttpClient.post(
    body: RequestBody,
    block: Request.Builder.() -> Unit
): Response {
    return newCall(
        request = Request.Builder()
            .post(body)
            .apply(block)
            .build()
    ).executeAsync()
}