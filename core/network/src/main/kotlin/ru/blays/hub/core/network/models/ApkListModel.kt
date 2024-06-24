package ru.blays.hub.core.network.models

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ``` json
 * {
 *   "Original apk url": "",
 *   "Files": [
 *     {
 *       "Apk name": "",
 *       "Short description": "",
 *       "Apk url": ""
 *     },
 *     {
 *       "Apk name": "",
 *       "Short description": "",
 *       "Apk url": ""
 *     }
 *   ]
 * }
 */

@Keep
@Serializable
data class ApkListModel(
    @SerialName("Original apk url")
    val originalApkUrl: String? = null,
    @SerialName("Files")
    val files: List<Apk>
) {
    @Keep
    @Serializable
    data class Apk(
        @SerialName("Apk name")
        val apkName: String,
        @SerialName("Short description")
        val shortDescription: String,
        @SerialName("Apk url")
        val url: String
    )
}