package ru.blays.hub.core.domain.components.settingsComponents.developerMenu

import ru.blays.hub.core.domain.AppComponentContext

class DeveloperMenuComponent private constructor(
    componentContext: AppComponentContext,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    fun onOutput(output: Output) {
        onOutput.invoke(output)
    }

    sealed class Output {
        data object NavigateBack: Output()
        data object Logs: Output()
    }

    class Factory {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): DeveloperMenuComponent {
            return DeveloperMenuComponent(
                componentContext = componentContext,
                onOutput = onOutput
            )
        }
    }
}