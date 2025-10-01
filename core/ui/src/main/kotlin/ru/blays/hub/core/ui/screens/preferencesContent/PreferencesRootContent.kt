package ru.blays.hub.core.ui.screens.preferencesContent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.blays.hub.core.domain.components.preferencesComponents.PreferencesRootComponent
import ru.blays.hub.core.ui.screens.preferencesContent.developerMenuContent.DeveloperMenuRootContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsRootContent(
    modifier: Modifier = Modifier,
    component: PreferencesRootComponent
) {
    Children(
        modifier = modifier,
        stack = component.childStack,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            fallbackAnimation = stackAnimation(fade() + scale()),
            selector = { backEvent, _, _ -> androidPredictiveBackAnimatable(backEvent) },
            onBack = component::onBackClicked,
        )
    ) {
        when(val child = it.instance) {
            is PreferencesRootComponent.Child.DeveloperMenu -> DeveloperMenuRootContent(component = child.component)
            is PreferencesRootComponent.Child.SettingGroups -> SettingsContent(component = child.component)
            is PreferencesRootComponent.Child.CatalogsSetting -> CatalogsSettingScreen(component = child.component)
            is PreferencesRootComponent.Child.MainSettings -> MainSettingsContent(component = child.component)
            is PreferencesRootComponent.Child.ThemeSettings -> ThemeSettingsContent(component = child.component)
            is PreferencesRootComponent.Child.SelfUpdateSettings -> SelfUpdateSettingsContent(component = child.component)
        }
    }
}