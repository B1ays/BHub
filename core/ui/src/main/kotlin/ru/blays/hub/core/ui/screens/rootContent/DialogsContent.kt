package ru.blays.hub.core.ui.screens.rootContent

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import ru.blays.hub.core.logic.components.InfoDialogComponent
import ru.blays.hub.core.logic.components.SetupComponent
import ru.blays.hub.core.logic.components.rootComponents.DialogsComponent
import ru.blays.hub.core.logic.components.rootComponents.ShizukuDialogComponent
import ru.blays.hub.core.logic.utils.intent
import ru.blays.hub.core.logic.utils.packageUri
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.autoscaleText.AutoscaleText
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.utils.thenIf
import ru.blays.hub.core.ui.values.DefaultPadding
import ru.blays.hub.core.ui.values.LocalStackAnimator

@Composable
fun DialogsContent(
    modifier: Modifier = Modifier,
    component: DialogsComponent
) {
    val color1 = MaterialTheme.colorScheme.primary
    val color2 = MaterialTheme.colorScheme.tertiary.copy(0.4F)

    val orientation = LocalConfiguration.current.orientation

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    0F to color1,
                    0.95F to color2
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .thenIf(orientation == Configuration.ORIENTATION_PORTRAIT) {
                    fillMaxWidth(0.8F).aspectRatio(1 / 1.5F)
                }
                .thenIf(orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    fillMaxHeight(0.8F).aspectRatio(1.5F / 1F)
                }
        ) {
            Children(
                modifier = Modifier.fillMaxSize(),
                stack = component.childStack,
                animation = stackAnimation(LocalStackAnimator.current)
            ) {
                when(val child = it.instance) {
                    is DialogsComponent.Child.Setup -> LandingDialogContent(component = child.component)
                    is DialogsComponent.Child.RootDialog -> Unit // TODO implement
                    is DialogsComponent.Child.ShizukuDialog -> ShizukuDialogContent(component = child.component)
                }
            }
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
private fun LandingDialogContent(
    modifier: Modifier = Modifier,
    component: SetupComponent
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by component.state.collectAsState()

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.currentStateFlow.collect { state ->
            if(state == Lifecycle.State.RESUMED) {
                component.sendIntent(
                    SetupComponent.Intent.RecheckPermissions
                )
            }
        }
    }

    val notificationsSendLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { success ->
        component.sendIntent(
            SetupComponent.Intent.ChangeNotificationsSendStatus(success)
        )
    }

    val contentColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.8F)
    val enabledContainerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.2F)

    Column(
        modifier = modifier.padding(DefaultPadding.CardDefaultPaddingLarge),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_wrench),
                contentDescription = stringResource(id = R.string.content_description_icon_wrench),
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = stringResource(id = R.string.landing_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationsSendLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = if(state.notificationsSendGranted) {
                        enabledContainerColor
                    } else {
                        Color.Transparent
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1F)
                    ) {
                        Text(
                            text = stringResource(id = R.string.landing_permission_notifications),
                            style = MaterialTheme.typography.titleMedium
                        )
                        VerticalSpacer(height = 4.dp)
                        Text(
                            text = if (state.notificationsSendGranted) {
                                stringResource(id = R.string.landing_permission_granted)
                            } else {
                                stringResource(id = R.string.landing_permission_notGranted)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }

            Card(
                onClick = {
                    val settingsIntent = intent {
                        action = Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                        data = context.packageUri
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if(state.installAppsGranted) {
                        enabledContainerColor
                    } else {
                        Color.Transparent
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1F)
                    ) {
                        Text(
                            text = stringResource(id = R.string.landing_permission_installApps),
                            style = MaterialTheme.typography.titleMedium
                        )
                        VerticalSpacer(height = 4.dp)
                        Text(
                            text = if (state.installAppsGranted) {
                                stringResource(id = R.string.landing_permission_granted)
                            } else {
                                stringResource(id = R.string.landing_permission_notGranted)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }

            Card(
                onClick = {
                    intent {
                        action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        data = context.packageUri
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    .let(context::startActivity)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if(state.batteryNotOptimized) {
                        enabledContainerColor
                    } else {
                        Color.Transparent
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1F)
                    ) {
                        Text(
                            text = stringResource(id = R.string.landing_permission_batteryOptimization),
                            style = MaterialTheme.typography.titleMedium
                        )
                        VerticalSpacer(height = 4.dp)
                        Text(
                            text = if (state.batteryNotOptimized) {
                                stringResource(id = R.string.battery_notOptimized)
                            } else {
                                stringResource(id = R.string.battery_optimized)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }

            Card(
                onClick = {
                   component.sendIntent(
                       SetupComponent.Intent.EnableRootMode
                   )
                },
                colors = CardDefaults.cardColors(
                    containerColor = if(state.rootMode) {
                        enabledContainerColor
                    } else {
                        Color.Transparent
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1F)
                    ) {
                        Text(
                            text = stringResource(id = R.string.setting_title_rootMode),
                            style = MaterialTheme.typography.titleMedium
                        )
                        VerticalSpacer(height = 4.dp)
                        Text(
                            text = if (state.rootMode) {
                                stringResource(id = R.string.landing_setting_enabled)
                            } else {
                                stringResource(id = R.string.landing_setting_notEnabled)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }
        }
        VerticalSpacer(height = 4.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
        ) {
            val buttonEnabled by remember {
                derivedStateOf {
                    state.notificationsSendGranted &&
                    state.installAppsGranted &&
                    state.batteryNotOptimized
                }
            }

            Button(
                onClick = {
                    component.onOutput(
                        SetupComponent.Output.Close
                    )
                },
                shape = MaterialTheme.shapes.medium,
                enabled = buttonEnabled,
            ) {
                Text(text = stringResource(id = R.string.action_next))
                HorizontalSpacer(width = 6.dp)
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = stringResource(id = R.string.content_description_icon_forward),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/*@Composable
private fun RootDialogContent(
    modifier: Modifier = Modifier,
    component: InfoDialogComponent<ShizukuDialogComponent.Configuration, DialogsComponent.RootPermissionsDialogActions>
) {

}*/

@Composable
private fun ShizukuDialogContent(
    modifier: Modifier = Modifier,
    component: InfoDialogComponent<ShizukuDialogComponent.Configuration, ShizukuDialogComponent.Action>
) {
    val config = component.state

    Column(
        modifier = modifier.padding(DefaultPadding.CardDefaultPaddingLarge),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shizuku),
                contentDescription = stringResource(id = R.string.content_description_icon_shizuku),
                modifier = Modifier
                    .size(28.dp)
                    .scale(1.2F)
            )
            Text(
                text = config.title,
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Text(
            text = config.message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1F)
        )
        VerticalSpacer(height = 4.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            when(config) {
                is ShizukuDialogComponent.Configuration.ShizukuNotInstalled -> {
                    OutlinedButton(
                        onClick = {
                            component.sendIntent(
                                ShizukuDialogComponent.Action.Disable
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(0.5F, false)
                    ) {
                        AutoscaleText(
                            text = stringResource(id = R.string.action_disable),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = {
                            component.sendIntent(
                                ShizukuDialogComponent.Action.Download
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(0.5F, false)
                    ) {
                        AutoscaleText(
                            text = stringResource(id = R.string.dialog_shizuku_action_download),
                            maxLines = 1
                        )
                    }
                }
                is ShizukuDialogComponent.Configuration.ShizukuNotRunning -> {
                    OutlinedButton(
                        onClick = {
                            component.sendIntent(
                                ShizukuDialogComponent.Action.Disable
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(0.5F, false)
                    ) {
                        AutoscaleText(
                            text = stringResource(id = R.string.action_disable),
                            maxLines = 1
                        )
                    }
                    Button(
                        onClick = {
                            component.sendIntent(
                                ShizukuDialogComponent.Action.Open
                            )
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(0.5F, false)
                    ) {
                        AutoscaleText(
                            text = stringResource(id = R.string.dialog_shizuku_action_open),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}