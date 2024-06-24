package ru.blays.hub.core.ui.elements.bubbleTabBar

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color

@Stable
class BubbleTabBarState private constructor(
    tabs: List<BubbleTabConfig>,
    private val initialTabIndex: Int = 0
) {
    constructor(tabs: List<BubbleTabConfig>): this(tabs, 0) {
        tabs.ifEmpty { throw IllegalStateException("Tabs count < 1") }
    }

    var tabs: List<BubbleTabConfig> = tabs
        private set

    private var _selectedTabIndex = mutableIntStateOf(initialTabIndex)

    val selectedTabIndex by derivedStateOf {
        _selectedTabIndex.intValue
    }

    fun selectTab(tab: BubbleTabConfig) {
        val index = tabs.indexOfFirst { it.key == tab.key }
        _selectedTabIndex.intValue = index
    }
    fun selectTab(index: Int) {
        _selectedTabIndex.intValue = index
    }

    internal fun updateTabs(tabs: List<BubbleTabConfig>) {
        this.tabs = tabs
    }

    companion object {
        fun Saver(tabs: List<BubbleTabConfig>): Saver<BubbleTabBarState, Int> {
            return Saver(
                save = { it.selectedTabIndex },
                restore = {
                    BubbleTabBarState(
                        tabs = tabs,
                        initialTabIndex = it
                    )
                }
            )
        }
    }
}

@Suppress("NonSkippableComposable")
@Composable
fun rememberBubbleTabBarState(
    tabs: List<BubbleTabConfig>
): BubbleTabBarState {
    val state = rememberSaveable(
        saver = BubbleTabBarState.Saver(tabs)
    ) {
        BubbleTabBarState(tabs)
    }
    LaunchedEffect(tabs) {
        state.updateTabs(tabs)
    }
    return state
}

@Stable
data class BubbleTabConfig (
    val key: Any,
    val title: String,
    @DrawableRes val unselectedIcon: Int,
    @DrawableRes val selectedIcon: Int = unselectedIcon,
    val contentDescription: String? = null,
    val colors: BubbleTabColors
) {
    constructor(
        title: String,
        key: () -> Any = { title },
        @DrawableRes iconRes: Int,
        contentDescription: String? = null,
        colors: BubbleTabColors
    ): this(
        key = key(),
        title = title,
        unselectedIcon = iconRes,
        contentDescription = contentDescription,
        colors = colors
    )
}

@Stable
data class BubbleTabColors(
    val activeContainerColor: Color,
    val activeContentColor: Color,
)