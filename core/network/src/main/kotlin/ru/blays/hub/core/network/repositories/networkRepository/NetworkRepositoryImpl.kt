package ru.blays.hub.core.network.repositories.networkRepository

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.okHttpDsl.get
import ru.blays.hub.core.network.okHttpDsl.stringOrThrow
import ru.blays.hub.core.network.utils.rethrowCancellationException
import java.io.InputStream

internal class NetworkRepositoryImpl(
    private val client: OkHttpClient
): NetworkRepository {
    override suspend fun getString(url: String): NetworkResult<String> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                url(url)
            }
            val body = response.body.stringOrThrow()
            NetworkResult.success(body)
        } catch (e: Exception) {
            rethrowCancellationException(e)
            NetworkResult.failure(e)
        }
    }

    override suspend fun openStream(url: String): NetworkResult<InputStream> {
        return try {
            val response = client.get(url)
            if(!response.isSuccessful) return NetworkResult.failure(
                IllegalStateException("Response not successful, code: ${response.code}")
            )
            NetworkResult.success(
                response.body.byteStream()
            )
        } catch (e: Exception) {
            return NetworkResult.failure(e)
        }
    }
}