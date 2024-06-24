package ru.blays.hub.core.network.okHttpDsl

import okhttp3.FormBody
import okhttp3.MultipartBody
import okhttp3.RequestBody

internal inline fun formBody(
    block: FormBody.Builder.() -> Unit
): FormBody {
    return FormBody.Builder()
        .apply(block)
        .build()
}

internal inline fun multipartBody(
    block: MultipartBody.Builder.() -> Unit
): RequestBody {
    return MultipartBody.Builder()
        .apply(block)
        .build()
}

internal fun bodyPart(name: String, value: String): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name = name,
        value = value
    )
}