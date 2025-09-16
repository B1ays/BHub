package ru.blays.hub.core.packageManager.shizuku

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerType

@OptIn(KoinExperimentalAPI::class)
val shizukuPMModule = module {
    singleOf(::PackageManagerImpl) {
        qualifier = named(PackageManagerType.Shizuku)
    } bind PackageManager::class
}