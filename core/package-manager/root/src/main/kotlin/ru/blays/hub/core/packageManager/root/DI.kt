package ru.blays.hub.core.packageManager.root

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerType

@OptIn(KoinExperimentalAPI::class)
val rootPMModule = lazyModule {
    singleOf(::PackageManagerImpl) {
        qualifier = named(PackageManagerType.Root)
    } bind PackageManager::class
}