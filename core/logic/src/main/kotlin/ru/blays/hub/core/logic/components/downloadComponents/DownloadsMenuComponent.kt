package ru.blays.hub.core.logic.components.downloadComponents

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.logic.utils.map
import ru.blays.hub.core.preferences.proto.FilesSortSetting
import ru.blays.hub.core.preferences.SettingsRepository

class DownloadsMenuComponent(
    componentContext: ComponentContext,
    private val onIntent: (Intent) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()

    private val scope = CoroutineScope(Dispatchers.Default)

    val state: StateFlow<State> = settingsRepository.filesSortSettingFlow.map(
        coroutineScope = scope,
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
}