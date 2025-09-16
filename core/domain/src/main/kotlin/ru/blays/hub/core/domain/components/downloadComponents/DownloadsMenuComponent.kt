package ru.blays.hub.core.domain.components.downloadComponents

import kotlinx.coroutines.flow.StateFlow
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.utils.map
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.FilesSortSetting

class DownloadsMenuComponent private constructor(
    componentContext: AppComponentContext,
    settingsRepository: SettingsRepository,
    private val onIntent: (Intent) -> Unit
): AppComponentContext by componentContext {
    val state: StateFlow<State> = settingsRepository.filesSortSettingFlow.map(
        coroutineScope = componentScope,
        mapper = ::State
    )

    fun sendIntent(intent: Intent) = onIntent.invoke(intent)

    @JvmInline
    value class State(
        val filesSortSetting: FilesSortSetting
    )

    sealed class Intent {
        data object Refresh : Intent()
        data object ClearAll : Intent()
        data class ChangeSortSetting(val setting: FilesSortSetting): Intent()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onIntent: (Intent) -> Unit
        ): DownloadsMenuComponent {
            return DownloadsMenuComponent(
                componentContext = componentContext,
                settingsRepository = settingsRepository,
                onIntent = onIntent
            )
        }
    }
}