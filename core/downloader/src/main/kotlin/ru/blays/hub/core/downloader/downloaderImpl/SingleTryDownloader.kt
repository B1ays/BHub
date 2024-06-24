package ru.blays.hub.core.downloader.downloaderImpl

import androidx.work.ListenableWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import okhttp3.internal.closeQuietly
import ru.blays.hub.core.downloader.FileUpdateMode
import ru.blays.hub.core.downloader.LoggerAdapter
import ru.blays.hub.core.downloader.utils.rethrowCancellationException
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun OkHttpClient.singleTryDownloader(
    logger: LoggerAdapter? = null,
    file: File,
    url: String,
    fileUpdateMode: FileUpdateMode,
    onProgressChange: (Float) -> Unit
): ListenableWorker.Result = coroutineScope {
    val tempFile = File(file.absolutePath + ".tmp").apply {
        parentFile?.mkdirs()
        if(!exists()) createNewFile()
    }

    try {
        val request = Request.Builder()
            .url(url)
            .build()
        val response = newCall(request).executeAsync()

        if(!response.isSuccessful) {
            logger?.e("SingleTryDownloader: Response not successful: ${response.code} ${response.message}")
            ListenableWorker.Result.failure()
        }

        val contentLength = response.body.contentLength()
        val fileLength = file.length()

        if(
            contentLength == fileLength &&
            fileUpdateMode == FileUpdateMode.SkipIfExists
        ) {
            return@coroutineScope ListenableWorker.Result.success()
        }

        val inputStream = response.body.byteStream()
        val outputStream = tempFile.outputStream()

        var bytesCopied: Long = 0

        inputStream.use {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var readedBytes = it.read(buffer)
            while (readedBytes >= 0) {
                ensureActive()
                outputStream.write(buffer, 0, readedBytes)
                bytesCopied += readedBytes

                onProgressChange(
                    bytesCopied / contentLength.toFloat()
                )

                readedBytes = it.read(buffer)
            }
        }

        logger?.d("SingleTryDownloader: Download end")

        outputStream.closeQuietly()
        Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)

        return@coroutineScope ListenableWorker.Result.success()
    } catch (e: Exception) {
        logger?.e(e)
        rethrowCancellationException(e) {
            tempFile.delete()
        }
        return@coroutineScope ListenableWorker.Result.failure()
    } finally {
        tempFile.delete()
    }
}