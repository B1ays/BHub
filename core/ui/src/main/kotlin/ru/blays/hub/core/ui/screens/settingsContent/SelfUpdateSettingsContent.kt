package ru.blays.hub.core.ui.screens.settingsContent

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.lighten
import ru.blays.hub.core.domain.components.SelfUpdateComponent
import ru.blays.hub.core.domain.components.settingsComponents.SelfUpdateSettingsComponent
import ru.blays.hub.core.domain.data.models.AppUpdateInfoModel
import ru.blays.hub.core.preferences.proto.UpdateChannel
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.placeholder.FullscreenPlaceholder
import ru.blays.hub.core.ui.elements.settingsElements.SettingsRadioButtonWithTitle
import ru.blays.hub.core.ui.elements.settingsElements.SettingsSwitchWithTitle
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.screens.rootContent.SelfUpdateContent
import ru.blays.hub.core.ui.values.DefaultPadding

@Composable
fun SelfUpdateSettingsContent(
    modifier: Modifier = Modifier,
    component: SelfUpdateSettingsComponent,
) {

    val selfUpdateState by component.selfUpdateComponent.state.collectAsState()
    val updatesChannel by component.updatesChannelFlow.collectAsState()
    val checkUpdates by component.checkUpdatesFlow.collectAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                SelfUpdateSettingsComponent.Output.NavigateBack
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
                    titleText = stringResource(id = R.string.appBar_title_selfUpdate)
                )
            )
        },
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxSize(),
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.primary.lighten(1.4F)
                ),
                shape = MaterialTheme.shapes.large.copy(
                    bottomStart = ZeroCornerSize,
                    bottomEnd = ZeroCornerSize
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = selfUpdateState != SelfUpdateComponent.State.Loading,
                        ) {
                            component.selfUpdateComponent.sendIntent(
                                SelfUpdateComponent.Intent.Refresh
                            )
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Crossfade(
                        targetState = selfUpdateState == SelfUpdateComponent.State.Loading,
                        label = "icon crossfade"
                    ) { loading ->
                        if(loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_time_forward),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    HorizontalSpacer(16.dp)
                    Text(
                        text = stringResource(R.string.action_checkUpdates),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            VerticalSpacer(6.dp)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large.copy(
                    topStart = ZeroCornerSize,
                    topEnd = ZeroCornerSize
                )
            ) {
                Column(
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
                ) {
                    VerticalSpacer(4.dp)
                    SettingsSwitchWithTitle(
                        title = stringResource(R.string.setting_title_checkUpdates),
                        state = checkUpdates
                    ) {
                        component.sendIntent(
                            SelfUpdateSettingsComponent.Intent.ChangeCheckUpdates(it)
                        )
                    }
                    VerticalSpacer(4.dp)
                    SettingsRadioButtonWithTitle(
                        title = stringResource(R.string.updatesChannel_stable),
                        selected = updatesChannel == UpdateChannel.STABLE,
                    ) {
                        component.sendIntent(
                            SelfUpdateSettingsComponent.Intent.ChangeUpdatesChannel(
                                UpdateChannel.STABLE
                            )
                        )
                    }
                    VerticalSpacer(4.dp)
                    SettingsRadioButtonWithTitle(
                        title = stringResource(R.string.updatesChannel_beta),
                        selected = updatesChannel == UpdateChannel.BETA
                    ) {
                        component.sendIntent(
                            SelfUpdateSettingsComponent.Intent.ChangeUpdatesChannel(
                                UpdateChannel.BETA
                            )
                        )
                    }
                }
            }
            VerticalSpacer(10.dp)
            UpdatesInfoContainer(
                state = selfUpdateState,
                onUpdate = {
                    component.selfUpdateComponent.sendIntent(
                        SelfUpdateComponent.Intent.UpdateApp(it)
                    )
                }
            )
        }
    }
}

@Composable
private fun UpdatesInfoContainer(
    modifier: Modifier = Modifier,
    state: SelfUpdateComponent.State,
    onUpdate: (AppUpdateInfoModel) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when(state) {
            is SelfUpdateComponent.State.Available -> Column {
                SelfUpdateContent(
                    updateInfo = state.updateInfo,
                    onUpdate = onUpdate
                )
            }
            SelfUpdateComponent.State.Downloading -> {
                Card(
                    modifier = Modifier.align(Alignment.Center),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.downloading),
                            style = MaterialTheme.typography.titleMedium
                        )
                        VerticalSpacer(8.dp)
                        LinearProgressIndicator()
                    }
                }
            }
            is SelfUpdateComponent.State.Error -> FullscreenPlaceholder(
                iconId = R.drawable.ic_error,
                contentDescriptionId = R.string.content_description_icon_error,
                message = state.message
            )
            SelfUpdateComponent.State.NotAvailable -> FullscreenPlaceholder(
                iconId = R.drawable.ic_time_crossed,
                contentDescriptionId = R.string.content_description_icon_time_crossed,
                messageId = R.string.updatesNotFound
            )
            else -> Unit
        }
    }
}