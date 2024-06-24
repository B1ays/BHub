package ru.blays.hub.core.logic.components.settingsComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import kotlinx.serialization.Serializable
import ru.blays.hub.core.logic.components.settingsComponents.catalogsSettings.CatalogsSettingComponent
import ru.blays.hub.core.logic.components.settingsComponents.developerMenu.DeveloperMenuRootComponent

class SettingsRootComponent(
    componentContext: ComponentContext
): ComponentContext by componentContext {
    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        initialConfiguration = Configuration.SettingGroups,
        serializer = Configuration.serializer(),
        handleBackButton = true,
        childFactory = ::childFactory
    )

    fun onBackClicked() = navigation.pop()

    private fun childFactory(configuration: Configuration, childContext: ComponentContext): Child {
        return when(configuration) {
            Configuration.SettingGroups -> Child.SettingGroups(
                SettingsComponent(
                    componentContext = childContext,
                    onOutput = ::onSettingsOutput
                )
            )
            Configuration.DeveloperMenu -> Child.DeveloperMenu(
                DeveloperMenuRootComponent(
                    componentContext = childContext,
                    onOutput = ::onDevMenuOutput
                )
            )
            Configuration.CatalogsSetting -> Child.CatalogsSetting(
                CatalogsSettingComponent(
                    componentContext = childContext,
                    onOutput = ::onCatalogsSettingOutput
                )
            )
            Configuration.MainSettings -> Child.MainSettings(
                MainSettingsComponent(
                    componentContext = childContext,
                    onOutput = ::onCommonSettingsOutput
                )
            )
            Configuration.ThemeSettings -> Child.ThemeSettings(
                ThemeSettingsComponent(
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
    private fun onCommonSettingsOutput(output: MainSettingsComponent.Output) {
        when(output) {
            MainSettingsComponent.Output.NavigateBack -> navigation.pop()
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
        data object DeveloperMenu: Configuration()
    }
    sealed class Child {
        data class SettingGroups(
            val component: SettingsComponent
        ): Child()
        data class ThemeSettings(val component: ThemeSettingsComponent): Child()
        data class CatalogsSetting(val component: CatalogsSettingComponent): Child()
        data class MainSettings(val component: MainSettingsComponent): Child()
        data class DeveloperMenu(
            val component: DeveloperMenuRootComponent
        ): Child()
    }
}