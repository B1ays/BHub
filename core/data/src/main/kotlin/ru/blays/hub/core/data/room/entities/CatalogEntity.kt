package ru.blays.hub.core.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalogs")
data class CatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("owner") val owner: String,
    @ColumnInfo("url") val url: String,
    @ColumnInfo("enabled") val enabled: Boolean,
)