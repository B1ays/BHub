package ru.blays.hub.core.domain.components.settingsComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.domain.components.SelfUpdateComponent
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.UpdateChannel

class SelfUpdateSettingsComponent(
    componentContext: ComponentContext,
    private val onOutput: (output: Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val settingsRepository: SettingsRepository by inject()

    val selfUpdateComponent: SelfUpdateComponent = SelfUpdateComponent(
        componentContext = childContext("selfUpdateComponent"),
        checkOnCreate = false
    )

    val checkUpdatesFlow: StateFlow<Boolean>
        get() = settingsRepository.checkUpdatesFlow
    val updatesChannelFlow: StateFlow<UpdateChannel>
        get() = settingsRepository.updateChannelFlow

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.ChangeCheckUpdates -> settingsRepository.setValue {
                checkUpdates = intent.value
            }
            is Intent.ChangeUpdatesChannel -> settingsRepository.setValue {
                updateChannel = intent.value
            }
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    sealed class Intent {
        data class ChangeCheckUpdates(val value: Boolean) : Intent()
        data class ChangeUpdatesChannel(val value: UpdateChannel) : Intent()
    }

    sealed class Output {
        data object NavigateBack : Output()
    }
}