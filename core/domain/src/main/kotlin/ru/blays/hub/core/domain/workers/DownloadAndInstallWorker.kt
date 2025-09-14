package ru.blays.hub.core.domain.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.downloader.repository.DownloadsRepository
import ru.blays.hub.core.domain.FLAG_REINSTALL
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.utils.collectWhile
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerResult
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.hub.core.packageManager.api.injectPackageManager
import ru.blays.hub.utils.workerdsl.oneTimeWorkRequest
import ru.blays.hub.utils.workerdsl.workData
import java.io.File
import kotlin.random.Random

internal class DownloadAndInstallWorker(
    appContext: Context,
    params: WorkerParameters,
    private val downloadRepository: DownloadsRepository
) : CoroutineWorker(
    appContext,
    params
) {
    private val notificationManager = applicationContext.getSystemService<NotificationManager>() as NotificationManager

    private var workerNotificationID: Int = -1
    private var resultNotificationID: Int = -1

    override suspend fun doWork(): Result {
        val downloadRequestJson = inputData.getString(DOWNLOAD_REQUEST_KEY) ?: return Result.failure()
        val pmTypeName = inputData.getString(PM_TYPE_KEY) ?: return Result.failure()
        val pmType = PackageManagerType.valueOf(pmTypeName)
        val downloadRequest: DownloadRequest = Json.decodeFromString(downloadRequestJson)

        val flags = inputData.getIntArray(FLAGS_KEY) ?: return Result.failure()
        val packageName = inputData.getString(PACKAGE_NAME_KEY) ?: return Result.failure()

        val packageManager: PackageManager by getKoin().injectPackageManager(pmType)

        workerNotificationID = downloadRequest.hashCode()
        resultNotificationID = workerNotificationID + Random.nextInt()

        createForegroundInfo(
            title = applicationContext.getString(R.string.app_installing_formatted, downloadRequest.fileName),
            content = applicationContext.getString(R.string.initializing)
        ).let {
            setForeground(it)
        }

        val downloadInfoFlow = downloadRepository.download(downloadRequest)

        var success = false

        downloadInfoFlow.collectWhile { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.ENQUEUED -> {
                    Log.d(TAG, "Download worker enqueued")
                    true
                }
                WorkInfo.State.RUNNING -> {
                    Log.d(TAG, "Download worker running")
                    createNotification(
                        title = applicationContext.getString(R.string.app_installing_formatted, downloadRequest.fileName),
                        content = applicationContext.getString(R.string.awaitingDownloads),
                        intermediateProgress = true
                    ).also { notification ->
                        notificationManager.notify(
                            workerNotificationID,
                            notification
                        )
                    }
                    true
                }
                WorkInfo.State.SUCCEEDED -> {
                    Log.d(TAG, "Download worker succeeded")
                    createNotification(
                        title = applicationContext.getString(R.string.app_installing_formatted, downloadRequest.fileName),
                        content = applicationContext.getString(R.string.app_installing_startInstallation),
                    ).also { notification ->
                        notificationManager.notify(
                            workerNotificationID,
                            notification
                        )
                    }

                    if(FLAG_REINSTALL in flags) {
                        packageManager.uninstallApp(packageName)
                    }

                    val file = File(downloadRequest.filePath)
                    val installResult = packageManager.installApp(file)
                    success = when(installResult) {
                        is PackageManagerResult.Error -> false
                        is PackageManagerResult.Success -> true
                    }
                    val intent = if(success) {
                        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(packageName)
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            launchIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    } else {
                        null
                    }

                    createNotification(
                        title = applicationContext.getString(R.string.app_installing_formatted, downloadRequest.fileName),
                        content = if(success) {
                            applicationContext.getString(R.string.app_installing_success)
                        } else {
                            applicationContext.getString(R.string.app_installing_failed)
                        },
                        intermediateProgress = false,
                        ongoing = false,
                        intent = intent
                    ).also { notification ->
                        notificationManager.notify(
                            resultNotificationID,
                            notification
                        )
                    }
                    false
                }
                WorkInfo.State.FAILED,
                WorkInfo.State.BLOCKED,
                WorkInfo.State.CANCELLED -> {
                    Log.d(TAG, "Download worker failed or cancelled or blocked")
                    createNotification(
                        title = applicationContext.getString(R.string.app_installing_formatted, downloadRequest.fileName),
                        content = applicationContext.getString(R.string.creatingModule_downloadError),
                        ongoing = false
                    ).also { notification ->
                        notificationManager.notify(
                            resultNotificationID,
                            notification
                        )
                    }
                    success = false
                    false
                }
            }
        }
        return if(success) Result.success() else Result.failure()
    }

    private fun createForegroundInfo(
        title: String,
        content: String
    ): ForegroundInfo {
        createChannel()

        val notification: Notification = createNotification(
            title = title,
            content = content
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(workerNotificationID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(workerNotificationID, notification)
        }
    }

    private fun createNotification(
        title: String,
        content: String,
        intermediateProgress: Boolean = false,
        ongoing: Boolean = true,
        intent: PendingIntent? = null
    ): Notification {
        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_process_small)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(ongoing)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, intermediateProgress)
            .setContentIntent(intent)
            .build()
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.app_installer_notification_channel_name)
        val descriptionText =
            applicationContext.getString(R.string.app_installer_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        val notificationManager =
            applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "InstallAppWorker"

        private const val CHANNEL_ID = "app_installer"

        private const val DOWNLOAD_REQUEST_KEY = "download_request"
        private const val PM_TYPE_KEY = "pm_type"
        private const val PACKAGE_NAME_KEY = "package_name"
        private const val FLAGS_KEY = "flags"

        @Suppress("NOTHING_TO_INLINE")
        inline fun createRequest(
            downloadRequest: DownloadRequest,
            packageManagerType: PackageManagerType,
            packageName: String,
            vararg flags: Int,
        ): OneTimeWorkRequest {
            return oneTimeWorkRequest<DownloadAndInstallWorker> {
                setInputData(
                    inputData = workData {
                        putString(DOWNLOAD_REQUEST_KEY, Json.encodeToString(downloadRequest))
                        putString(PM_TYPE_KEY, packageManagerType.name)
                        putString(PACKAGE_NAME_KEY, packageName)
                        putIntArray(FLAGS_KEY, flags)
                    }
                )
            }
        }
    }
}