package ru.blays.hub.core.ui.elements.tabs

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun CustomTabRow(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    containerColor: Color = TabRowDefaults.primaryContainerColor,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    shape: Shape = RectangleShape,
    border: BorderStroke? = null,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
        if (selectedTabIndex < tabPositions.size) {
            CustomTabIndicator(currentPagePosition = tabPositions[selectedTabIndex])
        }
    },
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit

) {
    Surface(
        modifier = modifier.selectableGroup(),
        color = containerColor,
        contentColor = contentColor,
        shape = shape,
        border = border
    ) {
        SubcomposeLayout(Modifier.fillMaxWidth()) { constraints ->
            val tabRowWidth = constraints.maxWidth
            val tabMeasurables = subcompose(TabSlots.Tabs, tabs)
            val tabCount = tabMeasurables.size
            var tabWidth = 0
            if (tabCount > 0) {
                tabWidth = (tabRowWidth / tabCount)
            }
            val tabRowHeight = tabMeasurables.fold(initial = 0) { max, curr ->
                maxOf(curr.maxIntrinsicHeight(tabWidth), max)
            }

            val tabPlaceables = tabMeasurables.map {
                it.measure(
                    constraints.copy(
                        minWidth = tabWidth,
                        maxWidth = tabWidth,
                        minHeight = tabRowHeight,
                        maxHeight = tabRowHeight,
                    )
                )
            }

            val tabPositions = List(tabCount) { index ->
                var contentWidth =
                    minOf(tabMeasurables[index].maxIntrinsicWidth(tabRowHeight), tabWidth).toDp()
                contentWidth -= HorizontalTextPadding * 2
                TabPosition(tabWidth.toDp() * index, tabWidth.toDp(), contentWidth)
            }

            layout(tabRowWidth, tabRowHeight) {
                subcompose(TabSlots.Indicator) {
                    indicator(tabPositions)
                }.forEach {
                    it.measure(Constraints.fixed(tabRowWidth, tabRowHeight)).placeRelative(0, 0)
                }

                tabPlaceables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(index * tabWidth, 0)
                }

                subcompose(TabSlots.Divider, divider).forEach {
                    val placeable = it.measure(constraints.copy(minHeight = 0))
                    placeable.placeRelative(0, tabRowHeight - placeable.height)
                }
            }
        }
    }
}

@Composable
fun CustomTabIndicator(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    widthFill: Float = 1F,
    padding: Dp = Dp.Unspecified,
    color: Color = MaterialTheme.colorScheme.primary,
    currentPagePosition: TabPosition
) {
    Box(
        modifier
            .padding(padding)
            .customTabIndicatorOffset(currentPagePosition, widthFill, padding)
            .fillMaxHeight()
            .clip(shape)
            .background(color = color)
    )
}

@Suppress("AnimateAsStateLabel")
private fun Modifier.customTabIndicatorOffset(
    currentTabPosition: TabPosition,
    widthFill: Float,
    padding: Dp
): Modifier = composed {
    val currentTabWidth by animateDpAsState(
        targetValue = currentTabPosition.width - (padding * 2),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    val indicatorOffset by animateDpAsState(
        targetValue = currentTabPosition.left,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )

    val initialOffset = (currentTabWidth - (currentTabWidth * widthFill)) / 2

    fillMaxWidth()
        .wrapContentSize(Alignment.CenterStart)
        .offset {
            IntOffset(
                x = initialOffset.roundToPx() + indicatorOffset.roundToPx(),
                y = 0
            )
        }
        .width(currentTabWidth * widthFill)
}

@Composable
fun CustomTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = 32.dp,
    selectedContentColor: Color = LocalContentColor.current,
    unselectedContentColor: Color = selectedContentColor,
    content: @Composable ColumnScope.() -> Unit
) {

    CustomTabTransition(selectedContentColor, unselectedContentColor, selected) {
        Column(
            modifier = modifier
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Tab
                )
                .fillMaxWidth()
                .requiredHeightIn(min = minHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Suppress("UpdateTransitionLabel", "TransitionPropertiesLabel")
@Composable
private fun CustomTabTransition(
    activeColor: Color,
    inactiveColor: Color,
    selected: Boolean,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(selected)
    val color by transition.animateColor(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = TabFadeInAnimationDuration,
                    delayMillis = TabFadeInAnimationDelay,
                    easing = LinearEasing
                )
            } else {
                tween(
                    durationMillis = TabFadeOutAnimationDuration,
                    easing = LinearEasing
                )
            }
        }
    ) {
        if (it) activeColor else inactiveColor
    }
    CompositionLocalProvider(
        LocalContentColor provides color,
        content = content
    )
}

private const val TabFadeInAnimationDuration = 150
private const val TabFadeInAnimationDelay = 100
private const val TabFadeOutAnimationDuration = 100

private val HorizontalTextPadding = 16.dp

private enum class TabSlots {
    Tabs,
    Divider,
    Indicator
}

class TabPosition internal constructor(val left: Dp, val width: Dp, val contentWidth: Dp) {

    private val right: Dp get() = left + width

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TabPosition) return false

        if (left != other.left) return false
        if (width != other.width) return false
        if (contentWidth != other.contentWidth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + width.hashCode()
        result = 31 * result + contentWidth.hashCode()
        return result
    }

    override fun toString(): String {
        return "TabPosition(left=$left, right=$right, width=$width, contentWidth=$contentWidth)"
    }
}
