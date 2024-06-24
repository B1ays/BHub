package ru.blays.hub.core.network.models


import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ```json
 * {
 *     "Title": "",
 *     "Description": "",
 *     "Icon url": "",
 *     "NonRoot": {
 *         "Package name": "",
 *         "Catalog url": ""
 *     },
 *     "Root": {
 *         "Package name": "",
 *         "Catalog url": ""
 *     }
 * }
 **/
@Keep
@Serializable
data class AppModel(
    @SerialName("Title")
    val title: String,
    @SerialName("Description")
    val shortDescription: String,
    @SerialName("Info")
    val appInfo: AppInfoModel? = null,
    @SerialName("Icon href")
    val iconHref: String,
    @SerialName("NonRoot")
    val nonRoot: VersionType? = null,
    @SerialName("Root")
    val root: VersionType? = null
) {
    @Keep
    @Serializable
    data class VersionType(
        @SerialName("Package name")
        val packageName: String,
        @SerialName("Required packages")
        val requiredPackages: List<String>? = null,
        @SerialName("Catalog href")
        val catalogHref: String
    )
}

@Keep
@Serializable
data class AppInfoModel(
    @SerialName("Readme href")
    val readmeHref: String,
    @SerialName("Images")
    val images: List<String> = emptyList()
)