package ru.blays.hub.core.network.repositories.appsRepository

import ru.blays.hub.core.network.CacheUpdatePolicy
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.ApkListModel
import ru.blays.hub.core.network.models.AppModel
import ru.blays.hub.core.network.models.CatalogModel

interface AppsRepository {
    suspend fun getApps(
        baseUrl: String,
        appsHref: String,
        cacheUpdatePolicy: CacheUpdatePolicy = CacheUpdatePolicy.Always
    ): NetworkResult<List<AppModel>>

    suspend fun getAppVersions(
        baseUrl: String,
        versionsHref: String,
        cacheUpdatePolicy: CacheUpdatePolicy = CacheUpdatePolicy.Always
    ): NetworkResult<List<CatalogModel>>

    suspend fun getApkList(
        baseUrl: String,
        catalogHref: String,
        cacheUpdatePolicy: CacheUpdatePolicy = CacheUpdatePolicy.Always
    ): NetworkResult<ApkListModel>
}