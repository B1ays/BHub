package ru.blays.hub.core.logic.components.appPageComponents

import android.content.Context
import android.widget.Toast
import androidx.work.WorkManager
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.downloader.DownloadMode
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.logic.FLAG_REINSTALL
import ru.blays.hub.core.logic.R
import ru.blays.hub.core.logic.VIBRATION_LENGTH
import ru.blays.hub.core.logic.components.InfoDialogComponent
import ru.blays.hub.core.logic.components.InfoDialogConfig
import ru.blays.hub.core.logic.data.LocalizedMessage
import ru.blays.hub.core.logic.data.models.ApkInfoCardModel
import ru.blays.hub.core.logic.data.realType
import ru.blays.hub.core.logic.data.toDownloaderType
import ru.blays.hub.core.logic.utils.VersionName.Companion.toVersionName
import ru.blays.hub.core.logic.utils.currentLanguage
import ru.blays.hub.core.logic.utils.downloadsFolder
import ru.blays.hub.core.logic.utils.runOnUiThread
import ru.blays.hub.core.logic.utils.toIntArray
import ru.blays.hub.core.logic.utils.vibrate
import ru.blays.hub.core.logic.workers.DownloadAndInstallModuleWorker
import ru.blays.hub.core.logic.workers.DownloadAndInstallWorker
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.ApkListModel
import ru.blays.hub.core.network.okHttpDsl.fullUrlString
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepository
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository
import ru.blays.hub.core.packageManager.PackageManager
import ru.blays.hub.core.packageManager.PackageManagerType
import ru.blays.hub.core.packageManager.getPackageManager
import ru.blays.hub.core.preferences.SettingsRepository
import java.io.File

