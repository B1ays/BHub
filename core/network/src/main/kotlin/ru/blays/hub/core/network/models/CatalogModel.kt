package ru.blays.hub.core.network.models


import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ```json
 * {
 *   "Version": "",
 *   "Patches version": "",
 *   "Build date": "",
 *   "Changelog url": "",
 *   "Apk list url": ""
 * }
 * ```
 **/
@Keep
@Serializable
data class CatalogModel(
    @SerialName("Apk list href")
    val apkListHref: String,
    @SerialName("Build date")
    val buildDate: String,
    @SerialName("Changelog href")
    val changelogHref: String,
    @SerialName("Patches version")
    val patchesVersion: String? = null,
    @SerialName("Version")
    val version: String
)