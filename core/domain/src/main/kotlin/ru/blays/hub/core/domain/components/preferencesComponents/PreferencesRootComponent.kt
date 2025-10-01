package ru.blays.hub.core.domain.components.preferencesComponents

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.components.preferencesComponents.catalogs.CatalogsPreferencesComponent
import ru.blays.hub.core.domain.components.preferencesComponents.developerMenu.DeveloperMenuRootComponent

class PreferencesRootComponent private constructor(
    componentContext: AppComponentContext,
    private val preferencesComponentFactory: PreferencesComponent.Factory,
    private val developerComponentFactory: DeveloperMenuRootComponent.Factory,
    private val catalogComponentFactory: CatalogsPreferencesComponent.Factory,
    private val mainComponentFactory: MainPreferencesComponent.Factory,
    private val updateComponentFactory: SelfUpdatePreferencesComponent.Factory,
    private val themeComponentFactory: ThemePreferencesComponent.Factory,
): AppComponentContext by componentContext {
    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.SettingGroups,
        serializer = Configuration.serializer(),
        handleBackButton = true,
        childFactory = ::childFactory
    )

    fun onBackClicked() = navigation.pop()

    private fun childFactory(
        configuration: Configuration,
        childContext: AppComponentContext
    ): Child {
        return when(configuration) {
            Configuration.SettingGroups -> Child.SettingGroups(
                preferencesComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onSettingsOutput
                )
            )
            Configuration.DeveloperMenu -> Child.DeveloperMenu(
                developerComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onDevMenuOutput
                )
            )
            Configuration.CatalogsSetting -> Child.CatalogsSetting(
                catalogComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onCatalogsSettingOutput
                )
            )
            Configuration.MainSettings -> Child.MainSettings(
                mainComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onMainSettingsOutput,
                )
            )
            Configuration.SelfUpdateSettings -> Child.SelfUpdateSettings(
                updateComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onSelfUpdateSettingsOutput
                )
            )
            Configuration.ThemeSettings -> Child.ThemeSettings(
                themeComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onThemeSettingsOutput
                )
            )
        }
    }

    private fun onSettingsOutput(output: PreferencesComponent.Output) {
        when(output) {
            PreferencesComponent.Output.DeveloperMenu -> navigation.pushNew(
                Configuration.DeveloperMenu
            )
            PreferencesComponent.Output.CatalogsSetting -> navigation.pushNew(
                Configuration.CatalogsSetting
            )
            PreferencesComponent.Output.MainSettings -> navigation.pushNew(
                Configuration.MainSettings
            )
            PreferencesComponent.Output.ThemeSettings -> navigation.pushNew(
                Configuration.ThemeSettings
            )
            PreferencesComponent.Output.SelfUpdateSettings -> navigation.pushNew(
                Configuration.SelfUpdateSettings
            )
        }
    }
    private fun onDevMenuOutput(output: DeveloperMenuRootComponent.Output) {
        when(output) {
            DeveloperMenuRootComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onCatalogsSettingOutput(output: CatalogsPreferencesComponent.Output) {
        when(output) {
            CatalogsPreferencesComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onThemeSettingsOutput(output: ThemePreferencesComponent.Output) {
        when(output) {
            ThemePreferencesComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onMainSettingsOutput(output: MainPreferencesComponent.Output) {
        when(output) {
            MainPreferencesComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onSelfUpdateSettingsOutput(output: SelfUpdatePreferencesComponent.Output) {
        when(output) {
            SelfUpdatePreferencesComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object SettingGroups: Configuration()
        @Serializable
        data object ThemeSettings: Configuration()
        @Serializable
        data object CatalogsSetting: Configuration()
        @Serializable
        data object MainSettings: Configuration()
        @Serializable
        data object SelfUpdateSettings: Configuration()
        @Serializable
        data object DeveloperMenu: Configuration()
    }

    sealed class Child {
        data class SettingGroups(
            val component: PreferencesComponent
        ): Child()
        data class ThemeSettings(val component: ThemePreferencesComponent): Child()
        data class CatalogsSetting(val component: CatalogsPreferencesComponent): Child()
        data class MainSettings(val component: MainPreferencesComponent): Child()
        data class SelfUpdateSettings(val component: SelfUpdatePreferencesComponent): Child()
        data class DeveloperMenu(
            val component: DeveloperMenuRootComponent
        ): Child()
    }

    class Factory(
        private val preferencesComponentFactory: PreferencesComponent.Factory,
        private val developerComponentFactory: DeveloperMenuRootComponent.Factory,
        private val catalogComponentFactory: CatalogsPreferencesComponent.Factory,
        private val mainComponentFactory: MainPreferencesComponent.Factory,
        private val updateComponentFactory: SelfUpdatePreferencesComponent.Factory,
        private val themeComponentFactory: ThemePreferencesComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,

        ): PreferencesRootComponent {
            return PreferencesRootComponent(
                componentContext = componentContext,
                preferencesComponentFactory = preferencesComponentFactory,
                developerComponentFactory = developerComponentFactory,
                catalogComponentFactory = catalogComponentFactory,
                mainComponentFactory = mainComponentFactory,
                updateComponentFactory = updateComponentFactory,
                themeComponentFactory = themeComponentFactory,
            )
        }
    }
}
