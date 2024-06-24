package ru.blays.hub.core.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

class DirectionalLazyListState(
    private val lazyListState: LazyListState
) {
    private var positionY = lazyListState.firstVisibleItemScrollOffset
    private var visibleItem = lazyListState.firstVisibleItemIndex

    val scrollDirection by derivedStateOf {
        if (!lazyListState.isScrollInProgress) {
            ScrollDirection.None
        } else {
            val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
            val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset

            if (firstVisibleItemIndex == visibleItem) {
                val direction = if (firstVisibleItemScrollOffset == positionY) {
                    ScrollDirection.None
                } else if (firstVisibleItemScrollOffset > positionY) {
                    ScrollDirection.Down
                } else {
                    ScrollDirection.Up
                }
                positionY = firstVisibleItemScrollOffset

                return@derivedStateOf direction
            } else {
                val direction = if (firstVisibleItemIndex > visibleItem) {
                    ScrollDirection.Down
                } else {
                    ScrollDirection.Up
                }
                positionY = firstVisibleItemScrollOffset
                visibleItem = firstVisibleItemIndex
                return@derivedStateOf direction
            }
        }
    }
}

@Composable
fun rememberDirectionalLazyListState(
    lazyListState: LazyListState,
): DirectionalLazyListState {
    return remember {
        DirectionalLazyListState(lazyListState)
    }
}

enum class ScrollDirection {
    Up, Down, None
}