package ru.blays.hub.core.logic.components.downloadComponents

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageInfo
import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.downloader.DownloadTask
import ru.blays.hub.core.downloader.repository.DownloadRepository
import ru.blays.hub.core.logic.data.models.ApkFile
import ru.blays.hub.core.logic.data.models.ApkInfo
import ru.blays.hub.core.logic.data.realType
import ru.blays.hub.core.logic.utils.downloadsFolder
import ru.blays.hub.core.logic.utils.getUriForFile
import ru.blays.hub.core.logic.utils.isApkFile
import ru.blays.hub.core.logic.utils.readableDate
import ru.blays.hub.core.logic.utils.readableSize
import ru.blays.hub.core.packageManager.getPackageManager
import ru.blays.hub.core.packageManager.utils.getPackageInfo
import ru.blays.hub.core.packageManager.utils.signatureHash
import ru.blays.hub.core.packageManager.utils.versionCodeLong
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.FilesSortMethod
import ru.blays.hub.core.preferences.proto.FilesSortSetting
import java.io.File

class DownloadsListComponent(
    componentContext: ComponentContext
): ComponentContext by componentContext, KoinComponent {
    private val downloadRepository: DownloadRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val context: Context by inject()
    private val packageManager by lazy(context::getPackageManager)

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _state = MutableStateFlow(State())

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    val menuComponent: DownloadsMenuComponent by lazy {
        DownloadsMenuComponent(
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
            is DownloadsMenuComponent.Intent.ChangeSortSetting -> changeSortSetting(intent.setting)
            DownloadsMenuComponent.Intent.ClearAll -> clearAll()
            DownloadsMenuComponent.Intent.Refresh -> refresh()
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
        scope.launch {
            val packageManager = getPackageManager(settingsRepository.pmType.realType)
            packageManager.installApp(file.file)
        }
    }

    private fun refresh() = scope.launch {
        _state.update {
            it.copy(downloadedFiles = emptyList())
        }
        loadExistingFiles()
    }

    private fun clearAll() {
        scope.launch {
            state.value.downloadedFiles.forEach(::deleteFile)
        }
    }

    private fun changeSortSetting(setting: FilesSortSetting) {
        scope.launch {
            settingsRepository.setValue {
                filesSortSetting = setting
            }

            val oldList = state.value.downloadedFiles
            if(oldList.isEmpty()) return@launch
            _state.update {
                it.copy(
                    downloadedFiles = it.downloadedFiles.sortFiles(setting)
                )
            }
        }
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    private fun List<ApkFile>.sortFiles(
        setting: FilesSortSetting
    ): List<ApkFile> {
        return when (setting.method) {
            FilesSortMethod.NAME -> sortedBy { it.file.name }
            FilesSortMethod.SIZE -> sortedBy { it.file.length() }
            FilesSortMethod.DATE -> sortedBy { it.file.lastModified() }
            FilesSortMethod.UNRECOGNIZED -> this
        }.run {
            if(setting.reverse) asReversed() else this
        }
    }

    private suspend fun loadExistingFiles() {
        _state.update {
            it.copy(loading = true)
        }
        val files = context.downloadsFolder.listFiles()
            ?.filter(File::isApkFile)
            ?.map { file -> file.toDownloadedApk() }
            ?.sortFiles(settingsRepository.filesSortSetting)
            ?: emptyList()
        _state.update {
            it.copy(
                downloadedFiles = files,
                loading = false
            )
        }
    }

    private suspend fun loadExistingTasks() {
        val task = downloadRepository.tasks.map { it.toTask() }
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
            apkInfo = packageInfo?.toApkInfo(),
            sizeString = file.readableSize,
            dateString = file.readableDate,
            file = file
        )
    }

    private suspend fun File.toDownloadedApk() = coroutineScope {
        val packageInfo = context.getPackageInfo(path)
        ApkFile(
            name = name,
            apkInfo = packageInfo?.toApkInfo(),
            sizeString = readableSize,
            dateString = readableDate,
            file = this@toDownloadedApk
        )
    }

    private fun PackageInfo.toApkInfo() = ApkInfo(
        icon = applicationInfo.loadIcon(packageManager),
        name = applicationInfo.name,
        packageName = packageName,
        versionName = versionName,
        versionCode = versionCodeLong,
        signatureHash = signatureHash
    )

    init {
        val onTaskAddedListener = DownloadRepository.OnTaskAddedListener { task ->
            _state.update { oldState ->
                oldState.copy(
                    tasks = buildList {
                        add(task.toTask())
                        addAll(oldState.tasks)
                    }
                )
            }
        }
        val onTaskRemovedListener = DownloadRepository.OnTaskRemovedListener { task ->
            _state.update { oldState ->
                oldState.copy(
                    tasks = oldState.tasks.filterNot { it.filePath == task.filePath }
                )
            }
        }
        val onTaskFinishedListener = DownloadRepository.OnTaskFinishedListener { task ->
            scope.launch {
                _state.update { oldState ->
                    oldState.copy(
                        tasks = oldState.tasks.filterNot { it.filePath == task.filePath },
                        downloadedFiles = (oldState.downloadedFiles + task.toDownloadedApk())
                            .sortFiles(settingsRepository.filesSortSetting)
                    )
                }
            }
        }
        lifecycle.doOnCreate {
            scope.launch {
                loadExistingTasks()
                loadExistingFiles()
            }

            downloadRepository += onTaskAddedListener
            downloadRepository += onTaskRemovedListener
            downloadRepository += onTaskFinishedListener
        }
        lifecycle.doOnDestroy {
            downloadRepository -= onTaskAddedListener
            downloadRepository -= onTaskRemovedListener
            downloadRepository -= onTaskFinishedListener
            scope.cancel()
        }
    }

    data class State(
        val tasks: List<Task> = emptyList(),
        val downloadedFiles: List<ApkFile> = emptyList(),
        val loading: Boolean = false,
    )

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
}