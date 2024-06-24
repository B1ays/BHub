package ru.blays.hub.core.ui.elements.bubbleTabBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.theme.disableRippleConfig
import ru.blays.hub.core.ui.utils.thenIf

@Composable
fun BubbleTabBar(
    modifier: Modifier = Modifier,
    state: BubbleTabBarState,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onValueChange: (index: Int, config: BubbleTabConfig) -> Unit = { _, _ -> }
) {
    val activeTabIndex = state.selectedTabIndex
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .background(containerColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            state.tabs.forEachIndexed { index, tab ->
                val active = index == activeTabIndex
                BubbleTab(
                    modifier = Modifier.thenIf(active) {
                        weight(1F, fill = false)
                    },
                    active = active,
                    tabConfig = tab,
                    onClick = {
                        onValueChange(index, tab)
                        state.selectTab(tab)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BubbleTab(
    modifier: Modifier = Modifier,
    active: Boolean,
    tabConfig: BubbleTabConfig,
    onClick: () -> Unit
) {
    val localContentColor = LocalContentColor.current

    val updateTransition = updateTransition(
        targetState = active,
        label = "active/inactive"
    )

    val containerColor by updateTransition.animateColor(
        label = "containerColor"
    ) {
        if(it) tabConfig.colors.activeContainerColor else Color.Transparent
    }
    CardDefaults.cardColors()
    val contentColor by updateTransition.animateColor(
        label = "contentColor"
    ) {
        if(it) tabConfig.colors.activeContentColor else localContentColor
    }

    CompositionLocalProvider(
        LocalRippleConfiguration provides disableRippleConfig
    ) {
        Box(
            modifier = modifier
                .background(
                    color = containerColor,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                updateTransition.AnimatedContent(
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if(it) {
                                tabConfig.selectedIcon
                            } else {
                                tabConfig.unselectedIcon
                            }
                        ),
                        contentDescription = tabConfig.contentDescription,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                updateTransition.AnimatedVisibility(
                    visible = { it },
                    enter = expandHorizontally(springIntSize),
                    exit = shrinkHorizontally(springIntSize)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalSpacer(width = 6.dp)
                        Text(
                            text = tabConfig.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }
    }
}

private val springIntSize = spring<IntSize>(stiffness = 300F, dampingRatio = .6F)