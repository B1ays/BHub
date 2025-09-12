package ru.blays.hub.core.logic.components.settingsComponents

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.logic.utils.validateSettings
import ru.blays.hub.core.logic.workers.CheckAppsUpdatesWorker
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.DownloadModeSetting
import ru.blays.hub.core.preferences.proto.PMType
import ru.blays.hub.core.preferences.proto.SettingsKt
import ru.blays.hub.core.preferences.proto.UpdateChannel

class MainSettingsComponent(
    componentContext: ComponentContext,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()
    private val context: Context by inject()
    private val workManager: WorkManager by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
        coroutineScope.launch {
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

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    sealed class Output {
        data object NavigateBack : Output()
    }
}