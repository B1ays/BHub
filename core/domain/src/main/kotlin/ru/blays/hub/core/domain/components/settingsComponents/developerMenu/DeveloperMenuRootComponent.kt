package ru.blays.hub.core.domain.components.settingsComponents.developerMenu

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext

class DeveloperMenuRootComponent private constructor(
    componentContext: AppComponentContext,
    private val logsComponentFactory: DeveloperMenuLogsComponent.Factory,
    private val menuComponentFactory: DeveloperMenuComponent.Factory,
    private val onOutput: (Output) -> Unit,
): AppComponentContext by componentContext {
    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.Menu,
        handleBackButton = true,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    fun onBackClicked() = navigation.pop()

    private fun childFactory(
        configuration: Configuration,
        childContext: AppComponentContext
    ): Child {
        return when(configuration) {
            Configuration.Logs -> Child.Logs(
                logsComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onLogsOutput
                )
            )
            Configuration.Menu -> Child.Menu(
                menuComponentFactory(
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

    class Factory(
        private val logsComponentFactory: DeveloperMenuLogsComponent.Factory,
        private val menuComponentFactory: DeveloperMenuComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit,
        ): DeveloperMenuRootComponent {
            return DeveloperMenuRootComponent(
                componentContext = componentContext,
                logsComponentFactory = logsComponentFactory,
                menuComponentFactory = menuComponentFactory,
                onOutput = onOutput
            )
        }
    }
}