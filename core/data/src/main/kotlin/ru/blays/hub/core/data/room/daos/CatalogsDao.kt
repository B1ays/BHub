package ru.blays.hub.core.data.room.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.blays.hub.core.data.room.entities.CatalogEntity

@Dao
interface CatalogsDao {
    @Insert(entity = CatalogEntity::class)
    suspend fun insertCatalog(catalog: CatalogEntity): Long
    @Insert(entity = CatalogEntity::class)
    suspend fun insertCatalogs(catalogs: List<CatalogEntity>): List<Long>

    @Query("SELECT * FROM catalogs")
    suspend fun getAllCatalogs(): List<CatalogEntity>

    @Query("SELECT * FROM catalogs WHERE enabled = 1")
    suspend fun getAllEnabledCatalogs(): List<CatalogEntity>

    @Query("SELECT * FROM catalogs WHERE id = :id")
    suspend fun getCatalog(id: Long): CatalogEntity?

    @Query("SELECT * FROM catalogs WHERE name = :name")
    suspend fun getCatalogByName(name: String): CatalogEntity?

    @Update(entity = CatalogEntity::class)
    suspend fun updateCatalog(catalog: CatalogEntity)

    @Query("DELETE FROM catalogs WHERE id = :id")
    suspend fun deleteCatalog(id: Long)

    @Delete(entity = CatalogEntity::class)
    suspend fun deleteCatalog(catalog: CatalogEntity)

    @Query("DELETE FROM catalogs")
    suspend fun clearCatalogs()

    @Query("SELECT EXISTS(SELECT * FROM catalogs WHERE url = :url)")
    suspend fun catalogExists(url: String): Boolean

    @Query("SELECT * FROM catalogs")
    fun getCatalogsAsFlow(): Flow<List<CatalogEntity>>
    @Query("SELECT * FROM catalogs WHERE enabled = 1")
    fun getEnabledCatalogsAsFlow(): Flow<List<CatalogEntity>>
}