package ru.blays.hub.core.domain.components.appsComponent

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.components.appPageComponents.AppComponent
import ru.blays.hub.core.domain.data.models.AppCardModel

class AppsRootComponent private constructor(
    componentContext: AppComponentContext,
    private val appComponentFactory: AppComponent.Factory,
    private val appsComponentFactory: AppsComponent.Factory,
): AppComponentContext by componentContext {
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
        childContext: AppComponentContext
    ): Child {
        return when(configuration) {
            is Configuration.App -> Child.App(
                appComponentFactory(
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
                appsComponentFactory(
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
                navigation.pushNew(Configuration.App(output.app))
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

    class Factory(
        private val appComponentFactory: AppComponent.Factory,
        private val appsComponentFactory: AppsComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
        ): AppsRootComponent {
            return AppsRootComponent(
                componentContext = componentContext,
                appComponentFactory = appComponentFactory,
                appsComponentFactory = appsComponentFactory
            )
        }
    }
}