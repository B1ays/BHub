package ru.blays.hub.core.network.repositories.appsRepository

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.hub.core.network.CacheUpdatePolicy
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.json
import ru.blays.hub.core.network.models.ApkListModel
import ru.blays.hub.core.network.models.AppModel
import ru.blays.hub.core.network.models.CatalogModel
import ru.blays.hub.core.network.okHttpDsl.get
import ru.blays.hub.core.network.okHttpDsl.href
import ru.blays.hub.core.network.okHttpDsl.httpUrl
import ru.blays.hub.core.network.okHttpDsl.stringOrThrow
import ru.blays.hub.core.network.utils.rethrowCancellationException

internal class AppsRepositoryImpl(
    private val client: OkHttpClient
): AppsRepository {
    override suspend fun getApps(
        baseUrl: String,
        appsHref: String,
        cacheUpdatePolicy: CacheUpdatePolicy
    ): NetworkResult<List<AppModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                url(
                    httpUrl(baseUrl) {
                        href(appsHref)
                    }
                )
            }
            val body = response.body.stringOrThrow()
            val apps: List<AppModel> = json.decodeFromString(body)
            NetworkResult.success(apps)
        } catch (e: Exception) {
            rethrowCancellationException(e)
            NetworkResult.failure(e)
        }
    }

    override suspend fun getAppVersions(
        baseUrl: String,
        versionsHref: String,
        cacheUpdatePolicy: CacheUpdatePolicy
    ): NetworkResult<List<CatalogModel>> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                url(
                    httpUrl(baseUrl) {
                        href(versionsHref)
                    }
                )
            }
            val body = response.body.stringOrThrow()
            val apps: List<CatalogModel> = json.decodeFromString(body)
            NetworkResult.success(apps)
        } catch (e: Exception) {
            rethrowCancellationException(e)
            NetworkResult.failure(e)
        }
    }

    override suspend fun getApkList(
        baseUrl: String,
        catalogHref: String,
        cacheUpdatePolicy: CacheUpdatePolicy
    ): NetworkResult<ApkListModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                url(
                    httpUrl(baseUrl) {
                        href(catalogHref)
                    }
                )
            }
            val body = response.body.stringOrThrow()
            val apps: ApkListModel = json.decodeFromString(body)
            NetworkResult.success(apps)
        } catch (e: Exception) {
            rethrowCancellationException(e)
            NetworkResult.failure(e)
        }
    }
}