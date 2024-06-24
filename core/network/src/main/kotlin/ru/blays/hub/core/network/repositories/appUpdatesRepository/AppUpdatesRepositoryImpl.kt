package ru.blays.hub.core.network.repositories.appUpdatesRepository

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.json
import ru.blays.hub.core.network.models.UpdateInfoModel
import ru.blays.hub.core.network.okHttpDsl.get
import ru.blays.hub.core.network.okHttpDsl.stringOrThrow
import ru.blays.hub.core.network.utils.rethrowCancellationException

internal class AppUpdatesRepositoryImpl(
    private val client: OkHttpClient
): AppUpdatesRepository {
    override suspend fun getUpdateInfo(
        source: String
    ): NetworkResult<UpdateInfoModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                url(source)
            }
            val stringBody = response.body.stringOrThrow()
            val model: UpdateInfoModel = json.decodeFromString(stringBody)
            NetworkResult.success(model)
        } catch (e: Exception) {
            rethrowCancellationException(e)
            NetworkResult.failure(e)
        }
    }
}