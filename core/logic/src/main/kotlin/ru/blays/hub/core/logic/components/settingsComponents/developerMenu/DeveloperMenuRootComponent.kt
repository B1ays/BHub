package ru.blays.hub.core.logic.components.settingsComponents.developerMenu

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import kotlinx.serialization.Serializable

class DeveloperMenuRootComponent(
    componentContext: ComponentContext,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext {
    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.Menu,
        handleBackButton = true,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    fun onBackClicked() = navigation.pop()

    private fun childFactory(configuration: Configuration, childContext: ComponentContext): Child {
        return when(configuration) {
            Configuration.Logs -> Child.Logs(
                DeveloperMenuLogsComponent(
                    componentContext = childContext,
                    onOutput = ::onLogsOutput
                )
            )
            Configuration.Menu -> Child.Menu(
                DeveloperMenuComponent(
                    componentContext = childContext,
                    onOutput = ::onMenuOutput
                )
            )
        }
    }

    private fun onMenuOutput(output: DeveloperMenuComponent.Output) {
        when(output) {
            DeveloperMenuComponent.Output.Logs -> navigation.push(Configuration.Logs)
            DeveloperMenuComponent.Output.NavigateBack -> {
                onOutput.invoke(Output.NavigateBack)
            }
        }
    }

    private fun onLogsOutput(output: DeveloperMenuLogsComponent.Output) {
        when(output) {
            DeveloperMenuLogsComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    private fun internalOnOutput(output: Output) {
        when(output) {
            Output.NavigateBack -> {
                if(childStack.backStack.isNotEmpty()) {
                    navigation.pop()
                } else {
                    onOutput.invoke(Output.NavigateBack)
                }
            }
        }
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Menu: Configuration()
        @Serializable
        data object Logs: Configuration()
    }

    sealed class Child {
        data class Menu(
            val component: DeveloperMenuComponent
        ): Child()
        data class Logs(
            val component: DeveloperMenuLogsComponent
        ): Child()
    }
}