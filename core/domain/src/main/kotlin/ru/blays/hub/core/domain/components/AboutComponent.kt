package ru.blays.hub.core.domain.components

import com.arkivanov.decompose.ComponentContext

class AboutComponent(
    componentContext: ComponentContext,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext {
    fun onOutput(output: Output) {
        onOutput.invoke(output)
    }

    sealed class Output {
        data object NavigateBack: Output()
    }
}