package ru.blays.hub.core.domain.components.preferencesComponents

import androidx.compose.runtime.Stable
import ru.blays.hub.core.domain.AppComponentContext

@Stable
class PreferencesComponent private constructor(
    componentContext: AppComponentContext,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext{
    fun onOutput(output: Output) = onOutput.invoke(output)

    sealed class Output {
        data object DeveloperMenu : Output()
        data object ThemeSettings: Output()
        data object CatalogsSetting: Output()
        data object MainSettings: Output()
        data object SelfUpdateSettings: Output()
    }

    class Factory {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): PreferencesComponent {
            return PreferencesComponent(
                componentContext = componentContext,
                onOutput = onOutput
            )
        }
    }
}