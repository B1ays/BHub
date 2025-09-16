package ru.blays.hub.core.domain.components.settingsComponents

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.components.settingsComponents.catalogsSettings.CatalogsSettingComponent
import ru.blays.hub.core.domain.components.settingsComponents.developerMenu.DeveloperMenuRootComponent

class SettingsRootComponent private constructor(
    componentContext: AppComponentContext,
    private val settingsComponentFactory: SettingsComponent.Factory,
    private val developerComponentFactory: DeveloperMenuRootComponent.Factory,
    private val catalogComponentFactory: CatalogsSettingComponent.Factory,
    private val mainComponentFactory: MainSettingsComponent.Factory,
    private val updateComponentFactory: SelfUpdateSettingsComponent.Factory,
    private val themeComponentFactory: ThemeSettingsComponent.Factory,
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
                settingsComponentFactory(
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

    private fun onSettingsOutput(output: SettingsComponent.Output) {
        when(output) {
            SettingsComponent.Output.DeveloperMenu -> navigation.push(
                Configuration.DeveloperMenu
            )
            SettingsComponent.Output.CatalogsSetting -> navigation.push(
                Configuration.CatalogsSetting
            )
            SettingsComponent.Output.MainSettings -> navigation.push(
                Configuration.MainSettings
            )
            SettingsComponent.Output.ThemeSettings -> navigation.push(
                Configuration.ThemeSettings
            )
            SettingsComponent.Output.SelfUpdateSettings -> navigation.push(
                Configuration.SelfUpdateSettings
            )
        }
    }
    private fun onDevMenuOutput(output: DeveloperMenuRootComponent.Output) {
        when(output) {
            DeveloperMenuRootComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onCatalogsSettingOutput(output: CatalogsSettingComponent.Output) {
        when(output) {
            CatalogsSettingComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onThemeSettingsOutput(output: ThemeSettingsComponent.Output) {
        when(output) {
            ThemeSettingsComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onMainSettingsOutput(output: MainSettingsComponent.Output) {
        when(output) {
            MainSettingsComponent.Output.NavigateBack -> navigation.pop()
        }
    }
    private fun onSelfUpdateSettingsOutput(output: SelfUpdateSettingsComponent.Output) {
        when(output) {
            SelfUpdateSettingsComponent.Output.NavigateBack -> navigation.pop()
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
            val component: SettingsComponent
        ): Child()
        data class ThemeSettings(val component: ThemeSettingsComponent): Child()
        data class CatalogsSetting(val component: CatalogsSettingComponent): Child()
        data class MainSettings(val component: MainSettingsComponent): Child()
        data class SelfUpdateSettings(val component: SelfUpdateSettingsComponent): Child()
        data class DeveloperMenu(
            val component: DeveloperMenuRootComponent
        ): Child()
    }

    class Factory(
        private val settingsComponentFactory: SettingsComponent.Factory,
        private val developerComponentFactory: DeveloperMenuRootComponent.Factory,
        private val catalogComponentFactory: CatalogsSettingComponent.Factory,
        private val mainComponentFactory: MainSettingsComponent.Factory,
        private val updateComponentFactory: SelfUpdateSettingsComponent.Factory,
        private val themeComponentFactory: ThemeSettingsComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,

        ): SettingsRootComponent {
            return SettingsRootComponent(
                componentContext = componentContext,
                settingsComponentFactory = settingsComponentFactory,
                developerComponentFactory = developerComponentFactory,
                catalogComponentFactory = catalogComponentFactory,
                mainComponentFactory = mainComponentFactory,
                updateComponentFactory = updateComponentFactory,
                themeComponentFactory = themeComponentFactory,
            )
        }
    }
}
