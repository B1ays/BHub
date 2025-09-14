package ru.blays.hub.core.domain.components.settingsComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.ThemeSettings
import ru.blays.hub.core.preferences.proto.ThemeSettingsKt
import ru.blays.hub.core.preferences.proto.copy

class ThemeSettingsComponent(
    componentContext: ComponentContext,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val themeSettings: StateFlow<ThemeSettings>
        get() = settingsRepository.themeSettingsFlow

    fun onOutput(output: Output) = onOutput.invoke(output)

    fun setValue(transform: ThemeSettingsKt.Dsl.() -> Unit) {
        coroutineScope.launch {
            settingsRepository.setValue {
                themeSettings = themeSettings.copy(transform)
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