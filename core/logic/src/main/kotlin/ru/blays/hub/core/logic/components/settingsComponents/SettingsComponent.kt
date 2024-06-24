package ru.blays.hub.core.logic.components.settingsComponents

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ComponentContext
import org.koin.core.component.KoinComponent

@Stable
class SettingsComponent(
    componentContext: ComponentContext,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    fun onOutput(output: Output) {
        onOutput.invoke(output)
    }

    sealed class Output {
        data object DeveloperMenu : Output()
        data object ThemeSettings: Output()
        data object CatalogsSetting: Output()
        data object MainSettings: Output()
    }
}