package ru.blays.hub.core.domain.components.appPageComponents

import android.content.Context
import android.widget.Toast
import androidx.work.WorkManager
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.DownloadModeAccessor
import ru.blays.hub.core.domain.FLAG_REINSTALL
import ru.blays.hub.core.domain.PackageManagerAccessor
import ru.blays.hub.core.domain.PackageManagerResolver
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.VIBRATION_LENGTH
import ru.blays.hub.core.domain.components.InfoDialogComponent
import ru.blays.hub.core.domain.components.InfoDialogConfig
import ru.blays.hub.core.domain.data.LocalizedMessage
import ru.blays.hub.core.domain.data.models.ApkInfoCardModel
import ru.blays.hub.core.domain.utils.VersionName.Companion.toVersionName
import ru.blays.hub.core.domain.utils.currentLanguage
import ru.blays.hub.core.domain.utils.downloadsFolder
import ru.blays.hub.core.domain.utils.runOnUiThread
import ru.blays.hub.core.domain.utils.toIntArray
import ru.blays.hub.core.domain.utils.vibrate
import ru.blays.hub.core.domain.workers.DownloadAndInstallModuleWorker
import ru.blays.hub.core.domain.workers.DownloadAndInstallWorker
import ru.blays.hub.core.downloader.DownloadRequest
import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.ApkListModel
import ru.blays.hub.core.network.okHttpDsl.fullUrlString
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepository
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder
import java.io.File

class VersionPageComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val config: VersionPageSlotConfig,
    private val appsRepository: AppsRepository,
    private val networkRepository: NetworkRepository,
    private val packageManagerResolver: PackageManagerResolver,
    private val workManager: WorkManager,
    private val context: Context,
    private val onRefresh: () -> Unit,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    private val packageManagerValue = preferencesHolder.getValue(PackageManagerAccessor)
    private val downloadModeValue = preferencesHolder.getValue(DownloadModeAccessor)

    val packageManager: PackageManager
        get() = packageManagerResolver.getPackageManager(packageManagerValue.value)

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
                            componentScope.launch {
                                installApk(it, false)
                            }
                        }
                    }
                    ApkInstallAction.Reinstall -> componentScope.launch {
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
            is Intent.InstallApk -> componentScope.launch {
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
        componentScope.launch {
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
                    _state.update { oldState ->
                        oldState.copy(
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
            componentScope.launch {
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
                downloadMode(downloadModeValue.value)
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
                downloadMode(downloadModeValue.value)
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
                downloadMode(downloadModeValue.value)
            }
            DownloadAndInstallWorker.createRequest(
                downloadRequest = downloadRequest,
                packageManagerType = packageManagerValue.value,
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
        if(packageManagerValue.value == PackageManagerType.Root) return false

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

    init {
        lifecycle.doOnCreate {
            loadFiles()
        }
    }

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

    class Factory(
        private val appsRepository: AppsRepository,
        private val networkRepository: NetworkRepository,
        private val workManager: WorkManager,
        private val context: Context,
        private val preferencesHolder: PreferencesHolder,
        private val packageManagerResolver: PackageManagerResolver,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            config: VersionPageSlotConfig,
            onRefresh: () -> Unit,
            onOutput: (Output) -> Unit
        ): VersionPageComponent {
            return VersionPageComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                config = config,
                appsRepository = appsRepository,
                networkRepository = networkRepository,
                packageManagerResolver = packageManagerResolver,
                workManager = workManager,
                context = context,
                onRefresh = onRefresh,
                onOutput = onOutput,
            )
        }
    }
}

sealed class ApkInstallAction {
    data object Cancel: ApkInstallAction()
    data object Reinstall: ApkInstallAction()
    data object Continue: ApkInstallAction()
}