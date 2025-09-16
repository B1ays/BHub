package ru.blays.hub.core.domain.components

import android.content.Context
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.data.LocalizedMessage
import ru.blays.hub.core.domain.data.getUpdateChannelUrl
import ru.blays.hub.core.domain.data.models.AppUpdateInfoModel
import ru.blays.hub.core.domain.data.realType
import ru.blays.hub.core.domain.data.toDownloaderType
import ru.blays.hub.core.domain.utils.currentLanguage
import ru.blays.hub.core.domain.utils.downloadsFolder
import ru.blays.hub.core.domain.workers.DownloadAndInstallWorker
import ru.blays.hub.core.downloader.DownloadMode
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.downloader.repository.DownloadsRepository
import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.UpdateInfoModel
import ru.blays.hub.core.network.repositories.appUpdatesRepository.AppUpdatesRepository
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.getPackageManager
import ru.blays.hub.core.preferences.SettingsRepository
import java.io.File

class SelfUpdateComponent private constructor(
    componentContext: AppComponentContext,
    checkOnCreate: Boolean,
    private val settingsRepository: SettingsRepository,
    private val updatesRepository: AppUpdatesRepository,
    private val networkRepository: NetworkRepository,
    private val downloadsRepository: DownloadsRepository,
    private val workManager: WorkManager,
    private val context: Context,
): AppComponentContext by componentContext {
    private val packageManager: PackageManager
        get() = getPackageManager(settingsRepository.pmType.realType)
    private val updateChannelUrl: String
        get() = getUpdateChannelUrl(settingsRepository.updateChannel)
    private val downloadMode: DownloadMode
        get() = settingsRepository.downloadModeSetting.let { setting ->
            setting.mode.toDownloaderType(setting.triesNumber)
        }

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)

    val state: StateFlow<State> = _state.asStateFlow()

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.Refresh -> refresh()
            is Intent.UpdateApp -> update(intent.updateInfo)
        }
    }

    fun onOutput(output: Output) {
        when(output) {
            is Output.Close -> _state.update { State.Idle }
        }
    }

    private fun refresh() {
        componentScope.launch {
            _state.update { State.Loading }

            when(
                val result = updatesRepository.getUpdateInfo(updateChannelUrl)
            ) {
                is NetworkResult.Failure -> {
                    Logger.e(TAG, result.error)
                    _state.update {
                        State.Error(
                            result.error.message ?: context.getString(R.string.unknown_error)
                        )
                    }
                }
                is NetworkResult.Success -> {
                    val resultModel = result.data

                    val changelogResult = networkRepository
                        .openStream(resultModel.changelogUrl)

                    val updateInfo = when(changelogResult) {
                        is NetworkResult.Failure -> {
                            resultModel.toUIModel(
                                context.getString(R.string.error_changelog_not_found)
                            )
                        }
                        is NetworkResult.Success -> {
                            val localizedMessage = LocalizedMessage(changelogResult.data)
                            if(localizedMessage.isValid) {
                                val changelog = localizedMessage.getForLanguageOrDefault(
                                    context.currentLanguage
                                ) ?: context.getString(R.string.error_changelog_not_found)
                                resultModel.toUIModel(changelog)
                            } else {
                                resultModel.toUIModel(
                                    context.getString(R.string.error_changelog_not_found)
                                )
                            }
                        }
                    }

                    if(checkUpdateAvailable(resultModel.versionCode)) {
                        _state.update {
                            State.Available(updateInfo)
                        }
                    } else {
                        _state.update { State.NotAvailable }
                    }
                }
            }
        }
    }

    private fun update(updateInfo: AppUpdateInfoModel) {
        val fileName = "${context.packageName}-${updateInfo.versionName}(${updateInfo.versionCode})"
        val downloadRequest = DownloadRequest.builder(
            url = updateInfo.apkUrl,
            fileName = fileName,
            file = File(context.downloadsFolder, "$fileName.apk"),
        ) {
            downloadMode(downloadMode)
        }
        val installRequest = DownloadAndInstallWorker.createRequest(
            downloadRequest = downloadRequest,
            packageManagerType = settingsRepository.pmType.realType,
            packageName = context.packageName
        )
        workManager.enqueue(installRequest)
        _state.update { State.Downloading }
    }

    private suspend fun checkUpdateAvailable(availableVersionCode: Int): Boolean {
        val installedVersionCode = packageManager
            .getVersionCode(context.packageName)
            .getValueOrNull()
            ?: return false
        return installedVersionCode < availableVersionCode
    }

    private fun UpdateInfoModel.toUIModel(changelog: String): AppUpdateInfoModel = AppUpdateInfoModel(
        versionName = versionName,
        versionCode = versionCode,
        buildDate = buildDate,
        changelog = changelog,
        apkUrl = apkUrl
    )

    init {
        if(settingsRepository.checkUpdates && checkOnCreate) {
            refresh()
        }
    }

    sealed class Intent {
        data object Refresh: Intent()
        data class UpdateApp(val updateInfo: AppUpdateInfoModel): Intent()
    }

    sealed class Output {
        data object Close: Output()
    }

    sealed class State {
        data object Idle: State()
        data object Loading: State()
        data class Error(val message: String): State()
        data class Available(val updateInfo: AppUpdateInfoModel): State()
        data object NotAvailable: State()
        data object Downloading: State()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val updatesRepository: AppUpdatesRepository,
        private val networkRepository: NetworkRepository,
        private val downloadsRepository: DownloadsRepository,
        private val workManager: WorkManager,
        private val context: Context,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            checkOnCreate: Boolean,
        ): SelfUpdateComponent {
            return SelfUpdateComponent(
                componentContext = componentContext,
                checkOnCreate = checkOnCreate,
                settingsRepository = settingsRepository,
                updatesRepository = updatesRepository,
                networkRepository = networkRepository,
                downloadsRepository = downloadsRepository,
                workManager = workManager,
                context = context
            )
        }
    }

    companion object {
        private const val TAG = "SelfUpdateComponent"
    }
}