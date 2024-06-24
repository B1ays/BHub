package ru.blays.hub.core.network.okHttpDsl

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl


internal inline fun httpUrl(block: HttpUrl.Builder.() -> Unit): HttpUrl {
    return HttpUrl.Builder()
        .apply(block)
        .build()
}

internal inline fun httpUrl(
    baseUrl: HttpUrl,
    block: HttpUrl.Builder.() -> Unit
): HttpUrl {
    return baseUrl.newBuilder()
        .apply(block)
        .build()
}

internal inline fun httpUrl(
    baseUrl: String,
    block: HttpUrl.Builder.() -> Unit
): HttpUrl {
    return baseUrl.toHttpUrl()
        .newBuilder()
        .apply(block)
        .build()
}

internal fun HttpUrl.Builder.href(href: String): HttpUrl.Builder {
    val clearedHref = href.substringBefore('#').trim(' ', '/')
    return addPathSegments(clearedHref)
}

fun fullUrlString(baseUrl: String, href: String): String {
    return fullHttpUrl(baseUrl, href).toString()
}

fun fullUrlStringOrNull(baseUrl: String, href: String): String? {
    return runCatching {
        fullHttpUrl(baseUrl, href).toString()
    }.getOrNull()
}

fun fullHttpUrl(baseUrl: String, href: String): HttpUrl {
    return baseUrl.toHttpUrl()
        .newBuilder()
        .href(href)
        .build()
}