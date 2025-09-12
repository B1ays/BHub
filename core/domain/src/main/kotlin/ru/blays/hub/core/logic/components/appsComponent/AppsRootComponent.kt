package ru.blays.hub.core.logic.components.appsComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import kotlinx.serialization.Serializable
import ru.blays.hub.core.logic.components.appPageComponents.AppComponent
import ru.blays.hub.core.logic.data.models.AppCardModel

class AppsRootComponent(
    componentContext: ComponentContext
): ComponentContext by componentContext {
    private val navigation = StackNavigation<Configuration>()

    private var cacheAppsChild: Child.Apps? = null

    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.Apps,
        handleBackButton = true,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    fun onBackClicked() = navigation.pop()

    private fun childFactory(
        configuration: Configuration,
        childContext: ComponentContext
    ): Child {
        return when(configuration) {
            is Configuration.App -> Child.App(
                AppComponent(
                    componentContext = childContext,
                    app = configuration.app,
                    onRefresh = {
                        cacheAppsChild?.component?.sendIntent(
                            AppsComponent.Intent.Refresh
                        )
                    },
                    onOutput = ::onAppOutput
                )
            )
            is Configuration.Apps -> cacheAppsChild ?: Child.Apps(
                AppsComponent(
                    componentContext = childContext,
                    onOutput = ::onAppsOutput
                )
            ).also {
                cacheAppsChild = it
            }
        }
    }

    private fun onAppsOutput(output: AppsComponent.Output) {
        when(output) {
            is AppsComponent.Output.OpenApp -> {
                navigation.push(Configuration.App(output.app))
            }
        }
    }

    private fun onAppOutput(output: AppComponent.Output) {
        when(output) {
            is AppComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Apps: Configuration()
        @Serializable
        data class App(val app: AppCardModel): Configuration()
    }

    sealed class Child {
        data class Apps(val component: AppsComponent): Child()
        data class App(val component: AppComponent): Child()
    }
}