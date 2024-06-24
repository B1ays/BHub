package ru.blays.hub.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.blays.hub.core.data.room.daos.CatalogsDao
import ru.blays.hub.core.data.room.entities.CatalogEntity

@Database(
    entities = [
        CatalogEntity::class
    ],
    version = 2
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getCatalogsDao(): CatalogsDao
}