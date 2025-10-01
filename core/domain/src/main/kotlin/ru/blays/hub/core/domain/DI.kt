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
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.hub.core.data.dataModule
import ru.blays.hub.core.domain.components.AboutComponent
import ru.blays.hub.core.domain.components.InfoDialogComponent
import ru.blays.hub.core.domain.components.SelfUpdateComponent
import ru.blays.hub.core.domain.components.SetupComponent
import ru.blays.hub.core.domain.components.appPageComponents.AppComponent
import ru.blays.hub.core.domain.components.appPageComponents.AppDescriptionComponent
import ru.blays.hub.core.domain.components.appPageComponents.AppVersionsListComponent
import ru.blays.hub.core.domain.components.appPageComponents.VersionPageComponent
import ru.blays.hub.core.domain.components.appsComponent.AppsComponent
import ru.blays.hub.core.domain.components.appsComponent.AppsRootComponent
import ru.blays.hub.core.domain.components.downloadComponents.DownloadsListComponent
import ru.blays.hub.core.domain.components.downloadComponents.DownloadsMenuComponent
import ru.blays.hub.core.domain.components.preferencesComponents.MainPreferencesComponent
import ru.blays.hub.core.domain.components.preferencesComponents.PreferencesComponent
import ru.blays.hub.core.domain.components.preferencesComponents.PreferencesRootComponent
import ru.blays.hub.core.domain.components.preferencesComponents.SelfUpdatePreferencesComponent
import ru.blays.hub.core.domain.components.preferencesComponents.ThemePreferencesComponent
import ru.blays.hub.core.domain.components.preferencesComponents.catalogs.AddCatalogComponent
import ru.blays.hub.core.domain.components.preferencesComponents.catalogs.CatalogsPreferencesComponent
import ru.blays.hub.core.domain.components.preferencesComponents.developerMenu.DeveloperMenuComponent
import ru.blays.hub.core.domain.components.preferencesComponents.developerMenu.DeveloperMenuLogsComponent
import ru.blays.hub.core.domain.components.preferencesComponents.developerMenu.DeveloperMenuRootComponent
import ru.blays.hub.core.domain.components.rootComponents.DialogsComponent
import ru.blays.hub.core.domain.components.rootComponents.RootComponent
import ru.blays.hub.core.domain.components.rootComponents.ShizukuDialogComponent
import ru.blays.hub.core.domain.components.rootComponents.TabsComponent
import ru.blays.hub.core.domain.loggerAdapters.DownloaderLoggerAdapter
import ru.blays.hub.core.domain.loggerAdapters.LoggerInterceptor
import ru.blays.hub.core.domain.loggerAdapters.ModuleManagerLoggerAdapter
import ru.blays.hub.core.domain.loggerAdapters.PackageManagerLoggerAdapter
import ru.blays.hub.core.domain.workers.CheckAppsUpdatesWorker
import ru.blays.hub.core.domain.workers.DownloadAndInstallModuleWorker
import ru.blays.hub.core.domain.workers.DownloadAndInstallWorker
import ru.blays.hub.core.downloader.downloaderModule
import ru.blays.hub.core.moduleManager.moduleManagerModule
import ru.blays.hub.core.network.networkModule
import ru.blays.hub.core.packageManager.api.LoggerAdapter
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.hub.core.packageManager.nonRoot.nonRootPMModule
import ru.blays.hub.core.packageManager.root.rootPMModule
import ru.blays.hub.core.packageManager.shizuku.shizukuPMModule
import java.io.File

private val componentsModule = module {
    singleOf(RootComponent::Factory)
    singleOf(TabsComponent::Factory)
    singleOf(DialogsComponent::Factory)
    singleOf(SetupComponent::Factory)
    singleOf(InfoDialogComponent<*, *>::Factory)
    singleOf(ShizukuDialogComponent::Factory)
    singleOf(SelfUpdateComponent::Factory)
    singleOf(AppsRootComponent::Factory)
    singleOf(AppComponent::Factory)
    singleOf(AppDescriptionComponent::Factory)
    singleOf(AppVersionsListComponent::Factory)
    singleOf(VersionPageComponent::Factory)
    singleOf(AppsComponent::Factory)
    singleOf(PreferencesRootComponent::Factory)
    singleOf(PreferencesComponent::Factory)
    singleOf(DeveloperMenuRootComponent::Factory)
    singleOf(DeveloperMenuLogsComponent::Factory)
    singleOf(DeveloperMenuComponent::Factory)
    singleOf(CatalogsPreferencesComponent::Factory)
    singleOf(AddCatalogComponent::Factory)
    singleOf(MainPreferencesComponent::Factory)
    singleOf(SelfUpdatePreferencesComponent::Factory)
    singleOf(ThemePreferencesComponent::Factory)
    singleOf(AboutComponent::Factory)
    singleOf(DownloadsListComponent::Factory)
    singleOf(DownloadsMenuComponent::Factory)
}

private val workersModule = module {
    single { WorkManager.getInstance(androidApplication()) }
    workerOf(::DownloadAndInstallWorker)
    workerOf(::DownloadAndInstallModuleWorker)
    workerOf(::CheckAppsUpdatesWorker)
}

private val loggerAdaptersModule = module {
    singleOf(::ModuleManagerLoggerAdapter) bind ru.blays.hub.core.moduleManager.LoggerAdapter::class
    singleOf(::DownloaderLoggerAdapter) bind ru.blays.hub.core.downloader.LoggerAdapter::class
    singleOf(::PackageManagerLoggerAdapter) bind LoggerAdapter::class
    singleOf(::LoggerInterceptor) bind Interceptor::class
}

private val packageManagerModule = module {
    includes(
        nonRootPMModule,
        rootPMModule,
        shizukuPMModule,
    )
    single {
        PackageManagerResolver(
            nonRootPackageManager = get(named(PackageManagerType.NonRoot)),
            rootPackageManager = get(named(PackageManagerType.Root)),
            shizukuPackageManager = get(named(PackageManagerType.Shizuku)),
        )
    }
}

val domainModule = module {
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

    singleOf(::PreferencesValidator)

    includes(
        componentsModule,
        networkModule,
        packageManagerModule,
        moduleManagerModule,
        downloaderModule,
        workersModule,
        loggerAdaptersModule,
        dataModule
    )
}

