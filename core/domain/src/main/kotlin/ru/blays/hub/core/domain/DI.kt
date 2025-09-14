@file:OptIn(KoinExperimentalAPI::class, KoinExperimentalAPI::class)

package ru.blays.hub.core.domain

import androidx.work.WorkManager
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.includes
import org.koin.dsl.bind
import org.koin.dsl.lazyModule
import org.koin.dsl.module
import ru.blays.hub.core.data.dataModule
import ru.blays.hub.core.downloader.downloaderModule
import ru.blays.hub.core.domain.loggerAdapters.DownloaderLoggerAdapter
import ru.blays.hub.core.domain.loggerAdapters.LoggerInterceptor
import ru.blays.hub.core.domain.loggerAdapters.ModuleManagerLoggerAdapter
import ru.blays.hub.core.domain.loggerAdapters.PackageManagerLoggerAdapter
import ru.blays.hub.core.domain.workers.CheckAppsUpdatesWorker
import ru.blays.hub.core.domain.workers.DownloadAndInstallModuleWorker
import ru.blays.hub.core.domain.workers.DownloadAndInstallWorker
import ru.blays.hub.core.moduleManager.moduleManagerModule
import ru.blays.hub.core.network.networkModule
import ru.blays.hub.core.packageManager.api.LoggerAdapter
import ru.blays.hub.core.packageManager.nonRoot.nonRootPMModule
import ru.blays.hub.core.packageManager.root.rootPMModule
import ru.blays.hub.core.packageManager.shizuku.shizukuPMModule
import ru.blays.hub.core.preferences.preferencesModule
import java.io.File

val workersModule = lazyModule {
    single { WorkManager.getInstance(androidApplication()) }
    workerOf(::DownloadAndInstallWorker)
    workerOf(::DownloadAndInstallModuleWorker)
    workerOf(::CheckAppsUpdatesWorker)
}

val loggerAdaptersModule = lazyModule {
    singleOf(::ModuleManagerLoggerAdapter) bind ru.blays.hub.core.moduleManager.LoggerAdapter::class
    singleOf(::DownloaderLoggerAdapter) bind ru.blays.hub.core.downloader.LoggerAdapter::class
    singleOf(::PackageManagerLoggerAdapter) bind LoggerAdapter::class
    singleOf(::LoggerInterceptor) bind Interceptor::class
}

val packageManagerModule = lazyModule {
    includes(
        nonRootPMModule,
        rootPMModule,
        shizukuPMModule,
    )
}

val coreModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder().apply {
            getAll<Interceptor>().forEach(::addInterceptor)
            retryOnConnectionFailure(false)
            cache(
                Cache(
                    directory = File(androidApplication().cacheDir, "http_cache"),
                    maxSize = 10L * 1024L * 1024L // 10 MiB
                )
            )
        }.build()
    }

    includes(
        networkModule,
        packageManagerModule,
        moduleManagerModule,
        downloaderModule,
        workersModule,
        preferencesModule,
        loggerAdaptersModule,
        dataModule
    )
}

