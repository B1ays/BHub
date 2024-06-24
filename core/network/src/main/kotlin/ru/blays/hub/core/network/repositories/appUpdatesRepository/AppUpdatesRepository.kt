package ru.blays.hub.core.network.repositories.appUpdatesRepository

import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.UpdateInfoModel

interface AppUpdatesRepository {
    suspend fun getUpdateInfo(source: String): NetworkResult<UpdateInfoModel>
}