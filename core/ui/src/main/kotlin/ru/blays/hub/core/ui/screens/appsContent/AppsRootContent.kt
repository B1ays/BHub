package ru.blays.hub.core.ui.screens.appsContent

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.androidPredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.blays.hub.core.logic.components.appsComponent.AppsRootComponent
import ru.blays.hub.core.ui.screens.appPageContent.AppPageContent2

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun AppsRootContent(component: AppsRootComponent) {
    Children(
        stack = component.childStack,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            fallbackAnimation = stackAnimation(fade() + scale()),
            selector = { backEvent, _, _ -> androidPredictiveBackAnimatable(backEvent) },
            onBack = component::onBackClicked,
        )
    ) {
        when(val child = it.instance) {
            is AppsRootComponent.Child.App -> AppPageContent2(component = child.component)
            is AppsRootComponent.Child.Apps -> AppsContent(component = child.component)
        }
    }
}