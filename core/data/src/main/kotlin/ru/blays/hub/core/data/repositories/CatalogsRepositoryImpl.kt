package ru.blays.hub.core.data.repositories

import kotlinx.coroutines.flow.Flow
import ru.blays.hub.core.data.AppDatabase
import ru.blays.hub.core.data.room.daos.CatalogsDao
import ru.blays.hub.core.data.room.entities.CatalogEntity

internal class CatalogsRepositoryImpl(
    private val dao: CatalogsDao
): CatalogsRepository {
    constructor(database: AppDatabase): this(
        dao = database.getCatalogsDao()
    )

    override suspend fun checkCatalogExists(url: String): Boolean {
        return dao.catalogExists(url)
    }

    override suspend fun addCatalog(entity: CatalogEntity): Long {
        return dao.insertCatalog(entity)
    }

    override suspend fun getCatalog(name: String): CatalogEntity? {
        return dao.getCatalogByName(name)
    }
    override suspend fun getCatalog(id: Long): CatalogEntity? {
        return dao.getCatalog(id)
    }

    override suspend fun getAllCatalogs(): List<CatalogEntity> {
        return dao.getAllCatalogs()
    }
    override suspend fun getAllEnabledCatalogs(): List<CatalogEntity> {
        return dao.getAllEnabledCatalogs()
    }

    override fun getCatalogsAsFlow(): Flow<List<CatalogEntity>> {
        return dao.getCatalogsAsFlow()
    }
    override fun getEnabledCatalogsAsFlow(): Flow<List<CatalogEntity>> {
        return dao.getEnabledCatalogsAsFlow()
    }

    override suspend fun updateCatalog(entity: CatalogEntity) {
        dao.updateCatalog(entity)
    }

    override suspend fun deleteCatalog(id: Long) {
        dao.deleteCatalog(id)
    }

    override suspend fun clearCatalogs() {
        dao.clearCatalogs()
    }
}