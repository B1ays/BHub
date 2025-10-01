package ru.blays.hub.core.domain.components.preferencesComponents

import com.arkivanov.decompose.childContext
import kotlinx.coroutines.flow.StateFlow
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.CheckSelfUpdatesAccessor
import ru.blays.hub.core.domain.SelfUpdatesChannelAccessor
import ru.blays.hub.core.domain.components.SelfUpdateComponent
import ru.blays.hub.core.domain.data.UpdateChannelType
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder

class SelfUpdatePreferencesComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val selfUpdateComponentFactory: SelfUpdateComponent.Factory,
    private val onOutput: (output: Output) -> Unit
): AppComponentContext by componentContext {
    private val checkUpdatesValue = preferencesHolder.getValue(CheckSelfUpdatesAccessor)
    private val updatesChannelValue = preferencesHolder.getValue(SelfUpdatesChannelAccessor)

    val selfUpdateComponent = selfUpdateComponentFactory(
        componentContext = childContext("selfUpdateComponent"),
        checkOnCreate = false
    )

    val checkUpdatesFlow: StateFlow<Boolean> = checkUpdatesValue
    val updatesChannelFlow: StateFlow<UpdateChannelType> = updatesChannelValue

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.ChangeCheckUpdates -> checkUpdatesValue.updateValue(intent.value)
            is Intent.ChangeUpdatesChannel -> updatesChannelValue.updateValue(intent.value)
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    sealed class Intent {
        data class ChangeCheckUpdates(val value: Boolean) : Intent()
        data class ChangeUpdatesChannel(val value: UpdateChannelType) : Intent()
    }

    sealed class Output {
        data object NavigateBack : Output()
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
        private val selfUpdateComponentFactory: SelfUpdateComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (output: Output) -> Unit
        ): SelfUpdatePreferencesComponent {
            return SelfUpdatePreferencesComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                selfUpdateComponentFactory = selfUpdateComponentFactory,
                onOutput = onOutput
            )
        }
    }
}