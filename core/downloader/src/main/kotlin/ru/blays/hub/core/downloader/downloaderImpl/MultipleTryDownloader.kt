package ru.blays.hub.core.downloader.downloaderImpl

import android.content.Context
import androidx.work.ListenableWorker.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import okhttp3.internal.closeQuietly
import ru.blays.hub.core.downloader.FileUpdateMode
import ru.blays.hub.core.downloader.LoggerAdapter
import ru.blays.hub.core.downloader.NetworkState
import ru.blays.hub.core.downloader.networkState
import ru.blays.hub.core.downloader.utils.moveTo
import ru.blays.hub.core.downloader.utils.rethrowCancellationException
import java.io.File
import java.nio.file.StandardCopyOption
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun OkHttpClient.multipleTryDownloader(
    logger: LoggerAdapter? = null,
    context: Context,
    file: File,
    url: String,
    fileUpdateMode: FileUpdateMode,
    tryCount: Int,
    onProgressChange: (Float) -> Unit
): Result = coroutineScope {
    val tempFile = File(file.absolutePath + ".tmp").apply {
        parentFile?.mkdirs()
        if(!exists()) createNewFile()
    }

    val networkStateFlow = context.networkState

    val outputStream = tempFile.outputStream()

    val fileLength = file.length()
    logger?.d("Existing file length: $fileLength")

    var contentLength: Long = Long.MAX_VALUE
    var bytesCopied: Long = 0
    var currentTry = 1

    do {
        ensureActive()
        if(currentTry > tryCount) {
            outputStream.closeQuietly()
            tempFile.delete()
            return@coroutineScope Result.failure()
        }
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=${bytesCopied}-")
                .build()
            val response = newCall(request).executeAsync()

            if(!response.isSuccessful) continue
            contentLength = response.body.contentLength()

            if(
                contentLength == fileLength &&
                fileUpdateMode == FileUpdateMode.SkipIfExists
            ) {
                return@coroutineScope Result.success()
            }

            response.body.byteStream().use { inputStream ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var readedBytes = inputStream.read(buffer)
                while (readedBytes >= 0) {
                    ensureActive()
                    outputStream.write(buffer, 0, readedBytes)
                    bytesCopied += readedBytes

                    onProgressChange(
                        bytesCopied / contentLength.toFloat()
                    )

                    readedBytes = inputStream.read(buffer)
                }
            }
        } catch (e: Exception) {
            rethrowCancellationException(e) {
                outputStream.closeQuietly()
                tempFile.delete()
            }
            currentTry++
            networkStateFlow.first { it == NetworkState.CONNECTED }
            delay(6.seconds)
        }
    } while (bytesCopied < contentLength)

    outputStream.closeQuietly()

    tempFile.moveTo(file, StandardCopyOption.REPLACE_EXISTING)
    tempFile.delete()

    return@coroutineScope Result.success()
}