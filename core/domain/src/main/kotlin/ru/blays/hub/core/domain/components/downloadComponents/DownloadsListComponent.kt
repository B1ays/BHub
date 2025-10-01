package ru.blays.hub.core.domain.components.downloadComponents

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import androidx.compose.runtime.Stable
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.PackageManagerAccessor
import ru.blays.hub.core.domain.PackageManagerResolver
import ru.blays.hub.core.domain.SortReverseOrderAccessor
import ru.blays.hub.core.domain.SortTypeAccessor
import ru.blays.hub.core.domain.data.FilesSortType
import ru.blays.hub.core.domain.data.models.ApkFile
import ru.blays.hub.core.domain.data.models.ApkInfo
import ru.blays.hub.core.domain.utils.downloadsFolder
import ru.blays.hub.core.domain.utils.getUriForFile
import ru.blays.hub.core.domain.utils.isApkFile
import ru.blays.hub.core.domain.utils.readableDate
import ru.blays.hub.core.domain.utils.readableSize
import ru.blays.hub.core.downloader.DownloadTask
import ru.blays.hub.core.downloader.repository.DownloadsRepository
import ru.blays.hub.core.packageManager.api.utils.getPackageInfo
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder
import java.io.File

class DownloadsListComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val downloadsRepository: DownloadsRepository,
    private val context: Context,
    private val packageManagerResolver: PackageManagerResolver,
    private val menuComponentFactory: DownloadsMenuComponent.Factory,
): AppComponentContext by componentContext {
    private val packageManagerValue = preferencesHolder.getValue(PackageManagerAccessor)
    private var sortTypeValue by preferencesHolder.getValue(SortTypeAccessor)
    private var sortReverseOrderValue by preferencesHolder.getValue(SortReverseOrderAccessor)

    private val systemPackageManager = context.packageManager

    private val _state = MutableStateFlow(State())

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    val menuComponent: DownloadsMenuComponent by lazy {
        menuComponentFactory(
            componentContext = childContext("DownloadsMenuComponent"),
            onIntent = ::onMenuIntent
        )
    }

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.CancelTask -> cancelTask(intent.task)
            is Intent.OpenFile -> openFile(intent.file)
            is Intent.DeleteFile -> deleteFile(intent.file)
            is Intent.InstallApk -> installApk(intent.file)
        }
    }

    private fun onMenuIntent(intent: DownloadsMenuComponent.Intent) {
        when(intent) {
            is DownloadsMenuComponent.Intent.ChangeSortSetting -> changeSortSetting(type = intent.type)
            DownloadsMenuComponent.Intent.ClearAll -> clearAll()
            DownloadsMenuComponent.Intent.Refresh -> refresh()
            is DownloadsMenuComponent.Intent.ChangeSortOrder -> changeSortSetting(reverseOrder = intent.reverse)
        }
    }

    private fun cancelTask(task: Task) {
        task.onCancel()
        _state.update {
            it.copy(
                tasks = it.tasks - task
            )
        }
    }

    private fun openFile(task: ApkFile) {
        context.startActivity(
            Intent().apply {
                action = ACTION_VIEW
                data = context.getUriForFile(task.file)
                addFlags(
                FLAG_ACTIVITY_NEW_TASK or
                    FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        )
    }

    private fun deleteFile(file: ApkFile) {
        if(file.file.delete()) {
            _state.update {
                it.copy(
                    downloadedFiles = it.downloadedFiles - file
                )
            }
        }
    }

    private fun installApk(file: ApkFile) {
        componentScope.launch {
            val packageManager = packageManagerResolver.getPackageManager(packageManagerValue.value)
            packageManager.installApp(file.file)
        }
    }

    private fun refresh() = componentScope.launch {
        _state.update {
            it.copy(downloadedFiles = emptyList())
        }
        loadExistingFiles()
    }

    private fun clearAll() {
        componentScope.launch {
            state.value.downloadedFiles.forEach(::deleteFile)
        }
    }

    private fun changeSortSetting(
        type: FilesSortType? = null,
        reverseOrder: Boolean? = null,
    ) {
        type?.let { this.sortTypeValue = it }
        reverseOrder?.let { this.sortReverseOrderValue = it }

        val oldList = state.value.downloadedFiles
        if(oldList.isEmpty()) return
        _state.update {
            it.copy(
                downloadedFiles = it.downloadedFiles.sortFiles()
            )
        }
    }

    private fun List<ApkFile>.sortFiles(): List<ApkFile> {
        return when (sortTypeValue) {
            FilesSortType.NAME -> sortedBy { it.file.name }
            FilesSortType.SIZE -> sortedBy { it.file.length() }
            FilesSortType.MODIFY -> sortedBy { it.file.lastModified() }
        }.run {
            if(sortReverseOrderValue) asReversed() else this
        }
    }

    private suspend fun loadExistingFiles() {
        _state.update {
            it.copy(loading = true)
        }
        val files = context.downloadsFolder.listFiles()
            ?.filter(File::isApkFile)
            ?.map { file -> file.toDownloadedApk() }
            ?.sortFiles()
            ?: emptyList()
        _state.update {
            it.copy(
                downloadedFiles = files,
                loading = false
            )
        }
    }

    private suspend fun loadExistingTasks() {
        val task = downloadsRepository.tasks.map { it.toTask() }
        _state.update {
            it.copy(tasks = task)
        }
    }

    private fun DownloadTask.toTask() = Task(
        name = name,
        progressFlow = progressFlow,
        filePath = filePath,
        onCancel = cancelCallback
    )

    private suspend fun DownloadTask.toDownloadedApk() = coroutineScope {
        val packageInfo = context.getPackageInfo(filePath)
        val file = File(filePath)
        ApkFile(
            name = name,
            apkInfo = packageInfo?.let { ApkInfo.fromPackageInfo(packageInfo, systemPackageManager) },
            sizeString = file.readableSize,
            dateString = file.readableDate,
            file = file
        )
    }

    private suspend fun File.toDownloadedApk() = coroutineScope {
        val packageInfo = context.getPackageInfo(path)
        ApkFile(
            name = name,
            apkInfo = packageInfo?.let { ApkInfo.fromPackageInfo(packageInfo, systemPackageManager) },
            sizeString = readableSize,
            dateString = readableDate,
            file = this@toDownloadedApk
        )
    }

    init {
        val onTaskAddedListener = DownloadsRepository.OnTaskAddedListener { task ->
            _state.update { oldState ->
                oldState.copy(
                    tasks = buildList {
                        add(task.toTask())
                        addAll(oldState.tasks)
                    }
                )
            }
        }
        val onTaskRemovedListener = DownloadsRepository.OnTaskRemovedListener { task ->
            _state.update { oldState ->
                oldState.copy(
                    tasks = oldState.tasks.filterNot { it.filePath == task.filePath }
                )
            }
        }
        val onTaskFinishedListener = DownloadsRepository.OnTaskFinishedListener { task ->
            componentScope.launch {
                _state.update { oldState ->
                    oldState.copy(
                        tasks = oldState.tasks.filterNot { it.filePath == task.filePath },
                        downloadedFiles = (oldState.downloadedFiles + task.toDownloadedApk()).sortFiles()
                    )
                }
            }
        }
        lifecycle.doOnCreate {
            componentScope.launch {
                loadExistingTasks()
                loadExistingFiles()
            }

            downloadsRepository += onTaskAddedListener
            downloadsRepository += onTaskRemovedListener
            downloadsRepository += onTaskFinishedListener
        }
        lifecycle.doOnDestroy {
            downloadsRepository -= onTaskAddedListener
            downloadsRepository -= onTaskRemovedListener
            downloadsRepository -= onTaskFinishedListener
        }
    }

    data class State(
        val tasks: List<Task> = emptyList(),
        val downloadedFiles: List<ApkFile> = emptyList(),
        val loading: Boolean = false,
    )

    @ConsistentCopyVisibility
    @Stable
    data class Task internal constructor(
        val name: String,
        val progressFlow: Flow<Float>,
        internal val filePath: String,
        internal val onCancel: () -> Unit
    ) {
        internal val file by lazy { File(filePath) }
    }

    sealed class Intent {
        data class CancelTask(val task: Task) : Intent()
        data class OpenFile(val file: ApkFile) : Intent()
        data class DeleteFile(val file: ApkFile) : Intent()
        data class InstallApk(val file: ApkFile) : Intent()
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
        private val downloadsRepository: DownloadsRepository,
        private val context: Context,
        private val packageManagerResolver: PackageManagerResolver,
        private val menuComponentFactory: DownloadsMenuComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
        ): DownloadsListComponent {
            return DownloadsListComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                downloadsRepository = downloadsRepository,
                context = context,
                packageManagerResolver = packageManagerResolver,
                menuComponentFactory = menuComponentFactory,
            )
        }
    }
}