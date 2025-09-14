package ru.blays.hub.core.ui.screens.settingsContent

import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.domain.components.settingsComponents.MainSettingsComponent
import ru.blays.hub.core.preferences.proto.DownloadMode
import ru.blays.hub.core.preferences.proto.DownloadModeSetting
import ru.blays.hub.core.preferences.proto.PMType
import ru.blays.hub.core.preferences.proto.UpdateChannel
import ru.blays.hub.core.preferences.proto.copy
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.settingsElements.SettingsCardWithSwitch
import ru.blays.hub.core.ui.elements.settingsElements.SettingsExpandableCard
import ru.blays.hub.core.ui.elements.settingsElements.SettingsRadioButtonWithTitle
import ru.blays.hub.core.ui.elements.settingsElements.SettingsSwitchWithTitle
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.values.CardShape
import ru.blays.hub.core.ui.values.DefaultPadding

@Composable
fun MainSettingsContent(
    modifier: Modifier = Modifier,
    component: MainSettingsComponent
) {
    val rootMode by component.rootModeFlow.collectAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                MainSettingsComponent.Output.NavigateBack
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
                    titleText = stringResource(id = R.string.settings_group_main)
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = modifier,
            contentPadding = padding
        ) {
            item {
                PackageManagerSetting(
                    value = component.pmTypeFlow.collectAsState().value,
                    rootMode = rootMode,
                    shizukuEnabled = true,
                    onValueChange = { newValue ->
                        component.setValue {
                            pmType = newValue
                        }
                    }
                )
            }
            item {
                RootModeSetting(
                    value = rootMode,
                    enabled = component.rootAvailable,
                    onValueChange = { newValue ->
                        component.setValue {
                            this.rootMode = newValue
                        }
                    }
                )
            }
            item {
                DownloadModeSetting(
                    value = component.downloadModeFlow.collectAsState().value,
                    onValueChange = { newValue ->
                        component.setValue {
                            downloadModeSetting = newValue
                        }
                    }
                )
            }
            item {
                CheckAppsUpdates(
                    checkUpdates = component.checkAppsUpdatesFlow.collectAsState().value,
                    checkInterval = component.checkAppsUpdatesIntervalFlow.collectAsState().value,
                    onCheckUpdatesChange = { newValue ->
                        component.changeCheckUpdatesSetting(
                            checkUpdates = newValue
                        )
                    },
                    onIntervalChange = { newValue ->
                        component.changeCheckUpdatesSetting(
                            interval = newValue
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PackageManagerSetting(
    modifier: Modifier = Modifier,
    value: PMType,
    rootMode: Boolean,
    shizukuEnabled: Boolean,
    onValueChange: (PMType) -> Unit
) {
    SettingsExpandableCard(
        modifier = modifier,
        title = stringResource(R.string.setting_title_packageManager),
        subtitle = stringResource(R.string.setting_subtitle_packageManager),
        icon = painterResource(R.drawable.ic_boxes),
        shape = CardShape.CardStart
    ) {
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.packageManager_nonRoot),
            selected = value == PMType.NON_ROOT,
        ) {
            onValueChange(PMType.NON_ROOT)
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.packageManager_root),
            selected = value == PMType.ROOT,
            enabled = rootMode
        ) {
            onValueChange(PMType.ROOT)
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.packageManager_shizuku),
            selected = value == PMType.SHIZUKU,
            enabled = shizukuEnabled
        ) {
            onValueChange(PMType.SHIZUKU)
        }
    }
}

@Composable
private fun RootModeSetting(
    modifier: Modifier = Modifier,
    value: Boolean,
    enabled: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    SettingsCardWithSwitch(
        modifier = modifier,
        title = stringResource(R.string.setting_title_rootMode),
        subtitle = stringResource(R.string.setting_subtitle_rootMode),
        value = value,
        enabled = enabled,
        icon = painterResource(R.drawable.ic_root),
        shape = CardShape.CardMid,
        action = onValueChange
    )
}

@Composable
private fun CheckUpdatesSetting(
    modifier: Modifier = Modifier,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    SettingsCardWithSwitch(
        modifier = modifier,
        title = stringResource(R.string.setting_title_checkUpdates),
        subtitle = stringResource(R.string.setting_subtitle_checkUpdate),
        value = value,
        icon = painterResource(R.drawable.ic_time_forward),
        shape = CardShape.CardMid,
        action = onValueChange
    )
}

@Composable
private fun UpdatesChannelSetting(
    modifier: Modifier = Modifier,
    value: UpdateChannel,
    onValueChange: (UpdateChannel) -> Unit
) {
    SettingsExpandableCard(
        modifier = modifier,
        title = stringResource(R.string.setting_title_checkUpdatesChannel),
        subtitle = stringResource(R.string.setting_subtitle_checkUpdateChannel),
        icon = painterResource(R.drawable.ic_merrge),
        shape = CardShape.CardEnd
    ) {
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.updatesChannel_stable),
            selected = value == UpdateChannel.STABLE,
        ) {
            onValueChange(UpdateChannel.STABLE)
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.updatesChannel_beta),
            selected = value == UpdateChannel.BETA
        ) {
            onValueChange(UpdateChannel.BETA)
        }
        /*SettingsRadioButtonWithTitle(
            title = stringResource(R.string.updatesChannel_nightly),
            selected = value == UpdateChannel.NIGHTLY
        ) {
            onValueChange(UpdateChannel.NIGHTLY)
        }*/
    }
}

