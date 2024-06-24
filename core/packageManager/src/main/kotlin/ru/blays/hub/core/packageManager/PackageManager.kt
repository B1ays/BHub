package ru.blays.hub.core.packageManager

import java.io.File


interface PackageManager {
    suspend fun getVersionCode(packageName: String): PackageManagerResult<Int>

    suspend fun getVersionName(packageName: String): PackageManagerResult<String>

    suspend fun checkPackageInstalled(packageName: String): Boolean

    suspend fun getInstallationDir(packageName: String): PackageManagerResult<String>

    suspend fun setInstaller(targetPackage: String, installerPackage: String): PackageManagerResult<Nothing>

    suspend fun forceStop(packageName: String): PackageManagerResult<Nothing>

    suspend fun installApp(apk: File): PackageManagerResult<Nothing>

    suspend fun installSplitApp(apks: Array<File>): PackageManagerResult<Nothing>

    suspend fun uninstallApp(packageName: String): PackageManagerResult<Nothing>

    suspend fun launchApp(packageName: String): PackageManagerResult<Nothing>
}