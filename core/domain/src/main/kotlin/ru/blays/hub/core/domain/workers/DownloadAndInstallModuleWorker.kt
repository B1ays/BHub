package ru.blays.hub.core.domain.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.EXTRA_STATUS_MESSAGE
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.content.getSystemService
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import ru.blays.hub.core.domain.ACTION_MODULE_INSTALL
import ru.blays.hub.core.domain.FLAG_REINSTALL
import ru.blays.hub.core.domain.PackageManagerResolver
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.utils.collectWhile
import ru.blays.hub.core.domain.utils.launchSelfIntent
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.downloader.repository.DownloadsRepository
import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.moduleManager.InstallRequest
import ru.blays.hub.core.moduleManager.InstallStatus
import ru.blays.hub.core.moduleManager.ModuleManager
import ru.blays.hub.core.packageManager.api.EXTRA_ACTION_SUCCESS
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.hub.utils.workerdsl.oneTimeWorkRequest
import ru.blays.hub.utils.workerdsl.workData
import java.io.File
import kotlin.random.Random

class DownloadAndInstallModuleWorker(
    appContext: Context,
    params: WorkerParameters,
    private val downloadRepository: DownloadsRepository,
    private val moduleManager: ModuleManager,
    private val workManager: WorkManager,
    private val packageManagerResolver: PackageManagerResolver,
) : CoroutineWorker(appContext, params), KoinComponent {
    private val notificationManager = applicationContext.getSystemService<NotificationManager>()!!

    private var workerNotificationID: Int = -1
    private var resultNotificationID: Int = -1

    override suspend fun doWork(): Result {
        val modApkDownloadRequestJson = inputData.getString(MOD_APK_DOWNLOAD_REQUEST_KEY)
            ?: return Result.failure()
        val origApkDownloadRequestJson = inputData.getString(ORIGINAL_APK_DOWNLOAD_REQUEST_KEY)
            ?: return Result.failure()
        val modApkDownloadRequest: DownloadRequest =
            Json.decodeFromString(modApkDownloadRequestJson)
        val origApkDownloadRequest: DownloadRequest =
            Json.decodeFromString(origApkDownloadRequestJson)

        val packageName = inputData.getString(PACKAGE_NAME_KEY) ?: return Result.failure()

        val flags: IntArray = inputData.getIntArray(FLAGS_KEY) ?: return Result.failure()

        val packageManager = packageManagerResolver.getPackageManager(PackageManagerType.Root)

        workerNotificationID = modApkDownloadRequest.hashCode()
        resultNotificationID = workerNotificationID + Random.nextInt()

        createForegroundInfo(
            title = applicationContext.getString(R.string.module_creating_formatted, packageName),
            content = applicationContext.getString(R.string.initializing)
        ).let {
            setForeground(it)
        }

        val modApkDownloadStatus = downloadRepository.download(modApkDownloadRequest)
        val origApkDownloadStatus = downloadRepository.download(origApkDownloadRequest)

        val downloadStatus: Flow<DownloadStatus> = combine(
            flow = modApkDownloadStatus,
            flow2 = origApkDownloadStatus,
            transform = ::transformFlow
        )

        var downloadSuccess = false

        downloadStatus.collectWhile { status ->
            when (status) {
                DownloadStatus.AllSuccess -> {
                    createNotification(
                        title = applicationContext.getString(
                            R.string.module_creating_formatted,
                            packageName
                        ),
                        content = applicationContext.getString(R.string.creatingModule),
                        intermediateProgress = true
                    ).also { notification ->
                        notificationManager.notify(
                            workerNotificationID,
                            notification
                        )
                    }
                    downloadSuccess = true
                    false
                }

                DownloadStatus.Failed -> {
                    createNotification(
                        title = applicationContext.getString(
                            R.string.module_creating_formatted,
                            packageName
                        ),
                        content = applicationContext.getString(R.string.creatingModule_error),
                        ongoing = false
                    ).also { notification ->
                        notificationManager.notify(
                            resultNotificationID,
                            notification
                        )
                    }
                    downloadSuccess = false
                    false
                }

                DownloadStatus.Running -> {
                    createNotification(
                        title = applicationContext.getString(
                            R.string.module_creating_formatted,
                            packageName
                        ),
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
            }
        }

        if (!downloadSuccess) return Result.failure()

        val originalApkFile = File(origApkDownloadRequest.filePath)

        val request = InstallRequest.createFor(
            packageName = packageName,
            filePath = modApkDownloadRequest.filePath
        )

        if(request == null) return Result.failure()

        val installSuccess = packageManager.installModule(
            request = request,
            originalApk = originalApkFile,
            reinstall = flags.contains(FLAG_REINSTALL)
        )

        createNotification(
            title = applicationContext.getString(
                R.string.module_creating_formatted,
                request.packageName
            ),
            content = if (installSuccess) {
                applicationContext.getString(R.string.creatingModule_success)
            } else {
                applicationContext.getString(R.string.creatingModule_error)
            },
            ongoing = false,
            autoCancel = true,
            contentIntent = applicationContext.launchSelfIntent,
        ).also { notification ->
            notificationManager.notify(
                resultNotificationID,
                notification
            )
        }

        Logger.d(TAG, "Module creating succeed: $installSuccess")

        return if (installSuccess) Result.success() else Result.failure()
    }

    private suspend fun PackageManager.installModule(
        request: InstallRequest,
        originalApk: File,
        reinstall: Boolean
    ): Boolean {
        if (reinstall) {
            uninstallApp(request.packageName)
        }

        val installResult = installApp(originalApk)
        Logger.d(TAG, "Install success: $installResult")
        if (installResult.isError) return false
        val statusFlow = moduleManager.install(request)
        val result = statusFlow.first { status ->
            status is InstallStatus.Success || status is InstallStatus.Failed
        }
        val success = result is InstallStatus.Success
        val broadcastIntent = Intent(ACTION_MODULE_INSTALL).apply {
            putExtra(EXTRA_ACTION_SUCCESS, success)
            if (result is InstallStatus.Failed) {
                putExtra(EXTRA_STATUS_MESSAGE, result.error.message)
            }
        }
        applicationContext.sendBroadcast(broadcastIntent)
        return success
    }

    private fun transformFlow(
        value1: WorkInfo,
        value2: WorkInfo
    ): DownloadStatus {
        return when {
            value1.state == WorkInfo.State.SUCCEEDED && value2.state == WorkInfo.State.SUCCEEDED -> {
                Logger.d(TAG, "All download succeeded")
                DownloadStatus.AllSuccess
            }

            value1.state == WorkInfo.State.FAILED || value2.state == WorkInfo.State.FAILED -> {
                Logger.d(TAG, "One of downloads failed")
                cancelIfActive(value1, value2)
                DownloadStatus.Failed
            }

            value1.state == WorkInfo.State.CANCELLED || value2.state == WorkInfo.State.CANCELLED -> {
                Logger.d(TAG, "One of downloads cancelled")
                cancelIfActive(value1, value2)
                DownloadStatus.Failed
            }

            value1.state == WorkInfo.State.BLOCKED || value2.state == WorkInfo.State.BLOCKED -> {
                Logger.d(TAG, "One of downloads blocked")
                cancelIfActive(value1, value2)
                DownloadStatus.Failed
            }

            else -> {
                DownloadStatus.Running
            }
        }
    }

    private fun cancelIfActive(workInfo: WorkInfo) {
        if (!workInfo.state.isFinished) {
            Logger.d(TAG, "Cancel work ${workInfo.id} because other is cancelled or blocked")
            workManager.cancelWorkById(workInfo.id)
        }
    }

    private fun cancelIfActive(vararg workInfo: WorkInfo) {
        workInfo.forEach(::cancelIfActive)
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
            ForegroundInfo(
                workerNotificationID,
                notification,
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
        autoCancel: Boolean = false,
        contentIntent: PendingIntent? = null,
    ): Notification {
        return Notification.Builder(applicationContext, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_process_small)
            setContentTitle(title)
            setContentText(content)
            setOngoing(ongoing)
            setAutoCancel(autoCancel)
            setContentIntent(contentIntent)
            setOnlyAlertOnce(true)
            setProgress(0, 0, intermediateProgress)
        }.build()
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.app_installer_notification_channel_name)
        val descriptionText =
            applicationContext.getString(R.string.app_installer_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "ModuleInstallWorker"

        private const val CHANNEL_ID = "app_installer"

        private const val MOD_APK_DOWNLOAD_REQUEST_KEY = "download_request_mod"
        private const val ORIGINAL_APK_DOWNLOAD_REQUEST_KEY = "download_request_orig"
        private const val PACKAGE_NAME_KEY = "package_name"
        private const val FLAGS_KEY = "flags"

        fun createRequest(
            modApkDownloadRequest: DownloadRequest,
            originalApkDownloadRequest: DownloadRequest,
            packageName: String,
            vararg flags: Int
        ): OneTimeWorkRequest {
            return oneTimeWorkRequest<DownloadAndInstallModuleWorker> {
                setInputData(
                    inputData = workData {
                        putString(
                            MOD_APK_DOWNLOAD_REQUEST_KEY,
                            Json.encodeToString(modApkDownloadRequest)
                        )
                        putString(
                            ORIGINAL_APK_DOWNLOAD_REQUEST_KEY,
                            Json.encodeToString(originalApkDownloadRequest)
                        )
                        putString(PACKAGE_NAME_KEY, packageName)
                        putIntArray(FLAGS_KEY, flags)
                    }
                )
            }
        }
    }
}

private sealed class DownloadStatus {
    data object Running : DownloadStatus()
    data object AllSuccess : DownloadStatus()
    data object Failed : DownloadStatus()
}