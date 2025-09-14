package ru.blays.hub.core.domain.components.moduleInstallerComponents

import android.content.Context
import android.net.Uri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.domain.data.models.ApkFile
import ru.blays.hub.core.domain.data.models.ApkInfo
import ru.blays.hub.core.domain.data.realType
import ru.blays.hub.core.domain.utils.getService
import ru.blays.hub.core.domain.utils.readableDate
import ru.blays.hub.core.domain.utils.readableSize
import ru.blays.hub.core.moduleManager.ModuleManager
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerResult
import ru.blays.hub.core.packageManager.api.getPackageManager
import ru.blays.hub.core.packageManager.api.utils.getPackageInfo
import ru.blays.hub.core.preferences.SettingsRepository
import java.io.File
import android.content.pm.PackageManager as AndroidPackageManager

class ModuleInstallerComponent(
    componentContext: ComponentContext,
    private val modApk: ApkFile,
    private val origApk: ApkFile? = null,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val context: Context by inject()
    private val settingsRepository: SettingsRepository by inject()

    private val moduleManager: ModuleManager by inject()
    private val androidPackageManager = context.getService<AndroidPackageManager>()
    private val packageManager: PackageManager
        get() = getPackageManager(settingsRepository.pmType.realType)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var tempFile: File? = null

    private val _state: MutableStateFlow<State> = MutableStateFlow(
        State.Empty
    )

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    fun sendIntent(intent: Intent) {
        when(intent) {
            Intent.Install -> install()
            Intent.PickFile -> pickFile()
            is Intent.FilePicked -> onFilePicked(intent.uri)
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    private fun install() {
        when(val stateValue = state.value) {
            is State.AllPicked -> {
                // TODO install
            }
            else -> Unit
        }
    }

    private fun pickFile() {
        when(val stateValue = state.value) {
            is State.NeedToPickOrig -> {
                _state.update {
                    stateValue.copy(
                        pickFile = true,
                        incorrectPackageName = false
                    )
                }
            }
            else -> Unit
        }
    }

    private fun onFilePicked(uri: Uri?) {
        coroutineScope.launch {
            if(uri == null) {
                when(val stateValue = state.value) {
                    is State.NeedToPickOrig -> {
                        _state.update {
                            stateValue.copy(
                                pickFile = false
                            )
                        }
                    }
                    else -> Unit
                }
                return@launch
            }

            val tempFile = File(
                context.cacheDir,
                "install_cache/orig_apk.apk"
            ).apply { mkdirs() }
            this@ModuleInstallerComponent.tempFile = tempFile
            val outputStream = tempFile.outputStream()
            try {
                context.contentResolver
                    .openInputStream(uri)
                    .use { inputStream ->
                        inputStream?.copyTo(outputStream)
                    }
                val packageInfo = context.getPackageInfo(tempFile) ?: error("Failed to get package info")
                val origApk = ApkFile(
                    name = tempFile.name,
                    apkInfo = ApkInfo.fromPackageInfo(
                        packageInfo = packageInfo,
                        packageManager = androidPackageManager
                    ),
                    sizeString = tempFile.readableSize,
                    dateString = tempFile.readableDate,
                    file = tempFile
                )
                when(val stateValue = state.value) {
                    is State.NeedToPickOrig -> {
                        _state.update {
                            if(origApk.apkInfo?.packageName == stateValue.modApk.apkInfo?.packageName) {
                                State.AllPicked(
                                    modApk = stateValue.modApk,
                                    origApk = origApk
                                )
                            } else {
                                stateValue.copy(
                                    pickFile = false,
                                    incorrectPackageName = true
                                )
                            }
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _state.update {
                    State.Error(e.message.orEmpty())
                }
            } finally {
                outputStream.closeQuietly()
            }
        }
    }

    private suspend fun createState(): State {
        if(!DeviceInfo.isRootGranted) return State.RootNotGranted
        val modPackageName = modApk.apkInfo?.packageName ?: error("Failed to get package name")
        return when(
            val result = packageManager.getVersionCode(modPackageName)
        ) {
            is PackageManagerResult.Error -> {
                if(origApk == null) {
                    State.NeedToPickOrig(modApk)
                } else {
                    State.AllPicked(modApk, origApk)
                }
            }
            is PackageManagerResult.Success -> {
                if(result.value?.toLong() == modApk.apkInfo.versionCode) {
                    State.OrigInstalled(modApk)
                } else {
                    State.NeedToPickOrig(modApk)
                }
            }
        }
    }

    init {
        lifecycle.doOnCreate {
            coroutineScope.launch {
                _state.update { createState() }
            }
        }
        lifecycle.doOnDestroy {
            tempFile?.delete()
            coroutineScope.cancel()
        }
    }

    sealed class Intent {
        data object Install: Intent()
        data object PickFile: Intent()
        data class FilePicked(val uri: Uri?): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    sealed class State {
        data object Empty: State()
        data class NeedToPickOrig(
            val modApk: ApkFile,
            val origApk: ApkFile? = null,
            val pickFile: Boolean = false,
            val incorrectPackageName: Boolean = false
        ): State()
        data class AllPicked(
            val modApk: ApkFile,
            val origApk: ApkFile,
        ): State()
        data class OrigInstalled(val modApk: ApkFile): State()
        data object Installing: State()
        data object Success: State()
        data class Error(val message: String): State()
        data object RootNotGranted: State()
    }
}