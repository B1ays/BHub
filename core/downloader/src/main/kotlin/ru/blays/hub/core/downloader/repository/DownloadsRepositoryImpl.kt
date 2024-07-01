package ru.blays.hub.core.downloader.repository

import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.blays.hub.core.downloader.DownloadMode
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.downloader.DownloadTask
import ru.blays.hub.core.downloader.DownloadWorker
import ru.blays.hub.utils.workerdsl.constraints
import ru.blays.hub.utils.workerdsl.oneTimeWorkRequest
import ru.blays.hub.utils.workerdsl.workData

internal class DownloadsRepositoryImpl(
    private val workManager: WorkManager
): DownloadsRepository {
    private val _tasks: MutableList<DownloadTask> = mutableListOf()

    private val _onTaskAddedListeners: MutableSet<DownloadsRepository.OnTaskAddedListener> = mutableSetOf()
    private val _onTaskRemovedListeners: MutableSet<DownloadsRepository.OnTaskRemovedListener> = mutableSetOf()
    private val _onTaskFinishedListeners: MutableSet<DownloadsRepository.OnTaskFinishedListener> = mutableSetOf()

    override val tasks: List<DownloadTask>
        get() = _tasks.toList()

    override fun download(request: DownloadRequest): Flow<WorkInfo> {
        val existingTask = tasks.find { it.filePath == request.filePath }
        if(existingTask != null) {
            return workManager.getWorkInfoByIdFlow(existingTask.workId).filterNotNull()
        }

        val requestString = Json.encodeToString(request)
        val workRequest = oneTimeWorkRequest<DownloadWorker> {
            setInputData(
                inputData = workData {
                    putString(DownloadWorker.REQUEST_KEY, requestString)
                }
            )
            setConstraints(
                constraints = constraints {
                    if(request.downloadMode == DownloadMode.SingleTry) {
                        setRequiredNetworkType(NetworkType.CONNECTED)
                    }
                }
            )
        }
        workManager.enqueue(workRequest)
        val progressFlow = MutableStateFlow(0F)
        val task = DownloadTask(
            workId = workRequest.id,
            name = request.fileName,
            progressFlow = progressFlow.asStateFlow(),
            filePath = request.filePath
        ) {
            workManager.cancelWorkById(workRequest.id)
        }
        onTaskAdded(task)
        return workManager.getWorkInfoByIdFlow(workRequest.id)
            .filterNotNull()
            .onEach { workInfo ->
                when(workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        progressFlow.update {
                            workInfo.progress.getFloat(DownloadWorker.PROGRESS_VALUE_KEY, it)
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> onTaskFinished(task)
                    WorkInfo.State.FAILED,
                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.CANCELLED -> onTaskCancelled(task)
                    else -> Unit
                }
            }
    }

    override fun cancelAllTasks() {
        tasks.forEach { task ->
            task.cancelCallback()
            notifyTaskRemoved(task)
        }
        _tasks.clear()
    }

    private fun onTaskAdded(task: DownloadTask) {
        notifyTaskAdded(task)
        _tasks += task
    }
    private fun onTaskCancelled(task: DownloadTask) {
        notifyTaskRemoved(task)
        _tasks -= task
    }
    private fun onTaskFinished(task: DownloadTask) {
        notifyTaskFinished(task)
        _tasks -= task
    }

    override operator fun plusAssign(listener: DownloadsRepository.OnTaskAddedListener) {
        _onTaskAddedListeners.add(listener)
    }
    override operator fun minusAssign(listener: DownloadsRepository.OnTaskAddedListener) {
        _onTaskAddedListeners.remove(listener)
    }

    override operator fun plusAssign(listener: DownloadsRepository.OnTaskRemovedListener) {
        _onTaskRemovedListeners.add(listener)
    }
    override operator fun minusAssign(listener: DownloadsRepository.OnTaskRemovedListener) {
        _onTaskRemovedListeners.remove(listener)
    }

    override operator fun plusAssign(listener: DownloadsRepository.OnTaskFinishedListener) {
        _onTaskFinishedListeners.add(listener)
    }
    override operator fun minusAssign(listener: DownloadsRepository.OnTaskFinishedListener) {
        _onTaskFinishedListeners.remove(listener)
    }

    private fun notifyTaskAdded(task: DownloadTask) {
        _onTaskAddedListeners.forEach {
            it.onTaskAdded(task)
        }
    }
    private fun notifyTaskRemoved(task: DownloadTask) {
        _onTaskRemovedListeners.forEach {
            it.onTaskRemoved(task)
        }
    }
    private fun notifyTaskFinished(task: DownloadTask) {
        _onTaskFinishedListeners.forEach {
            it.onTaskFinished(task)
        }
    }
}