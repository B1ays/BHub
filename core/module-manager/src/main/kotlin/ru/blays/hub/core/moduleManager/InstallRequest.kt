package ru.blays.hub.core.moduleManager

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class InstallRequest internal constructor(
    val packageName: String,
    val filePath: String
) {
    companion object {
        fun createFor(
            packageName: String,
            filePath: String
        ): InstallRequest? {
            return if(!filePath.endsWith("apk")) {
                null
            } else {
                InstallRequest(packageName, filePath)
            }
        }
    }
}