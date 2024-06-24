package ru.blays.hub.core.ui.screens.rootContent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.blays.hub.core.logic.components.rootComponents.RootComponent
import ru.blays.hub.core.ui.values.LocalStackAnimator

@Composable
fun RootContent(
    component: RootComponent
) {
    Children(
        modifier = Modifier.fillMaxSize(),
        stack = component.childStack,
        animation = stackAnimation(LocalStackAnimator.current)
    ) {
        when(val child = it.instance) {
            is RootComponent.Child.Tabs -> TabsContent(component = child.component)
            is RootComponent.Child.Splash -> SplashContent()
            is RootComponent.Child.RootDialog -> DialogsContent(component = child.component)
        }
    }
}

