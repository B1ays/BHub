package ru.blays.hub.core.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.blays.hub.core.preferences.proto.DownloadModeSetting
import ru.blays.hub.core.preferences.proto.FilesSortSetting
import ru.blays.hub.core.preferences.proto.PMType
import ru.blays.hub.core.preferences.proto.Settings
import ru.blays.hub.core.preferences.proto.SettingsKt
import ru.blays.hub.core.preferences.proto.ThemeSettings
import ru.blays.hub.core.preferences.proto.UpdateChannel
import ru.blays.hub.core.preferences.proto.copy

class SettingsRepository internal constructor(
    private val dataStore: DataStore<Settings>,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): CoroutineScope by coroutineScope {
    val themeSettingsFlow: StateFlow<ThemeSettings> = dataStore.toStateFlow(Settings::getThemeSettings)
    val filesSortSettingFlow: StateFlow<FilesSortSetting> = dataStore.toStateFlow(Settings::getFilesSortSetting)
    val downloadModeSettingFlow: StateFlow<DownloadModeSetting> = dataStore.toStateFlow(Settings::getDownloadModeSetting)
    val pmTypeFlow: StateFlow<PMType> = dataStore.toStateFlow(Settings::getPmType)
    //val cacheLifetimeFlow: StateFlow<Long> = dataStore.toStateFlow(Settings::getCacheLifetime)
    val rootModeFlow: StateFlow<Boolean> = dataStore.toStateFlow(Settings::getRootMode)
    val checkUpdatesFlow: StateFlow<Boolean> = dataStore.toStateFlow(Settings::getCheckUpdates)
    val updateChannelFlow: StateFlow<UpdateChannel> = dataStore.toStateFlow(Settings::getUpdateChannel)
    val checkAppsUpdatesFlow: StateFlow<Boolean> = dataStore.toStateFlow(Settings::getCheckAppsUpdates)
    val checkAppsUpdatesIntervalFlow: StateFlow<Int> = dataStore.toStateFlow(Settings::getCheckAppsUpdatesInterval)

    val themeSettings: ThemeSettings
        get() = themeSettingsFlow.value
    val filesSortSetting: FilesSortSetting
        get() = filesSortSettingFlow.value
    val downloadModeSetting
        get() = downloadModeSettingFlow.value
    val pmType: PMType
        get() = pmTypeFlow.value
    /*val cacheLifetime: Long
        get() = cacheLifetimeFlow.value*/
    val rootMode: Boolean
        get() = rootModeFlow.value
    val checkUpdates: Boolean
        get() = checkUpdatesFlow.value
    val updateChannel: UpdateChannel
        get() = updateChannelFlow.value

    val checkAppsUpdates: Boolean
        get() = checkAppsUpdatesFlow.value
    val checkAppsUpdatesInterval: Int
        get() = checkAppsUpdatesIntervalFlow.value

    fun setValue(transform: SettingsKt.Dsl.() -> Unit) {
        launch {
            dataStore.updateData {
                it.copy(transform)
            }
        }
    }
    fun setValue(settings: Settings) {
        launch {
            dataStore.updateData { settings }
        }
    }

    private inline fun <O: Any> DataStore<Settings>.toStateFlow(
        crossinline transform: suspend (value: Settings) -> O
    ): StateFlow<O> = runBlocking {
        data.map(transform).stateIn(this@SettingsRepository)
    }
}