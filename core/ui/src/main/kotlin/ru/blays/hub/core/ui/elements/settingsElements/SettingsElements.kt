package ru.blays.hub.core.ui.elements.settingsElements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.shapes.squircleShape.CornerSmoothing
import ru.blays.hub.core.ui.elements.shapes.squircleShape.SquircleShape
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.utils.surfaceColorAtAlpha
import ru.blays.hub.core.ui.utils.thenIf
import ru.blays.hub.core.ui.values.CardShape
import ru.blays.hub.core.ui.values.DefaultPadding
import ru.blays.hub.core.ui.values.ZeroCardElevation

@Composable
fun SettingsClickableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    shape: Shape = CardDefaults.shape,
) {
    Card(
        modifier = modifier
            .padding(DefaultPadding.CardPaddingSmallVertical)
            .fillMaxWidth(),
        shape = shape,
        enabled = enabled,
        onClick = onClick,
        elevation = CardDefaults.ZeroCardElevation,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(icon != null) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(50.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = SquircleShape(
                                cornerSmoothing = CornerSmoothing.High
                            ),
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(.6F),
                        modifier = Modifier.fillMaxSize(0.5F)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Column(
                    modifier = Modifier.fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (subtitle != null) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Suppress("TransitionPropertiesLabel", "AnimatedContentLabel")
@Composable
fun SettingsExpandableCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    shape: Shape = CardDefaults.shape,
    content: @Composable ColumnScope.() -> Unit
) {
    var isCardExpanded by rememberSaveable { mutableStateOf(false) }

    val transition = updateTransition(
        targetState = isCardExpanded,
        label = null
    )
    val rotateValue by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = ANIMATION_DURATION_MILLIS)
        }
    ) { expanded ->
        if (expanded) 180F else 0F
    }

    Card(
        modifier = modifier
            .padding(DefaultPadding.CardPaddingSmallVertical)
            .fillMaxWidth(),
        shape = shape,
        enabled = enabled,
        onClick = { isCardExpanded = !isCardExpanded },
        elevation = CardDefaults.ZeroCardElevation,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = SquircleShape(
                            cornerSmoothing = CornerSmoothing.High
                        ),
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = icon,
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith  fadeOut() + scaleOut()
                    }
                ) { painter ->
                    if(painter != null) {
                        Icon(
                            painter = painter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(.6F),
                            modifier = Modifier.fillMaxSize(0.5F)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (subtitle != null) Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotateValue),
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = null
                )
            }
        }
        transition.AnimatedVisibility(
            visible = { it },
            enter = expandVertically(
                animationSpec = spring(stiffness = 300F, dampingRatio = .6F)
            ),
            exit = shrinkVertically(
                animationSpec = spring(stiffness = 300F, dampingRatio = .6F)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
fun SettingsExpandableCard(
    enabled: Boolean = true,
    title: String,
    subtitle: String = "",
    icon: ImageVector? = null,
    shape: Shape = CardDefaults.shape,
    content: @Composable ColumnScope.() -> Unit
) = SettingsExpandableCard(
    enabled = enabled,
    title = title,
    subtitle = subtitle,
    icon = icon?.let { rememberVectorPainter(it) },
    shape = shape,
    content = content
)

@Composable
fun SettingsCardWithSwitch(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: Painter? = null,
    shape: Shape = CardDefaults.shape,
    value: Boolean,
    enabled: Boolean = true,
    action: (Boolean) -> Unit
) {
    Card(
        modifier = modifier
            .padding(DefaultPadding.CardPaddingSmallVertical)
            .clip(shape),
        onClick = { action(!value) },
        shape = shape,
        enabled = enabled,
        elevation = CardDefaults.ZeroCardElevation,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = SquircleShape(
                            cornerSmoothing = CornerSmoothing.High
                        ),
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = icon,
                    transitionSpec = {
                        fadeIn() + scaleIn() togetherWith  fadeOut() + scaleOut()
                    },
                    label = "iconAnimation"
                ) { painter ->
                    if(painter != null) {
                        Icon(
                            painter = painter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(.6F),
                            modifier = Modifier.fillMaxSize(0.5F)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp, horizontal = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7F)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = value,
                    onCheckedChange = null,
                    enabled = enabled
                )
            }
        }
    }
}

@Composable
fun SettingsRadioButtonWithTitle(
    title: String,
    selected: Boolean,
    enabled: Boolean = true,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .clip(CardShape.CardStandalone)
            .clickable(
                enabled = enabled,
                onClick = onSelect
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                vertical = 4.dp
            ),
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled
            )
            HorizontalSpacer(8.dp)
            Text(
                text = title,
                color = if(enabled) LocalContentColor.current else LocalContentColor.current.copy(.6F),
            )
        }
    }
}

@Composable
fun SettingsCheckboxWithTitle(
    title: String,
    state: Boolean,
    enabled: Boolean = true,
    action: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(
                vertical = 2.dp,
                horizontal = 12.dp
            )
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        Text(text = title)
        Checkbox(
            checked = state,
            onCheckedChange = action,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsSwitchWithTitle(
    modifier: Modifier = Modifier,
    title: String,
    state: Boolean,
    enabled: Boolean = true,
    action: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape.CardStandalone)
            .toggleable(
                value = state,
                onValueChange = action,
                enabled = enabled
            ),
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1F),
                text = title
            )
            Spacer(modifier = Modifier.width(6.dp))
            Switch(
                checked = state,
                onCheckedChange = null,
                enabled = enabled
            )
        }
    }
}

@Composable
fun SettingsSliderWithTitle(
    title: String,
    enabled: Boolean,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(
                vertical = 2.dp,
                horizontal = 12.dp
            )
            .fillMaxWidth()
    ) {
        Text(text = title)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = value,
                valueRange = valueRange,
                onValueChange = onValueChange,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.2F)
                ),
                modifier = Modifier.weight(1F),
            )
            Text(
                text = String.format(
                    LocalConfiguration.current.locales[0],
                    "%.2f",
                    value
                )
            )
        }

    }
}

@Composable
fun ColorPickerItem(
    modifier: Modifier = Modifier,
    color: Color,
    index: Int,
    selectedItemIndex: Int?,
    actionSelectColor: (Int) -> Unit
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color = color)
            .clickable { actionSelectColor(index) }
            .thenIf(selectedItemIndex == index) {
                border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                )
            }
    )
}

private const val ANIMATION_DURATION_MILLIS = 300