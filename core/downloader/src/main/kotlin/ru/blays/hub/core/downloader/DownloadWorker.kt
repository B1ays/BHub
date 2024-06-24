package ru.blays.hub.core.downloader

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import ru.blays.hub.core.downloader.downloaderImpl.infinityTryDownloader
import ru.blays.hub.core.downloader.downloaderImpl.multipleTryDownloader
import ru.blays.hub.core.downloader.downloaderImpl.singleTryDownloader
import java.io.File
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt

@Suppress("NOTHING_TO_INLINE")
internal class DownloadWorker(
    appContext: Context,
    params: WorkerParameters,
    private val client: OkHttpClient,
    private val logger: LoggerAdapter
) : CoroutineWorker(appContext, params) {
    private val notificationManager =
        applicationContext.getSystemService(NotificationManager::class.java)

    private val cancelTitle = applicationContext.getString(android.R.string.cancel)
    private val cancelIntent =
        WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

    private var notificationID = -1

    private var progressInt = 0
    private var progressFloat = 0F

    private var timer: Timer? = null

    override suspend fun doWork(): Result {
        val requestString = inputData.getString(REQUEST_KEY)
        val request: DownloadRequest? = requestString?.let(Json::decodeFromString)

        if (request == null) {
            logger.e("Unable to parse request from string: $requestString")
            return Result.failure()
        }

        val file = File(request.filePath)

        val onProgressChange: (Float) -> Unit = { progress ->
            progressInt = (progress * 100).roundToInt()
            progressFloat = progress
        }

        timer = fixedRateTimer("progressNotify", false, 0L, 1500) {
            if (isStopped) {
                timer?.cancel()
                notificationManager.cancel(notificationID)
                return@fixedRateTimer
            }
            setProgressAsync(createProgressData(progressFloat))
            createNotification(
                progress = progressInt,
                title = request.fileName,
                content = "$progressInt/100%"
            ).also { notification ->
                notificationManager.notify(notificationID, notification)
            }
        }

        notificationID = file.hashCode()

        createForegroundInfo(
            notification = createNotification(
                title = request.fileName,
                content = applicationContext.getString(R.string.downloading_start)
            )
        ).let { foregroundInfo ->
            setForeground(foregroundInfo)
        }

        logger.d("Download mode: ${request.downloadMode}")

        val result = when (
            val mode = request.downloadMode
        ) {
            DownloadMode.InfinityTry -> client.infinityTryDownloader(
                context = applicationContext,
                file = file,
                url = request.url,
                fileUpdateMode = request.fileUpdateMode,
                onProgressChange = onProgressChange
            )

            is DownloadMode.MultipleTry -> client.multipleTryDownloader(
                context = applicationContext,
                file = file,
                url = request.url,
                fileUpdateMode = request.fileUpdateMode,
                tryCount = mode.triesCount,
                onProgressChange = onProgressChange
            )

            DownloadMode.SingleTry -> client.singleTryDownloader(
                logger = logger,
                file = file,
                url = request.url,
                fileUpdateMode = request.fileUpdateMode,
                onProgressChange = onProgressChange
            )
        }

        notificationManager.cancel(notificationID)
        timer?.cancel()

        return result
    }

    private fun createNotification(
        progress: Int? = null,
        title: String,
        content: String
    ): Notification {
        return NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setContentTitle(title)
            setTicker(title)
            setContentText(content)
            setSmallIcon(R.drawable.ic_download_small)
            setOnlyAlertOnce(true)
            setOngoing(true)
            addAction(
                android.R.drawable.ic_delete,
                cancelTitle,
                cancelIntent
            )
            if (progress != null) {
                setProgress(100, progress, false)
            }
        }.build()
    }

    private fun createForegroundInfo(
        notification: Notification
    ): ForegroundInfo {
        createChannel()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                notificationID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationID, notification)
        }
    }

    private fun createChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                applicationContext.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private inline fun createProgressData(progress: Float): Data {
        return Data.Builder().putFloat(PROGRESS_VALUE_KEY, progress).build()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "downloader"

        const val PROGRESS_VALUE_KEY = "progressInt"
        const val REQUEST_KEY = "request"
    }
}