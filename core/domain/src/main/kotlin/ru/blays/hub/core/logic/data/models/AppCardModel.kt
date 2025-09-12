package ru.blays.hub.core.logic.data.models

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import ru.blays.hub.core.network.models.AppInfoModel

@Stable
@Serializable
data class AppCardModel(
    val title: String,
    val description: String,
    val iconUrl: String,
    val versions: List<VersionType> = emptyList(),
    val updateAvailable: Boolean = false,
    internal val appInfo: AppInfoModel?,
    internal val sourceUrl: String
)

@Serializable
sealed class VersionType {
    @Serializable
    data class NonRoot(
        override val packageName: String,
        override val catalogHref: String,
        override val availableVersionName: String? = null,
        override val installedVersionName: String? = null
    ): VersionType()
    @Serializable
    data class Root(
        override val packageName: String,
        override val catalogHref: String,
        override val availableVersionName: String? = null,
        override val installedVersionName: String? = null
    ): VersionType()

    internal abstract val packageName: String
    internal abstract val catalogHref: String
    abstract val availableVersionName: String?
    abstract val installedVersionName: String?

    companion object {
        fun VersionType.copy(
            packageName: String? = null,
            catalogUrl: String? = null,
            availableVersionName: String? = null,
            installedVersionName: String? = null
        ) = when(this) {
            is NonRoot -> this.copy(
                packageName = packageName ?: this.packageName,
                catalogHref = catalogUrl ?: this.catalogHref,
                availableVersionName = availableVersionName ?: this.availableVersionName,
                installedVersionName = installedVersionName ?: this.installedVersionName
            )
            is Root -> this.copy(
                packageName = packageName ?: this.packageName,
                catalogHref = catalogUrl ?: this.catalogHref,
                availableVersionName = availableVersionName ?: this.availableVersionName,
                installedVersionName = installedVersionName ?: this.installedVersionName
            )
        }
    }
}