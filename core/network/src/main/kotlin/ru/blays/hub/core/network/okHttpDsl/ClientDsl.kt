package ru.blays.hub.core.network.okHttpDsl

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.scope.Scope

internal fun Module.okHttpClient(
    block: context(Scope, OkHttpClient.Builder) () -> Unit
) {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .apply { block(this@single, this) }
            .build()
    }
}

context(Scope, OkHttpClient.Builder)
internal fun addAllInterceptors() {
    getAll<Interceptor>().forEach(::addInterceptor)
}