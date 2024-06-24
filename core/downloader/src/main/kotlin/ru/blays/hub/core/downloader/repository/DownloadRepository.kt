package ru.blays.hub.core.downloader.repository

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.downloader.DownloadTask

interface DownloadRepository {
    val tasks: List<DownloadTask>

    fun download(request: DownloadRequest): Flow<WorkInfo>
    fun cancelAllTasks()

    operator fun plusAssign(listener: OnTaskAddedListener)
    operator fun minusAssign(listener: OnTaskAddedListener)

    operator fun plusAssign(listener: OnTaskRemovedListener)
    operator fun minusAssign(listener: OnTaskRemovedListener)

    operator fun plusAssign(listener: OnTaskFinishedListener)
    operator fun minusAssign(listener: OnTaskFinishedListener)

    fun interface OnTaskAddedListener {
        fun onTaskAdded(task: DownloadTask)
    }
    fun interface OnTaskRemovedListener {
        fun onTaskRemoved(task: DownloadTask)
    }
    fun interface OnTaskFinishedListener {
        fun onTaskFinished(task: DownloadTask)
    }
}