package ru.blays.hub.core.domain.data.models

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class AppUpdateInfoModel(
    val versionName: String,
    val versionCode: Int,
    val buildDate: String,
    val changelog: String,
    internal val apkUrl: String
)
