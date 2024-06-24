package ru.blays.hub.core.ui.elements.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import ru.blays.hub.core.ui.utils.ScrollDirection
import ru.blays.hub.core.ui.utils.rememberDirectionalLazyListState


@Composable
fun ScrollToStartFAB(lazyListState: LazyListState) {
    val scrollDirectionProvider = rememberDirectionalLazyListState(lazyListState)
    val scrollDirection = scrollDirectionProvider.scrollDirection
    var showFAB by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(scrollDirection) {
        when(scrollDirection) {
            ScrollDirection.Up -> {
                showFAB = true
            }
            ScrollDirection.Down -> {
                showFAB = false
            }
            else -> {}
        }
    }

    LaunchedEffect(
        lazyListState.canScrollBackward,
        lazyListState.canScrollForward
    ) {
        if(!lazyListState.canScrollBackward) {
            showFAB = false
        }
        if(!lazyListState.canScrollForward) {
            showFAB = true
        }
    }

    AnimatedVisibility(
        visible = showFAB,
        enter = fadeIn(initialAlpha = 1f),
        exit = fadeOut(targetAlpha = 0f),
    ) {
        FloatingActionButton(
            onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(0)
                    showFAB = false
                }
            },
            shape = CircleShape,
        ) {
            /*Icon(
                painter = painterResource(Res.drawable.ic_arrow_up),
                contentDescription = stringResource(Res.string.content_description_icon_up),
                modifier = Modifier.size(32.dp),
            )*/ // TODO import icon
        }
    }
}