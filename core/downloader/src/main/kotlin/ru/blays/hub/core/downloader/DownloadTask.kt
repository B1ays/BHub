package ru.blays.hub.core.downloader

import kotlinx.coroutines.flow.Flow
import java.util.UUID

data class DownloadTask(
    internal val workId: UUID,
    val name: String,
    val progressFlow: Flow<Float>,
    val filePath: String,
    val cancelCallback: () -> Unit
)
