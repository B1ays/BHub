package ru.blays.hub.core.domain.components.preferencesComponents

import android.content.Context
import androidx.work.WorkManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.CheckAppsUpdatesAccessor
import ru.blays.hub.core.domain.CheckUpdatesIntervalAccessor
import ru.blays.hub.core.domain.DownloadModeAccessor
import ru.blays.hub.core.domain.PackageManagerAccessor
import ru.blays.hub.core.domain.RootModeAccessor
import ru.blays.hub.core.downloader.DownloadMode
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder

class MainPreferencesComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val context: Context,
    private val workManager: WorkManager,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    private val rootModeValue = preferencesHolder.getValue(RootModeAccessor)
    private val packageManagerValue = preferencesHolder.getValue(PackageManagerAccessor)
    private val downloadModeValue = preferencesHolder.getValue(DownloadModeAccessor)
    private val checkUpdatesValue = preferencesHolder.getValue(CheckAppsUpdatesAccessor)
    private val checkUpdatesIntervalValue = preferencesHolder.getValue(CheckUpdatesIntervalAccessor)

    val state: StateFlow<State> =
        combine(
            rootModeValue,
            packageManagerValue,
            downloadModeValue,
            checkUpdatesValue,
            checkUpdatesIntervalValue
        ) { rootMode, packageManager, downloadMode, checkUpdates, checkUpdatesInterval ->
            State(
                rootAvailable = DeviceInfo.isRootGranted,
                rootMode = rootMode,
                packageManagerType = packageManager,
                downloadMode = downloadMode,
                checkUpdates = checkUpdates,
                checkUpdatesInterval = checkUpdatesInterval,
            )
        }.stateIn(
            scope = componentScope,
            started = SharingStarted.Eagerly,
            initialValue = State(
                rootAvailable = rootModeValue.value,
                rootMode = rootModeValue.value,
                packageManagerType = packageManagerValue.value,
                downloadMode = downloadModeValue.value,
                checkUpdates = checkUpdatesValue.value,
                checkUpdatesInterval = checkUpdatesIntervalValue.value,
            )
        )

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.ChangeCheckUpdates ->
                checkUpdatesValue.updateValue(intent.value)
            is Intent.ChangeCheckUpdatesInterval ->
                checkUpdatesIntervalValue.updateValue(intent.value)
            is Intent.ChangeDownloadMode ->
                downloadModeValue.updateValue(intent.value)
            is Intent.ChangePackageManagerType ->
                packageManagerValue.updateValue(intent.value)
            is Intent.ChangeRootMode ->
                rootModeValue.updateValue(intent.value)
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    /*fun changeCheckUpdatesSetting(
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
    }*/ // TODO: Create new impl

    data class State(
        val rootAvailable: Boolean,
        val rootMode: Boolean,
        val packageManagerType: PackageManagerType,
        val downloadMode: DownloadMode,
        val checkUpdates: Boolean,
        val checkUpdatesInterval: Int,
    )

    sealed class Intent {
        data class ChangeRootMode(override val value: Boolean): Intent()
        data class ChangePackageManagerType(override val value: PackageManagerType): Intent()
        data class ChangeDownloadMode(override val value: DownloadMode): Intent()
        data class ChangeCheckUpdates(override val value: Boolean): Intent()
        data class ChangeCheckUpdatesInterval(override val value: Int): Intent()

        abstract val value: Any
    }

    sealed class Output {
        data object NavigateBack : Output()
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
        private val context: Context,
        private val workManager: WorkManager,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): MainPreferencesComponent {
            return MainPreferencesComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                context = context,
                workManager = workManager,
                onOutput = onOutput
            )
        }
    }
}