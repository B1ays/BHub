package ru.blays.hub.core.downloader

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

@ConsistentCopyVisibility
@Keep
@Serializable
data class DownloadRequest internal constructor(
    val fileName: String,
    val url: String,
    val filePath: String,
    val downloadMode: DownloadMode,
    val fileUpdateMode: FileUpdateMode
) {
    data class Builder(
        private val url: String,
        private val fileName: String,
        private val filePath: String,
    ) {
        constructor(
            url: String,
            fileName: String,
            file: File
        ) : this(
            url,
            fileName,
            file.absolutePath
        )

        private var downloadMode: DownloadMode = DownloadMode.SingleTry
        private var fileUpdateMode: FileUpdateMode = FileUpdateMode.SkipIfExists

        fun downloadMode(downloadMode: DownloadMode) = apply { this.downloadMode = downloadMode }
        fun fileUpdateMode(fileUpdateMode: FileUpdateMode) = apply { this.fileUpdateMode = fileUpdateMode }

        fun build() = DownloadRequest(
            fileName = fileName,
            url = url,
            filePath = filePath,
            downloadMode = downloadMode,
            fileUpdateMode = fileUpdateMode,
        )
    }

    companion object {
        fun builder(
            url: String,
            fileName: String,
            file: File,
            block: (Builder.() -> Unit)? = null
        ): DownloadRequest = Builder(url, fileName, file)
            .apply { block?.invoke(this) }
            .build()
    }
}

@Serializable
sealed class DownloadMode {
    @Serializable
    data object SingleTry: DownloadMode()
    @Serializable
    data class MultipleTry(@SerialName("tries_count") val triesCount: Int): DownloadMode()
    @Serializable
    data object InfinityTry: DownloadMode()
}

enum class FileUpdateMode {
    SkipIfExists,
    Recreate;
}
