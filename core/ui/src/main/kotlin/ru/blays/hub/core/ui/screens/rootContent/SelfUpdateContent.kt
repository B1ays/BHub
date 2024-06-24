package ru.blays.hub.core.ui.screens.rootContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import ru.blays.hub.core.logic.components.SelfUpdateComponent
import ru.blays.hub.core.logic.components.SelfUpdateDialogComponent
import ru.blays.hub.core.logic.data.models.AppUpdateInfoModel
import ru.blays.hub.core.logic.utils.openInBrowser
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.autoscaleText.AutoscaleText
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.values.DefaultPadding

context(ColumnScope)
@Composable
fun SelfUpdateContent(component: SelfUpdateDialogComponent) {
    val updateInfo = component.updateInfo
    Header()
    VerticalSpacer(height = 4.dp)
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth(0.9F)
            .align(Alignment.CenterHorizontally))
    VerticalSpacer(height = 4.dp)
    Info(updateInfo = updateInfo)
    VerticalSpacer(height = 4.dp)
    ChangelogContent(
        modifier = Modifier.weight(1F, false),
        changelog = updateInfo.changelog
    )
    VerticalSpacer(height = 4.dp)
    Actions(
        onCancel = {
            component.onOutput(
                SelfUpdateComponent.Output.Close
            )
        },
        onUpdate = {
            component.sendIntent(
                SelfUpdateComponent.Intent.UpdateApp(updateInfo)
            )
        }
    )
    VerticalSpacer(height = 4.dp)
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
    ) {
        AutoscaleText(
            text = stringResource(id = R.string.update_available),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
    }
}

@Composable
private fun Info(
    modifier: Modifier = Modifier,
    updateInfo: AppUpdateInfoModel
) {
    Column(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(id = R.string.version_formatted, updateInfo.versionName),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(id = R.string.versionCode_formatted, updateInfo.versionCode),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(id = R.string.buildDate_formatted, updateInfo.buildDate),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ChangelogContent(
    modifier: Modifier = Modifier,
    changelog: String
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        MarkdownText(
            modifier = Modifier.padding(12.dp),
            markdown = changelog,
            linkColor = MaterialTheme.colorScheme.primary,
            onLinkClicked = context::openInBrowser,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LocalContentColor.current
            )
        )
    }
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onUpdate: () -> Unit
) {
    Row(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
    ) {
        OutlinedButton(
            onClick = onCancel,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(0.5F, false)
        ) {
            AutoscaleText(
                text = stringResource(id = R.string.action_cancel),
                maxLines = 1
            )
        }
        Button(
            onClick = onUpdate,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(0.5F, false)
        ) {
            AutoscaleText(
                text = stringResource(id = R.string.action_update),
                maxLines = 1
            )
        }
    }
}