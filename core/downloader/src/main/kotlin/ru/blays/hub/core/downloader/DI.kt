package ru.blays.hub.core.downloader

import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.hub.core.downloader.repository.DownloadsRepository
import ru.blays.hub.core.downloader.repository.DownloadsRepositoryImpl

@OptIn(KoinExperimentalAPI::class)
val downloaderModule = module {
    singleOf(::DownloadsRepositoryImpl) bind DownloadsRepository::class
    workerOf(::DownloadWorker)
}