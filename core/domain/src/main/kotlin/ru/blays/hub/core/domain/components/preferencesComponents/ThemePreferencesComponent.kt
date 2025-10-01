package ru.blays.hub.core.domain.components.preferencesComponents

import kotlinx.coroutines.flow.StateFlow
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.AppThemeAccessor
import ru.blays.hub.core.domain.data.ThemePreferenceModel
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.accessor.update
import ru.blays.preferences.api.PreferencesHolder

class ThemePreferencesComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    private val themePreferenceValue = preferencesHolder.getValue(AppThemeAccessor)

    val themePreferenceFlow: StateFlow<ThemePreferenceModel> = themePreferenceValue

    fun onOutput(output: Output) = onOutput.invoke(output)

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.ChangeAccentType -> themePreferenceValue.update {
                it.copy(colorAccentType = intent.value)
            }
            is Intent.ChangeAmoledTheme -> themePreferenceValue.update {
                it.copy(amoledTheme = intent.value)
            }
            is Intent.ChangeThemeType -> themePreferenceValue.update {
                it.copy(themeType = intent.value)
            }
        }
    }

    sealed class Intent {
        data class ChangeThemeType(val value: ThemePreferenceModel.ThemeType): Intent()
        data class ChangeAccentType(val value: ThemePreferenceModel.AccentType): Intent()
        data class ChangeAmoledTheme(val value: Boolean): Intent()
    }

    sealed class Output {
        data object NavigateBack : Output()
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): ThemePreferencesComponent {
            return ThemePreferencesComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                onOutput = onOutput
            )
        }
    }
}