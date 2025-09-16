package ru.blays.hub.core.domain.components.settingsComponents

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.utils.validateSettings
import ru.blays.hub.core.domain.workers.CheckAppsUpdatesWorker
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.DownloadModeSetting
import ru.blays.hub.core.preferences.proto.PMType
import ru.blays.hub.core.preferences.proto.SettingsKt
import ru.blays.hub.core.preferences.proto.UpdateChannel

class MainSettingsComponent private constructor(
    componentContext: AppComponentContext,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    private val workManager: WorkManager,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    val rootAvailable = DeviceInfo.isRootGranted

    val pmTypeFlow: StateFlow<PMType>
        get() = settingsRepository.pmTypeFlow
    /*val cacheLifetimeFlow: StateFlow<Long>
        get() = settingsRepository.cacheLifetimeFlow*/
    val rootModeFlow: StateFlow<Boolean>
        get() = settingsRepository.rootModeFlow

    val checkUpdatesFlow: StateFlow<Boolean>
        get() = settingsRepository.checkUpdatesFlow
    val updatesChannelFlow: StateFlow<UpdateChannel>
        get() = settingsRepository.updateChannelFlow

    val downloadModeFlow: StateFlow<DownloadModeSetting>
        get() = settingsRepository.downloadModeSettingFlow

    val checkAppsUpdatesFlow: StateFlow<Boolean>
        get() = settingsRepository.checkAppsUpdatesFlow
    val checkAppsUpdatesIntervalFlow: StateFlow<Int>
        get() = settingsRepository.checkAppsUpdatesIntervalFlow

    fun onOutput(output: Output) = onOutput.invoke(output)

    fun setValue(transform: SettingsKt.Dsl.() -> Unit) {
        componentScope.launch {
            settingsRepository.setValue(transform)
            settingsRepository.validateSettings(context)
        }
    }

    fun changeCheckUpdatesSetting(
        checkUpdates: Boolean? = null,
        interval: Int? = null
    ) {
        if(checkUpdates != null) {
            if(checkUpdates) {
                val workRequest = CheckAppsUpdatesWorker.createWorkRequest(
                    interval ?: settingsRepository.checkAppsUpdatesInterval
                )
                workManager.enqueueUniquePeriodicWork(
                    uniqueWorkName = CheckAppsUpdatesWorker.WORK_NAME,
                    existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                    request = workRequest
                )
            } else {
                workManager.cancelUniqueWork(CheckAppsUpdatesWorker.WORK_NAME)
            }
        }
        if(interval != null) {
            if(settingsRepository.checkAppsUpdates) {
                val workRequest = CheckAppsUpdatesWorker.createWorkRequest(
                    interval
                )
                workManager.enqueueUniquePeriodicWork(
                    uniqueWorkName = CheckAppsUpdatesWorker.WORK_NAME,
                    existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                    request = workRequest
                )
            }
        }
        settingsRepository.setValue {
            checkUpdates?.let {
                checkAppsUpdates = it
            }
            interval?.let {
                checkAppsUpdatesInterval = it
            }
        }
    }

    sealed class Output {
        data object NavigateBack : Output()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val context: Context,
        private val workManager: WorkManager,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): MainSettingsComponent {
            return MainSettingsComponent(
                componentContext = componentContext,
                settingsRepository = settingsRepository,
                context = context,
                workManager = workManager,
                onOutput = onOutput
            )
        }
    }
}