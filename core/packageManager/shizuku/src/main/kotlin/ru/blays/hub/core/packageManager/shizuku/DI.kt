package ru.blays.hub.core.packageManager.shizuku

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import ru.blays.hub.core.packageManager.PackageManager
import ru.blays.hub.core.packageManager.PackageManagerType

@OptIn(KoinExperimentalAPI::class)
val shizukuPMModule = lazyModule {
    singleOf(::PackageManagerImpl) {
        qualifier = named(PackageManagerType.Shizuku)
    } bind PackageManager::class
}