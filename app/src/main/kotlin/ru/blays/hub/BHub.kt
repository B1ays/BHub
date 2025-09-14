package ru.blays.hub

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.lsposed.hiddenapibypass.HiddenApiBypass
import ru.blays.hub.core.domain.coreModule
import ru.blays.hub.core.domain.workers.CheckAppsUpdatesWorker
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.utils.coilDsl.crossfade
import ru.blays.hub.utils.coilDsl.diskCache
import ru.blays.hub.utils.coilDsl.imageLoader
import ru.blays.hub.utils.coilDsl.memoryCache
import java.io.File

class BHub : Application(), ImageLoaderFactory {
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate() {
        super.onCreate()

        //Shell.enableVerboseLogging = true
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER)
        )

        startKoin {
            androidContext(this@BHub)
            //analytics()
            modules(
                appModule,
                coreModule
            )
            workManagerFactory()
        }
        cancelPendingWorkManager()
        startUpdatesWork()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return imageLoader(this@BHub) {
            components {
                add(SvgDecoder.Factory())
            }
            memoryCache(this@BHub) {
                maxSizePercent(0.25)
            }
            diskCache(File(cacheDir, "image_cache"))
            crossfade = true
        }
    }

    @Suppress("DEPRECATION")
    private fun startUpdatesWork() {
        val workManager = WorkManager.getInstance(this)
        if (settingsRepository.checkAppsUpdates) {
            val workRequest = CheckAppsUpdatesWorker.createWorkRequest(
                settingsRepository.checkAppsUpdatesInterval
            )
            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = CheckAppsUpdatesWorker.WORK_NAME,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE,
                request = workRequest
            )
        } else {
            workManager.cancelUniqueWork(CheckAppsUpdatesWorker.WORK_NAME)
        }
    }

    /**
     * If there is a pending work because of previous crash we'd like it to not run.
     *
     */
    @SuppressLint("RestrictedApi")
    private fun cancelPendingWorkManager() {
        runBlocking {
            WorkManager.getInstance(this@BHub)
                .cancelAllWork()
                .result
                .get()
        }
    }
}