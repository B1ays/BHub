package ru.blays.hub.core.ui.screens.preferencesContent.developerMenuContent

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
import ru.blays.hub.core.domain.components.preferencesComponents.developerMenu.DeveloperMenuRootComponent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun DeveloperMenuRootContent(
    modifier: Modifier = Modifier,
    component: DeveloperMenuRootComponent
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
            is DeveloperMenuRootComponent.Child.Logs -> DeveloperMenuLogsContent(component = child.component)
            is DeveloperMenuRootComponent.Child.Menu -> DeveloperMenuContent(component = child.component)
        }
    }
}