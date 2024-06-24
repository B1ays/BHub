package ru.blays.hub.core.packageManager.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Keep
import java.io.File
import java.security.MessageDigest

typealias SignatureHash = String?

fun Context.packageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }
}

fun Context.getPackageInfo(
    file: File,
    getSignatures: Boolean = true
): PackageInfo? {
    val path = file.path
    return (
        packageManager.getPackageArchiveInfo(
            path,
            packageInfoFlags(getSignatures)
        ) ?: packageManager.getPackageArchiveInfo(
            path,
            packageInfoFlags(false)
        )
    )?.apply {
        applicationInfo.sourceDir = path
        applicationInfo.publicSourceDir = path
    }
}
fun Context.getPackageInfo(
    filePath: String,
    getSignatures: Boolean = true
): PackageInfo? {
    return (
        packageManager.getPackageArchiveInfo(
            filePath,
            packageInfoFlags(getSignatures)
        ) ?: packageManager.getPackageArchiveInfo(
            filePath,
            packageInfoFlags(false)
        )
    )?.apply {
        applicationInfo.sourceDir = filePath
        applicationInfo.publicSourceDir = filePath
    }
}

@Suppress("DEPRECATION")
private fun packageInfoFlags(getSignatures: Boolean): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    PackageManager.GET_ACTIVITIES or if(getSignatures) PackageManager.GET_SIGNING_CERTIFICATES else 0
} else {
    PackageManager.GET_ACTIVITIES or if(getSignatures) PackageManager.GET_SIGNATURES else 0
}

@Suppress("DEPRECATION")
val PackageInfo.signatureBytes: ByteArray?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()
    } else {
        signatures?.firstOrNull()?.toByteArray()
    }

@OptIn(ExperimentalStdlibApi::class)
val PackageInfo.signatureHash: SignatureHash
    get() {
        val signatureBytes = signatureBytes ?: return null
        val digest = signatureBytes.toDigest(DigestAlgorithm.`SHA-1`)
        return digest.toHexString()
    }

@Suppress("DEPRECATION")
inline val PackageInfo.versionCodeLong: Long
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        versionCode.toLong()
    }

private fun ByteArray.toDigest(algorithm: DigestAlgorithm): ByteArray {
    return MessageDigest.getInstance(algorithm.name).apply {
        update(this@toDigest)
    }.digest()
}

@Keep
private enum class DigestAlgorithm {
    `SHA-1`,
    `SHA-224`,
    `SHA-256`,
    `SHA-384`,
    `SHA-512`,
    MD5,
}