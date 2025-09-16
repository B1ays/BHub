package ru.blays.hub.core.packageManager.api

import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.get

inline fun getPackageManager(type: PackageManagerType): PackageManager = // TODO: Remove in future
    get(PackageManager::class.java, named(type))