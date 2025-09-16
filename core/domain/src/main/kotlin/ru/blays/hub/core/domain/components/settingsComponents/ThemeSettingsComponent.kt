package ru.blays.hub.core.domain.components.settingsComponents

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.ThemeSettings
import ru.blays.hub.core.preferences.proto.ThemeSettingsKt
import ru.blays.hub.core.preferences.proto.copy

class ThemeSettingsComponent private constructor(
    componentContext: AppComponentContext,
    private val settingsRepository: SettingsRepository,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    val themeSettings: StateFlow<ThemeSettings>
        get() = settingsRepository.themeSettingsFlow

    fun onOutput(output: Output) = onOutput.invoke(output)

    fun setValue(transform: ThemeSettingsKt.Dsl.() -> Unit) {
        componentScope.launch {
            settingsRepository.setValue {
                themeSettings = themeSettings.copy(transform)
            }
        }
    }

    sealed class Output {
        data object NavigateBack : Output()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): ThemeSettingsComponent {
            return ThemeSettingsComponent(
                componentContext = componentContext,
                settingsRepository = settingsRepository,
                onOutput = onOutput
            )
        }
    }
}