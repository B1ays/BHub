package ru.blays.hub.core.network

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import org.koin.dsl.module
import ru.blays.hub.core.network.repositories.appUpdatesRepository.AppUpdatesRepository
import ru.blays.hub.core.network.repositories.appUpdatesRepository.AppUpdatesRepositoryImpl
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepository
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepositoryImpl
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepositoryImpl

@OptIn(KoinExperimentalAPI::class)
val networkModule = module {
    singleOf(::AppsRepositoryImpl) bind AppsRepository::class
    singleOf(::NetworkRepositoryImpl) bind NetworkRepository::class
    singleOf(::AppUpdatesRepositoryImpl) bind AppUpdatesRepository::class
}