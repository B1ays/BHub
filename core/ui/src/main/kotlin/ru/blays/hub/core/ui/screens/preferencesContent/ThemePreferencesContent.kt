package ru.blays.hub.core.ui.screens.preferencesContent

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
import ru.blays.hub.core.domain.components.preferencesComponents.ThemePreferencesComponent
import ru.blays.hub.core.domain.data.ThemePreferenceModel.AccentType
import ru.blays.hub.core.domain.data.ThemePreferenceModel.ThemeType
import ru.blays.hub.core.domain.data.isMonet
import ru.blays.hub.core.domain.data.isPreset
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.settingsElements.ColorPickerItem
import ru.blays.hub.core.ui.elements.settingsElements.SettingsCardWithSwitch
import ru.blays.hub.core.ui.elements.settingsElements.SettingsExpandableCard
import ru.blays.hub.core.ui.elements.settingsElements.SettingsRadioButtonWithTitle
import ru.blays.hub.core.ui.theme.DefaultAccentColors
import ru.blays.hub.core.ui.values.CardShape
import ru.blays.hub.core.ui.values.DefaultPadding
import ru.blays.hub.core.ui.values.LocalDarkTheme

@Composable
fun ThemeSettingsContent(
    modifier: Modifier = Modifier,
    component: ThemePreferencesComponent
) {
    val themeModel by component.themePreferenceFlow.collectAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                ThemePreferencesComponent.Output.NavigateBack
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
                    value = themeModel.themeType,
                    onValueChange = { newValue ->
                        component.sendIntent(
                            ThemePreferencesComponent.Intent.ChangeThemeType(newValue)
                        )
                    }
                )
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    DynamicColorsSetting(
                        value = themeModel.colorAccentType,
                        onValueChange = { newValue ->
                            component.sendIntent(
                                ThemePreferencesComponent.Intent.ChangeAccentType(newValue)
                            )
                        }
                    )
                }
            }
            item {
                AmoledThemeSetting(
                    value = themeModel.amoledTheme,
                    enabled = LocalDarkTheme.current,
                    onValueChange = { newValue ->
                        component.sendIntent(
                            ThemePreferencesComponent.Intent.ChangeAmoledTheme(newValue)
                        )
                    }
                )
            }
            item {
                AccentColorSetting(
                    type = themeModel.colorAccentType,
                    onValueChange = { newValue ->
                        component.sendIntent(
                            ThemePreferencesComponent.Intent.ChangeAccentType(newValue)
                        )
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
    value: AccentType,
    onValueChange: (AccentType) -> Unit
) {
    val icon = painterResource(R.drawable.ic_color_swatches)
    SettingsCardWithSwitch(
        modifier = modifier,
        title = stringResource(R.string.setting_title_monet),
        subtitle = stringResource(R.string.setting_subtitle_monet),
        icon = icon,
        shape = CardShape.CardMid,
        value = value.isMonet(),
        action = { enabled ->
            if(enabled && value.isPreset()) {
                onValueChange(AccentType.Dynamic(value.index))
            } else if(!enabled && value.isMonet()) {
                onValueChange(AccentType.Preset(value.fallbackIndex))
            }
        }
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
    type: AccentType,
    onValueChange: (AccentType) -> Unit
) {
    val icon = painterResource(R.drawable.ic_brush)
    val lazyListState = rememberLazyListState()
    SettingsExpandableCard(
        modifier = modifier,
        enabled = !type.isMonet(),
        title = stringResource(R.string.setting_title_accent),
        subtitle = stringResource(R.string.setting_subtitle_accent),
        icon = icon,
        shape = CardShape.CardEnd,
    ) {
        if(type.isPreset()) {
            LazyRow(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingSmall)
                    .fillMaxWidth(),
                state = lazyListState
            ) {
                itemsIndexed(DefaultAccentColors) { index, color ->
                    ColorPickerItem(
                        color = color,
                        index = index,
                        selectedItemIndex = type.index,
                        actionSelectColor = { index ->
                            onValueChange(AccentType.Preset(index))
                        }
                    )
                }
            }
        }
    }
}