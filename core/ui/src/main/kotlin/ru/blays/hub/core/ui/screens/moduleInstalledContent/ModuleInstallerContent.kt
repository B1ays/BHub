package ru.blays.hub.core.ui.screens.moduleInstalledContent

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.blays.hub.core.logic.components.moduleInstallerComponents.ModuleInstallerComponent
import ru.blays.hub.core.logic.data.models.ApkFile
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.placeholder.FullscreenPlaceholder
import ru.blays.hub.core.ui.elements.shapes.squircleShape.SquircleShape
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.values.DefaultPadding

@Composable
fun ModuleInstallerContent(
    modifier: Modifier = Modifier,
    component: ModuleInstallerComponent
) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                ModuleInstallerComponent.Output.NavigateBack
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
                    titleText = stringResource(R.string.appBar_title_moduleInstall)
                ),
            )
        },
    ) { padding ->
        Crossfade(
            targetState = state,
            modifier = modifier
                .padding(padding)
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxSize(),
            label = "state_crossfade"
        ) { currentState ->
            when (currentState) {
                is ModuleInstallerComponent.State.AllPicked -> {
                    // TODO
                }
                ModuleInstallerComponent.State.Empty -> Unit
                is ModuleInstallerComponent.State.Error -> FullscreenPlaceholder(
                    iconId = R.drawable.ic_error,
                    contentDescriptionId = R.string.content_description_icon_error,
                    message = currentState.message,
                )
                ModuleInstallerComponent.State.Installing -> {
                    // TODO
                }
                is ModuleInstallerComponent.State.NeedToPickOrig -> {
                    Column {
                        ModApkHeader(
                            modApk = currentState.modApk
                        )
                    }
                }
                is ModuleInstallerComponent.State.OrigInstalled -> {
                    Column {
                        ModApkHeader(
                            modApk = currentState.modApk
                        )
                    }
                }
                ModuleInstallerComponent.State.RootNotGranted -> FullscreenPlaceholder(
                    iconId = R.drawable.ic_root_crossed,
                    contentDescriptionId = R.string.content_description_icon_rootCrossed,
                    messageId = R.string.error_root_not_granted
                )
                ModuleInstallerComponent.State.Success -> {
                    // TODO
                }
            }
        }
    }
}


@Composable
private fun ModApkHeader(
    modifier: Modifier = Modifier,
    modApk: ApkFile
) {
    val appInfo = modApk.apkInfo
    if(appInfo != null) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = appInfo.icon,
                contentDescription = stringResource(R.string.content_description_application_icon),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .clip(SquircleShape())
            )
            VerticalSpacer(6.dp)
            Text(
                text = appInfo.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            VerticalSpacer(10.dp)
            Text(
                text = appInfo.packageName,
                style = MaterialTheme.typography.bodyMedium,
            )
            VerticalSpacer(2.dp)
            Text(
                text = appInfo.versionName,
                style = MaterialTheme.typography.bodyMedium,
            )
            VerticalSpacer(2.dp)
            Text(
                text = appInfo.versionCode.toString(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}