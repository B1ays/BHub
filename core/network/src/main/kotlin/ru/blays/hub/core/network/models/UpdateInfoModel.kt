package ru.blays.hub.core.network.models


import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class UpdateInfoModel(
    @SerialName("Version name")
    val versionName: String,
    @SerialName("Version code")
    val versionCode: Int,
    @SerialName("Build date")
    val buildDate: String,
    @SerialName("Changelog url")
    val changelogUrl: String,
    @SerialName("Apk url")
    val apkUrl: String
)