@Composable
fun DownloadModeSetting(
    modifier: Modifier = Modifier,
    value: DownloadModeSetting,
    onValueChange: (DownloadModeSetting) -> Unit
) {
    SettingsExpandableCard(
        modifier = modifier,
        title = stringResource(id = R.string.setting_title_downloadMode),
        subtitle = stringResource(R.string.setting_subtitle_downloadMode),
        icon = painterResource(id = R.drawable.ic_download_outlined),
        shape = CardShape.CardMid
    ) {
        var triesNumber by remember {
            mutableFloatStateOf(value.triesNumber.toFloat())
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.downloadMode_singleTry),
            selected = value.mode == DownloadMode.SINGLE_TRY,
        ) {
            onValueChange(
                value.copy {
                    mode = DownloadMode.SINGLE_TRY
                }
            )
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.downloadMode_multipleTries),
            selected = value.mode == DownloadMode.MULTIPLE_TRIES,
        ) {
            onValueChange(
                value.copy {
                    mode = DownloadMode.MULTIPLE_TRIES
                }
            )
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(R.string.downloadMode_infinityTries),
            selected = value.mode == DownloadMode.INFINITY_TRIES,
        ) {
            onValueChange(
                value.copy {
                    mode = DownloadMode.INFINITY_TRIES
                }
            )
        }
        androidx.compose.animation.AnimatedVisibility(
            visible = value.mode == DownloadMode.MULTIPLE_TRIES,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                VerticalSpacer(height = 6.dp)
                Row(
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.downloadMode_triesCount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1F)
                    )
                    Text(
                        text = "${triesNumber.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Light
                    )
                }
                VerticalSpacer(height = 4.dp)
                Slider(
                    value = triesNumber,
                    onValueChange = { triesNumber = it },
                    onValueChangeFinished = {
                        if (value.triesNumber.toFloat() != triesNumber) {
                            onValueChange(
                                value.copy {
                                    this.triesNumber = triesNumber.toInt()
                                }
                            )
                        }
                    },
                    steps = 6,
                    valueRange = 3F..10F,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CheckAppsUpdates(
    modifier: Modifier = Modifier,
    checkUpdates: Boolean,
    checkInterval: Int,
    onCheckUpdatesChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit
) {
    var checkIntervalFloat by remember {
        mutableFloatStateOf(checkInterval.toFloat())
    }

    SettingsExpandableCard(
        modifier = modifier,
        title = stringResource(id = R.string.setting_title_checkAppsUpdates),
        subtitle = stringResource(R.string.setting_subtitle_checkAppsUpdates),
        icon = painterResource(R.drawable.ic_device_load),
        shape = CardShape.CardMid
    ) {
        SettingsSwitchWithTitle(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
            title = stringResource(R.string.enabled),
            state = checkUpdates,
            action = onCheckUpdatesChange
        )
        VerticalSpacer(height = 6.dp)
        Row(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.interval),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1F)
            )
            Text(
                text = "${checkIntervalFloat.toInt()}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Light
            )
        }
        VerticalSpacer(height = 4.dp)
        Slider(
            value = checkIntervalFloat,
            onValueChange = { checkIntervalFloat = it },
            onValueChangeFinished = {
                if (checkInterval != checkIntervalFloat.toInt()) {
                    onIntervalChange(
                        checkIntervalFloat.toInt()
                    )
                }
            },
            steps = 6,
            valueRange = 6F..48F,
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun CustomTabsSetting(
    modifier: Modifier = Modifier,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    SettingsCardWithSwitch(
        modifier = modifier,
        title = stringResource(R.string.setting_title_custom_tabs),
        subtitle = stringResource(R.string.setting_subtitle_custom_tabs),
        value = value,
        icon = painterResource(R.drawable.ic_chrome),
        shape = CardShape.CardEnd,
        action = onValueChange
    )
}