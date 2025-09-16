package ru.blays.hub.core.domain.components

import ru.blays.hub.core.domain.AppComponentContext

class AboutComponent private constructor(
    componentContext: AppComponentContext,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    fun onOutput(output: Output) {
        onOutput.invoke(output)
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    class Factory {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): AboutComponent {
            return AboutComponent(
                componentContext = componentContext,
                onOutput = onOutput
            )
        }
    }
}