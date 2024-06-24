package ru.blays.hub.core.ui.screens.settingsContent.developerMenuContent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.blays.hub.core.logic.components.settingsComponents.developerMenu.DeveloperMenuLogsComponent
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.buttons.CustomIconButton
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.values.DefaultPadding
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperMenuLogsContent(
    modifier: Modifier = Modifier,
    component: DeveloperMenuLogsComponent
) {
    val state by component.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                DeveloperMenuLogsComponent.Output.NavigateBack
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
                    titleText = stringResource(id = R.string.logs)
                ),
            )
        }
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(padding),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    component.sendIntent(
                        DeveloperMenuLogsComponent.Intent.Refresh
                    )
                    delay(1.seconds)
                    isRefreshing = false
                }
            },
        ) {
            Column(modifier = modifier) {
                Actions(
                    onCopyLog = {
                        component.sendIntent(
                            DeveloperMenuLogsComponent.Intent.CopyLogs
                        )
                    },
                    onShareLog = {
                        component.sendIntent(
                            DeveloperMenuLogsComponent.Intent.ShareLogs
                        )
                    },
                    onShareLogFile = {
                        component.sendIntent(
                            DeveloperMenuLogsComponent.Intent.ShareLogsFile
                        )
                    }
                )
                if(state.logs.isNotEmpty()) {
                    LogView(logs = state.logs)
                }
            }
        }
    }
}

@Suppress("NonSkippableComposable")
@Composable
fun LogView(
    modifier: Modifier = Modifier,
    logs: List<String>
) {
    SelectionContainer {
        Surface(
            modifier = Modifier.padding(12.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            val shape = RoundedCornerShape(5)
            LazyColumn(
                modifier = modifier.padding(12.dp)
            ) {
                itemsIndexed(logs) { index, log ->
                    Text(
                        text = log,
                        modifier = Modifier.background(
                            color = if(index % 2 == 0) {
                                Color.Unspecified
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            shape = shape
                        )
                    )
                    VerticalSpacer(height = 2.dp)
                }
            }
        }

    }
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    onCopyLog: () -> Unit,
    onShareLog: () -> Unit,
    onShareLogFile: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        CustomIconButton(
            onClick = onCopyLog,
            containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.25F),
            contentColor = MaterialTheme.colorScheme.primary,
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_copy), 
                contentDescription = stringResource(id = R.string.content_description_icon_copy),
                modifier = Modifier.size(22.dp)
            )
        }
        CustomIconButton(
            onClick = onShareLog,
            containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.25F),
            contentColor = MaterialTheme.colorScheme.primary,
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = stringResource(id = R.string.content_description_icon_share),
                modifier = Modifier.size(22.dp)
            )
        }
        CustomIconButton(
            onClick = onShareLogFile,
            containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.25F),
            contentColor = MaterialTheme.colorScheme.primary,
            contentPadding = PaddingValues(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_file_export),
                contentDescription = stringResource(id = R.string.content_description_icon_fileExport),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}