package ru.blays.hub.core.data.repositories

import kotlinx.coroutines.flow.Flow
import ru.blays.hub.core.data.room.entities.CatalogEntity

interface CatalogsRepository {
    suspend fun checkCatalogExists(url: String): Boolean

    suspend fun addCatalog(entity: CatalogEntity): Long

    suspend fun getCatalog(name: String): CatalogEntity?
    suspend fun getCatalog(id: Long): CatalogEntity?

    suspend fun getAllCatalogs(): List<CatalogEntity>
    suspend fun getAllEnabledCatalogs(): List<CatalogEntity>
    fun getCatalogsAsFlow(): Flow<List<CatalogEntity>>
    fun getEnabledCatalogsAsFlow(): Flow<List<CatalogEntity>>

    suspend fun updateCatalog(entity: CatalogEntity)

    suspend fun deleteCatalog(id: Long)

    suspend fun clearCatalogs()
}