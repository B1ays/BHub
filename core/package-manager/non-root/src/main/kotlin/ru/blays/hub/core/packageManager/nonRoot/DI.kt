package ru.blays.hub.core.packageManager.nonRoot

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerType

@OptIn(KoinExperimentalAPI::class)
val nonRootPMModule = lazyModule {
    factoryOf(::PackageManagerImpl) {
        qualifier = named(PackageManagerType.NonRoot)
    } bind PackageManager::class
}