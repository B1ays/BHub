package ru.blays.hub.core.domain.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import ru.blays.hub.core.data.repositories.CatalogsRepository
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.domain.APPS_HREF
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.utils.VersionName.Companion.toVersionName
import ru.blays.hub.core.domain.utils.launchSelfIntent
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepository
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.hub.core.packageManager.api.getPackageManager
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.utils.workerdsl.constraints
import ru.blays.hub.utils.workerdsl.periodicWorkRequest
import java.time.Duration
import kotlin.time.Duration.Companion.hours

class CheckAppsUpdatesWorker(
    appContext: Context,
    params: WorkerParameters,
    private val appsRepository: AppsRepository,
    private val catalogsRepository: CatalogsRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, params), KoinComponent {
    private val packageManager = getPackageManager(PackageManagerType.NonRoot)
    private val notificationManager = appContext.getSystemService<NotificationManager>()!!

    private val contentIntent = appContext.launchSelfIntent
    private val rootGranted = DeviceInfo.isRootGranted

    override suspend fun doWork(): Result {
        if(!settingsRepository.checkAppsUpdates) return Result.failure()

        val catalogs = catalogsRepository.getAllEnabledCatalogs()

        if (catalogs.isEmpty()) {
            return Result.failure()
        }

        createChannel()

        catalogs.forEach catalogsForEach@ { catalog ->
            val appsResult = appsRepository.getApps(
                baseUrl = catalog.url,
                appsHref = APPS_HREF
            )
            appsResult.getOrNull()?.forEach appsForEach@ { app ->
                val message = StringBuilder().apply {
                    appendLine(
                        applicationContext.getString(R.string.update_available_title)
                    )
                }
                var hasUpdates = false
                if (rootGranted) {
                    app.root?.let let@ { versionType ->
                        val version = appsRepository.getAppVersions(catalog.url, versionType.catalogHref)
                            .getOrNull()
                            ?.firstOrNull()
                            ?.version
                            ?: return@let

                        if (checkUpdates(version, versionType.packageName)) {
                            message.appendLine(
                                applicationContext.getString(
                                    R.string.root_version_formatted,
                                    version
                                )
                            )
                            hasUpdates = true
                        }
                    }
                }
                app.nonRoot?.let let@ { versionType ->
                    val version = appsRepository.getAppVersions(catalog.url, versionType.catalogHref)
                        .getOrNull()
                        ?.firstOrNull()
                        ?.version
                        ?: return@let

                    if(checkUpdates(version, versionType.packageName)) {
                        message.appendLine(
                            applicationContext.getString(R.string.nonRoot_version_formatted, version)
                        )
                        hasUpdates = true
                    }
                }
                if(hasUpdates) {
                    createNotification(
                        title = app.title,
                        text = message.toString()
                    ).let {
                        val id = "${catalog.name}-${app.title}".hashCode()
                        notificationManager.notify(id, it)
                    }
                }
            }
        }

        return Result.success()
    }

    private suspend fun checkUpdates(version: String, packageName: String): Boolean {
        val installedVersion = packageManager
            .getVersionName(packageName)
            .getValueOrNull()
            ?.toVersionName() ?: return false
        val availableVersion = version.toVersionName() ?: return false

        return installedVersion < availableVersion
    }

    private fun createNotification(
        title: String,
        text: String?,
        groupSummary: Boolean = false,
        clickable: Boolean = true,
        intermediateProgress: Boolean = false
    ): Notification {
        return Notification.Builder(applicationContext, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_app_small)
            setContentTitle(title)
            setContentText(text)
            if(clickable) {
                setContentIntent(contentIntent)
            }
            setGroup(GROUP_NAME)
            setAutoCancel(true)
            setGroupSummary(groupSummary)
            setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
            if(intermediateProgress) {
                setProgress(0, 0, true)
            }
        }.build()
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.checkUpdates_notificationChannel_name)
        val descriptions = applicationContext.getString(R.string.checkUpdates_notificationChannel_description)
        NotificationChannel(
            CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        ).let { channel ->
            channel.description = descriptions
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "check_apps_updates"
        private const val GROUP_NAME = "app_updates"

        const val WORK_NAME = "check_updates_work"

        fun createWorkRequest(interval: Int): PeriodicWorkRequest {
            val constraints = constraints {
                setRequiredNetworkType(NetworkType.CONNECTED)
                setRequiresBatteryNotLow(true)
            }
            return periodicWorkRequest<CheckAppsUpdatesWorker>(
                repeatInterval = interval.hours,
                flexTimeInterval = (interval - 1).hours
            ) {
                setConstraints(constraints)
                setInitialDelay(Duration.ofHours(1))
            }
        }
    }
}