package ru.blays.hub.core.domain.data.models

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import ru.blays.hub.core.data.room.entities.CatalogEntity

@Serializable
@Stable
data class CatalogModel(
    internal val id: Long,
    val name: String,
    val owner: String,
    val url: String,
    val enabled: Boolean
) {
    fun toDbEntity() = CatalogEntity(
        id = id,
        name = name,
        owner = owner,
        url = url,
        enabled = enabled
    )

    companion object {
        fun fromEntity(entity: CatalogEntity) = CatalogModel(
            id = entity.id,
            name = entity.name,
            owner = entity.owner,
            url = entity.url,
            enabled = entity.enabled
        )
    }
}
