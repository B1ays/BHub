package ru.blays.hub.core.ui.screens.settingsContent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.blays.hub.core.logic.components.settingsComponents.ThemeSettingsComponent
import ru.blays.hub.core.preferences.proto.ThemeType
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.settingsElements.ColorPickerItem
import ru.blays.hub.core.ui.elements.settingsElements.SettingsCardWithSwitch
import ru.blays.hub.core.ui.elements.settingsElements.SettingsExpandableCard
import ru.blays.hub.core.ui.elements.settingsElements.SettingsRadioButtonWithTitle
import ru.blays.hub.core.ui.theme.defaultAccentColors
import ru.blays.hub.core.ui.values.CardShape
import ru.blays.hub.core.ui.values.DefaultPadding
import ru.blays.hub.core.ui.values.LocalDarkTheme

@Composable
fun ThemeSettingsContent(
    modifier: Modifier = Modifier,
    component: ThemeSettingsComponent
) {
    val theme by component.themeSettings.collectAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                ThemeSettingsComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(
                    titleText = stringResource(id = R.string.settings_group_theme)
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = modifier,
            contentPadding = padding
        ) {
            item {
                ThemeSetting(
                    value = theme.themeType,
                    onValueChange = { newValue ->
                        component.setValue {
                            themeType = newValue
                        }
                    }
                )
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    DynamicColorsSetting(
                        value = theme.monetColors,
                        onValueChange = { newValue ->
                            component.setValue {
                                monetColors = newValue
                            }
                        }
                    )
                }
            }
            item {
                AmoledThemeSetting(
                    value = theme.amoledTheme,
                    enabled = LocalDarkTheme.current,
                    onValueChange = { newValue ->
                        component.setValue {
                            amoledTheme = newValue
                        }
                    }
                )
            }
            item {
                AccentColorSetting(
                    value = theme.accentColorIndex,
                    enabled = !theme.monetColors,
                    onValueChange = { newValue ->
                        component.setValue {
                            accentColorIndex = newValue
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSetting(
    modifier: Modifier = Modifier,
    value: ThemeType,
    onValueChange: (ThemeType) -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val sunIcon = painterResource(R.drawable.ic_sun)
    val moonIcon = painterResource(R.drawable.ic_moon)
    val icon = when(value) {
        ThemeType.SYSTEM -> if(darkTheme) moonIcon else sunIcon
        ThemeType.DARK -> moonIcon
        ThemeType.LIGHT -> sunIcon
        ThemeType.UNRECOGNIZED -> painterResource(id = R.drawable.ic_cross)
    }
    SettingsExpandableCard(
        modifier = modifier,
        title = stringResource(R.string.setting_title_theme),
        subtitle = stringResource(R.string.setting_subtitle_theme),
        icon = icon,
        shape = CardShape.CardStart
    ) {
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.theme_system),
            selected = value == ThemeType.SYSTEM,
        ) {
            onValueChange(ThemeType.SYSTEM)
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.theme_dark),
            selected = value == ThemeType.DARK,
        ) {
            onValueChange(ThemeType.DARK)
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.theme_light),
            selected = value == ThemeType.LIGHT,
        ) {
            onValueChange(ThemeType.LIGHT)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun DynamicColorsSetting(
    modifier: Modifier = Modifier,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    val icon = painterResource(R.drawable.ic_color_swatches)
    SettingsCardWithSwitch(
        modifier = modifier,
        title = stringResource(R.string.setting_title_monet),
        subtitle = stringResource(R.string.setting_subtitle_monet),
        icon = icon,
        shape = CardShape.CardMid,
        value = value,
        action = onValueChange
    )
}

@Composable
private fun AmoledThemeSetting(
    modifier: Modifier = Modifier,
    value: Boolean,
    enabled: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    val icon = painterResource(R.drawable.ic_eclipse)
    SettingsCardWithSwitch(
        modifier = modifier,
        title = stringResource(R.string.setting_title_amoled),
        subtitle = stringResource(R.string.setting_subtitle_amoled),
        icon = icon,
        shape = CardShape.CardMid,
        value = value,
        enabled = enabled,
        action = onValueChange
    )
}


@Composable
private fun AccentColorSetting(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val icon = painterResource(R.drawable.ic_brush)
    val lazyListState = rememberLazyListState()
    SettingsExpandableCard(
        modifier = modifier,
        enabled = enabled,
        title = stringResource(R.string.setting_title_accent),
        subtitle = stringResource(R.string.setting_subtitle_accent),
        icon = icon,
        shape = CardShape.CardEnd,
    ) {
        LazyRow(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPaddingSmall)
                .fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(defaultAccentColors) { index, color ->
                ColorPickerItem(
                    color = color,
                    index = index,
                    selectedItemIndex = value,
                    actionSelectColor = onValueChange
                )
            }
        }
    }
}