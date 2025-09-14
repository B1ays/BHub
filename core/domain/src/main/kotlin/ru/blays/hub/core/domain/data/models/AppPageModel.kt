package ru.blays.hub.core.domain.data.models

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import kotlinx.serialization.Serializable

@Stable
data class AppPageModel(
    val nonRoot: AppType? = null,
    val root: AppType? = null
)

@Stable
data class AppType(
    val packageName: String,
    val installed: Boolean,
    val versionsList: PersistentList<AppVersionCard>
)

@Stable
@Serializable
data class AppVersionCard(
    val version: String,
    val patchesVersion: String? = null,
    val buildDate: String,
    val changelogHref: String? = null,
    val apkListHref: String
)

@Stable
data class ApkInfoCardModel(
    val name: String,
    val description: String,
    val url: String
)