class VersionPageComponent(
    componentContext: ComponentContext,
    private val config: VersionPageSlotConfig,
    private val onRefresh: () -> Unit,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val appsRepository: AppsRepository by inject()
    private val networkRepository: NetworkRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val workManager: WorkManager by inject()
    private val context: Context by inject()

    val packageManager: PackageManager
        get() = getPackageManager(pmType)

    private val pmType: PackageManagerType
        get() = settingsRepository.pmType.realType
    private val downloadMode: DownloadMode
        get() = settingsRepository.downloadModeSetting.let { setting ->
            setting.mode.toDownloaderType(setting.triesNumber)
        }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val slotNavigation = SlotNavigation<InfoDialogConfig>()

    private var apkToInstall: ApkInfoCardModel? = null
    private var rootVersion: Boolean = false
    private var originalApkUrl: String? = null

    private var changelogLoaded: Boolean = false

    private val _state: MutableStateFlow<State> = MutableStateFlow(
        State(
            version = config.versionCard.version,
            patchesVersion = config.versionCard.patchesVersion,
            buildDate = config.versionCard.buildDate,
        )
    )

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    val dialogSlot = childSlot(
        source = slotNavigation,
        initialConfiguration = { null },
        serializer = InfoDialogConfig.serializer(),
    ) { config, childContext ->
        InfoDialogComponent<InfoDialogConfig, ApkInstallAction>(
            componentContext = childContext,
            state = config,
            onAction = { action ->
                when(action) {
                    ApkInstallAction.Cancel -> slotNavigation.dismiss()
                    ApkInstallAction.Continue -> {
                        slotNavigation.dismiss()
                        apkToInstall?.let {
                            coroutineScope.launch {
                                installApk(it, false)
                            }
                        }
                    }
                    ApkInstallAction.Reinstall -> coroutineScope.launch {
                        apkToInstall?.let {
                            installApk(it, reinstall = true)
                        }
                    }
                }
            }
        )
    }

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.LoadChangelog -> loadChangelog()
            is Intent.InstallApk -> coroutineScope.launch {
                if (needWarnAboutDowngrade()) {
                    apkToInstall = intent.apkInfo
                    slotNavigation.activate(
                        InfoDialogConfig(
                            title = context.getString(R.string.downgradeWarn_title),
                            message = context.getString(R.string.downgradeWarn_message)
                        )
                    )
                } else {
                    installApk(intent.apkInfo, reinstall = false)
                }
            }
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    private fun loadFiles() {
        coroutineScope.launch {
            _state.update { it.copy(apkListLoading = true) }
            val result = appsRepository.getApkList(
                config.sourceUrl,
                config.versionCard.apkListHref
            )
            when(result) {
                is NetworkResult.Failure -> {
                    _state.update {
                        it.copy(apkListLoading = false)
                    }
                }
                is NetworkResult.Success -> {
                    originalApkUrl = result.data.originalApkUrl
                    rootVersion = result.data.originalApkUrl != null
                    _state.update {
                        it.copy(
                            apkList = result.data.files.map { it.toUIModel() },
                            apkListLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadChangelog() {
        if(
            config.versionCard.changelogHref != null &&
            !state.value.changelogLoading &&
            !changelogLoaded
        ) {
            coroutineScope.launch {
                _state.update { it.copy(changelogLoading = true) }
                val result = networkRepository.getString(
                    fullUrlString(config.sourceUrl, config.versionCard.changelogHref)
                )
                if(result is NetworkResult.Success) {
                    val localizedMessage = LocalizedMessage(result.data)
                    val changelog = if(localizedMessage.isValid) {
                        localizedMessage.getForLanguageOrDefault(context.currentLanguage)
                    } else {
                        result.data
                    }
                    Logger.d(this::class.simpleName, "Changelog:\n$changelog")
                    _state.update {
                        it.copy(
                            changelog = changelog,
                            changelogLoading = false
                        )
                    }
                    changelogLoaded = true
                }
            }
        }
    }

    private suspend fun installApk(
        apkInfo: ApkInfoCardModel,
        reinstall: Boolean = false
    ) = coroutineScope {
        val flags: List<Int> = buildList {
            if(reinstall) {
                add(FLAG_REINSTALL)
            }
        }

        val installRequest = if(rootVersion) {
            if(originalApkUrl == null) return@coroutineScope
            val modApkDownloadRequest = DownloadRequest.builder(
                url = apkInfo.url,
                fileName = apkInfo.name,
                file = File(
                    context.downloadsFolder,
                    "${apkInfo.name}.apk"
                )
            ) {
                downloadMode(downloadMode)
            }
            val origFileName = "${config.appName}-${config.versionCard.version}-orig"
            val origApkDownloadRequest = DownloadRequest.builder(
                url = originalApkUrl!!,
                fileName = origFileName,
                file = File(
                    context.downloadsFolder,
                    "$origFileName.apk"
                )
            ) {
                downloadMode(downloadMode)
            }
            DownloadAndInstallModuleWorker.createRequest(
                modApkDownloadRequest = modApkDownloadRequest,
                originalApkDownloadRequest = origApkDownloadRequest,
                packageName = config.packageName,
                flags = flags.toIntArray()
            )
        } else {
            val downloadRequest = DownloadRequest.builder(
                url = apkInfo.url,
                fileName = apkInfo.name,
                file = File(
                    context.downloadsFolder,
                    "${apkInfo.name}.apk"
                )
            ) {
                downloadMode(downloadMode)
            }
            DownloadAndInstallWorker.createRequest(
                downloadRequest = downloadRequest,
                packageManagerType = pmType,
                packageName = config.packageName,
                flags = IntArray(flags.size, flags::get)
            )
        }
        runOnUiThread {
            context.vibrate(VIBRATION_LENGTH)
            Toast.makeText(
                context,
                R.string.downloadStarted,
                Toast.LENGTH_SHORT
            ).show()
        }
        try {
            workManager.enqueue(installRequest).result.get()
            onRefresh()
        } catch(e: Exception) {
            Logger.e("VersionPageComponent", "Install worker", e)
        }
    }

    private suspend fun needWarnAboutDowngrade(): Boolean {
        if(rootVersion) return false
        if(pmType == PackageManagerType.Root) return false

        val oldVersion = packageManager.getVersionName(config.packageName).getValueOrNull()
        val newVersion = config.versionCard.version
        val oldVersionName = oldVersion?.toVersionName() ?: return false
        val newVersionName = newVersion.toVersionName() ?: return false
        return newVersionName < oldVersionName
    }

    private fun ApkListModel.Apk.toUIModel() = ApkInfoCardModel(
        name = apkName,
        description = shortDescription,
        url = url
    )

    init { loadFiles() }

    sealed class Intent {
        data object LoadChangelog: Intent()
        data class InstallApk(val apkInfo: ApkInfoCardModel): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    data class State(
        val version: String,
        val patchesVersion: String? = null,
        val buildDate: String,
        val changelog: String? = null,
        val apkList: List<ApkInfoCardModel> = emptyList(),
        val apkListLoading: Boolean = false,
        val changelogLoading: Boolean = false
    )
}

sealed class ApkInstallAction {
    data object Cancel: ApkInstallAction()
    data object Reinstall: ApkInstallAction()
    data object Continue: ApkInstallAction()
}