package ru.blays.hub.core.logic.data.models

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Stable
import ru.blays.hub.core.packageManager.utils.signatureHash
import ru.blays.hub.core.packageManager.utils.versionCodeLong
import java.io.File

@Stable
data class ApkFile(
    val name: String,
    val apkInfo: ApkInfo? = null,
    val sizeString: String,
    val dateString: String,
    internal val file: File
)

@Stable
data class ApkInfo(
    val icon: Drawable,
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val signatureHash: String?
) {
    companion object {
        fun fromPackageInfo(
            packageInfo: PackageInfo,
            packageManager: PackageManager
        ): ApkInfo {
            return ApkInfo(
                icon = packageInfo.applicationInfo.loadIcon(packageManager),
                name = packageInfo.applicationInfo.name,
                packageName = packageInfo.packageName,
                versionName = packageInfo.versionName,
                versionCode = packageInfo.versionCodeLong,
                signatureHash = packageInfo.signatureHash
            )
        }
    }
}
