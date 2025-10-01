package ru.blays.hub.core.domain

import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerType

class PackageManagerResolver(
    private val nonRootPackageManager: PackageManager,
    private val rootPackageManager: PackageManager,
    private val shizukuPackageManager: PackageManager,
) {
    fun getPackageManager(type: PackageManagerType): PackageManager = when(type) {
        PackageManagerType.NonRoot -> nonRootPackageManager
        PackageManagerType.Root -> rootPackageManager
        PackageManagerType.Shizuku -> shizukuPackageManager
    }
}