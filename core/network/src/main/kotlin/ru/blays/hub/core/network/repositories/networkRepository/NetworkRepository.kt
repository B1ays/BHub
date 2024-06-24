package ru.blays.hub.core.network.repositories.networkRepository

import ru.blays.hub.core.network.NetworkResult
import java.io.InputStream

interface NetworkRepository {
    suspend fun getString(url: String): NetworkResult<String>
    suspend fun openStream(url: String): NetworkResult<InputStream>
}