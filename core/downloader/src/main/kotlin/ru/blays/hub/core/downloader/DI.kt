package ru.blays.hub.core.downloader

import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import ru.blays.hub.core.downloader.repository.DownloadRepository
import ru.blays.hub.core.downloader.repository.DownloadsRepositoryImpl

@OptIn(KoinExperimentalAPI::class)
val downloaderModule = lazyModule {
    singleOf(::DownloadsRepositoryImpl) bind DownloadRepository::class
    workerOf(::DownloadWorker)
}