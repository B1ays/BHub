package ru.blays.hub.core.ui.screens.downloadsContent

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.LayoutReference
import coil.compose.AsyncImage
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import ru.blays.hub.core.domain.components.downloadComponents.DownloadsListComponent
import ru.blays.hub.core.domain.components.downloadComponents.DownloadsMenuComponent
import ru.blays.hub.core.domain.data.FilesSortType
import ru.blays.hub.core.domain.data.models.ApkFile
import ru.blays.hub.core.domain.utils.copyToClipboard
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.buttons.CustomIconButton
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.collapsingToolbar.rememberToolbarScrollBehavior
import ru.blays.hub.core.ui.elements.contextMenu.ContextMenu
import ru.blays.hub.core.ui.elements.contextMenu.contextMenuAnchor
import ru.blays.hub.core.ui.elements.contextMenu.rememberContextMenuState
import ru.blays.hub.core.ui.elements.indicators.DotIndicator
import ru.blays.hub.core.ui.elements.progressIndicator.GradientCircularProgressIndicator
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.elements.text.TextWithPrefix
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.utils.shadowPlus
import ru.blays.hub.core.ui.utils.thenIf
import ru.blays.hub.core.ui.values.DefaultPadding

@Composable
fun DownloadsContent(
    modifier: Modifier = Modifier,
    component: DownloadsListComponent
) {
    val state by component.state.collectAsState()
    val lazyListState = rememberLazyListState()
    val toolbarScrollBehavior = rememberToolbarScrollBehavior()
    val shimmer = rememberShimmer(ShimmerBounds.Window)

    Scaffold(
        topBar = {
            CollapsingToolbar(
                collapsingTitle = CollapsingTitle.large(
                    titleText = stringResource(id = R.string.appBar_title_downloads)
                ),
                scrollBehavior = toolbarScrollBehavior,
                collapsedElevation = 0.dp
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                FloatingMenu(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    component = component.menuComponent,
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(toolbarScrollBehavior.nestedScrollConnection),
            state = lazyListState,
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = state.tasks,
                key = DownloadsListComponent.Task::name
            ) { task ->
                TaskItem(
                    modifier = Modifier.animateItem(),
                    task = task,
                    onCancel = {
                        component.sendIntent(
                            DownloadsListComponent.Intent.CancelTask(task)
                        )
                    },
                )
            }
            if (state.tasks.isNotEmpty()) {
                item { VerticalSpacer(height = 16.dp) }
            }
            if (state.loading) {
                items(4) {
                    ApkPlaceholderItem(shimmer = shimmer)
                }
            } else {
                items(
                    items = state.downloadedFiles,
                    key = ApkFile::name
                ) { file ->
                    ApkItem(
                        modifier = Modifier.animateItem(),
                        apk = file,
                        onInstall = {
                            component.sendIntent(
                                DownloadsListComponent.Intent.InstallApk(file)
                            )
                        },
                        onModuleInstall = {
                            // TODO
                        },
                        onOpen = {
                            component.sendIntent(
                                DownloadsListComponent.Intent.OpenFile(file)
                            )
                        },
                        onDelete = {
                            component.sendIntent(
                                DownloadsListComponent.Intent.DeleteFile(file)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    modifier: Modifier = Modifier,
    task: DownloadsListComponent.Task,
    onCancel: () -> Unit
) {
    val progress by task.progressFlow.collectAsState(0F)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(DefaultPadding.CardDefaultPaddingLarge)
    ) {
        GradientCircularProgressIndicator(
            progress = { progress },
            strokeWidth = 6.dp,
            gradientStart = MaterialTheme.colorScheme.primary,
            gradientEnd = MaterialTheme.colorScheme.tertiary,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.size(50.dp)
        )
        HorizontalSpacer(width = 10.dp)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1F)
        ) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${(progress * 100).toInt()}/100%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalSpacer(width = 10.dp)
        CustomIconButton(
            onClick = onCancel,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.2F),
            contentColor = MaterialTheme.colorScheme.primary,
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cross),
                contentDescription = stringResource(id = R.string.content_description_icon_cancel),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApkItem(
    modifier: Modifier = Modifier,
    apk: ApkFile,
    onInstall: () -> Unit,
    onModuleInstall: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    var infoExpanded by remember { mutableStateOf(false) }
    val contextMenuState = rememberContextMenuState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { infoExpanded = !infoExpanded },
                onLongClick = contextMenuState::show
            )
            .contextMenuAnchor(contextMenuState),
    ) {
        Row(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingLarge),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = apk.apkInfo?.icon
            if (icon != null) {
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
            } else {
                Box(
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_file),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            HorizontalSpacer(width = 10.dp)
            Column {
                Text(
                    text = apk.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.size_formatted, apk.sizeString),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DotIndicator(
                        size = 4.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Text(
                        text = stringResource(id = R.string.date_formatted, apk.dateString),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(
                    visible = infoExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        apk.apkInfo?.let { packageInfo ->
                            TextWithPrefix(
                                prefix = stringResource(id = R.string.packageName),
                                text = packageInfo.packageName,
                                onTextClick = context::copyToClipboard
                            )
                            TextWithPrefix(
                                prefix = stringResource(id = R.string.versionName),
                                text = packageInfo.versionName,
                                onTextClick = context::copyToClipboard
                            )
                            TextWithPrefix(
                                prefix = stringResource(id = R.string.versionCode),
                                text = "${packageInfo.versionCode}",
                                onTextClick = context::copyToClipboard
                            )
                            packageInfo.signatureHash?.let { signatureHash ->
                                TextWithPrefix(
                                    prefix = stringResource(id = R.string.signatureSHA),
                                    text = signatureHash,
                                    onTextClick = context::copyToClipboard
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    ContextMenu(
        state = contextMenuState,
        shadowRadius = 8.dp,
        ambientColor = MaterialTheme.colorScheme.primary,
    ) {
        if(apk.apkInfo?.signatureHash != null) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.action_install)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_process),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = {
                    onInstall()
                    contextMenuState.hide()
                },
            )
        }
        /*DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_installAsModule)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_process),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = {
                onModuleInstall()
                contextMenuState.hide()
            },
        )*/
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_open_in)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_apps_select),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = {
                onOpen()
                contextMenuState.hide()
            },
        )
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.action_delete)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            onClick = {
                onDelete()
                contextMenuState.hide()
            },
        )
    }
}

@Composable
private fun ApkPlaceholderItem(
    modifier: Modifier = Modifier,
    shimmer: Shimmer
) {
    val backgroundColor = MaterialTheme.colorScheme.onSurface.copy(0.4F)
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPaddingLarge)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .shimmer(shimmer)
                .background(backgroundColor)
        )
        HorizontalSpacer(width = 10.dp)
        Column {
            Text(
                text = "Placeholder",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Transparent,
                modifier = Modifier
                    .shimmer(shimmer)
                    .background(backgroundColor, shape)
            )
            VerticalSpacer(height = 2.dp)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "File size",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Transparent,
                    modifier = Modifier
                        .shimmer(shimmer)
                        .background(backgroundColor, shape)
                )
                DotIndicator(
                    size = 4.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                Text(
                    text = "File Date",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Transparent,
                    modifier = Modifier
                        .shimmer(shimmer)
                        .background(backgroundColor, shape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingMenu(
    modifier: Modifier = Modifier,
    component: DownloadsMenuComponent,
) {
    val state by component.state.collectAsState()

    var clearConfirmDialogOpened by remember {
        mutableStateOf(false)
    }
    var sortSettingMenuExpanded by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = modifier
            .shadowPlus(
                radius = 6.dp,
                color = MaterialTheme.colorScheme.primaryColorAtAlpha(0.8F),
                shape = MaterialTheme.shapes.large,
                offset = DpOffset(
                    x = 3.dp,
                    y = 3.dp
                )
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
    ) {
        ConstraintLayout {
            val (
                refreshItem,
                clearItem,
                sortItem,
                divider1,
                divider2,
            ) = createRefs()

            val startGuideline = createGuidelineFromStart(10.dp)
            val endGuideline = createGuidelineFromEnd(10.dp)
            val topGuideline = createGuidelineFromTop(10.dp)
            val bottomGuideline = createGuidelineFromBottom(10.dp)

            val rowChain = createHorizontalChain(
                refreshItem.endMargin(3.dp),
                divider1.endMargin(3.dp),
                clearItem.endMargin(3.dp),
                divider2.endMargin(3.dp),
                sortItem,
                chainStyle = ChainStyle.Packed
            )

            constrain(rowChain) {
                start.linkTo(startGuideline)
                end.linkTo(endGuideline)
            }

            MenuItem(
                label = stringResource(id = R.string.action_refresh),
                iconId = R.drawable.ic_refresh,
                modifier = Modifier.constrainAs(refreshItem) {
                    top.linkTo(topGuideline)
                    bottom.linkTo(bottomGuideline)
                }
            ) {
                component.sendIntent(
                    DownloadsMenuComponent.Intent.Refresh
                )
            }
            VerticalDivider(
                modifier = Modifier.constrainAs(divider1) {
                    start.linkTo(refreshItem.end)
                    top.linkTo(refreshItem.top, 2.dp)
                    bottom.linkTo(refreshItem.bottom, 2.dp)
                    height = Dimension.fillToConstraints
                }
            )
            MenuItem(
                label = stringResource(id = R.string.action_clearAll),
                iconId = R.drawable.ic_clear_all,
                modifier = Modifier.constrainAs(clearItem) {
                    start.linkTo(divider1.end)
                    top.linkTo(refreshItem.top)
                    bottom.linkTo(refreshItem.bottom)
                }
            ) {
                clearConfirmDialogOpened = true
            }
            VerticalDivider(
                modifier = Modifier.constrainAs(divider2) {
                    start.linkTo(clearItem.end)
                    top.linkTo(refreshItem.top, 2.dp)
                    bottom.linkTo(refreshItem.bottom, 2.dp)
                    height = Dimension.fillToConstraints
                }
            )
            Box(
                modifier = Modifier.constrainAs(sortItem) {
                    start.linkTo(divider2.end)
                    top.linkTo(refreshItem.top)
                    bottom.linkTo(refreshItem.bottom)
                }
            ) {
                MenuItem(
                    label = stringResource(id = R.string.action_sort),
                    iconId = R.drawable.ic_sort,
                ) {
                    sortSettingMenuExpanded = true
                }
                DropdownMenu(
                    expanded = sortSettingMenuExpanded,
                    onDismissRequest = { sortSettingMenuExpanded = false },
                ) {
                    FilesSortType.entries.forEach { type ->
                        sortMethodName(type = type)?.let { name ->
                            DropdownMenuItem(
                                enabled = type != state.filesSortType,
                                text = { Text(text = name) },
                                onClick = {
                                    component.sendIntent(
                                        DownloadsMenuComponent.Intent.ChangeSortSetting(type)
                                    )
                                },
                                modifier = Modifier.thenIf(type == state.filesSortType) {
                                    background(MaterialTheme.colorScheme.primaryColorAtAlpha(0.25F))
                                }
                            )
                        }
                    }
                    DropdownMenuItem(
                        text = {
                            Text(text = stringResource(id = R.string.sort_order_reversed))
                        },
                        trailingIcon = {
                            Checkbox(
                                checked = state.reverseOrder,
                                onCheckedChange = null
                            )
                        },
                        onClick = {
                            component.sendIntent(
                                DownloadsMenuComponent.Intent.ChangeSortOrder(!state.reverseOrder)
                            )
                        },
                    )
                }
            }
        }
    }

    if (clearConfirmDialogOpened) {
        BasicAlertDialog(
            modifier = Modifier
                .shadowPlus(
                    radius = 6.dp,
                    color = MaterialTheme.colorScheme.primaryColorAtAlpha(0.8F),
                    shape = AlertDialogDefaults.shape,
                    offset = DpOffset(
                        x = 3.dp,
                        y = 3.dp
                    )
                ),
            onDismissRequest = { clearConfirmDialogOpened = false },
        ) {
            Surface(
                shape = AlertDialogDefaults.shape,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clear_all),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                        HorizontalSpacer(width = 10.dp)
                        Text(
                            text = stringResource(id = R.string.dialog_clearAll_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                    ) {
                        TextButton(
                            onClick = { clearConfirmDialogOpened = false }
                        ) {
                            Text(text = stringResource(id = R.string.action_cancel))
                        }
                        TextButton(
                            onClick = {
                                component.sendIntent(
                                    DownloadsMenuComponent.Intent.ClearAll
                                )
                                clearConfirmDialogOpened = false
                            }
                        ) {
                            Text(text = stringResource(id = R.string.action_ok))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    modifier: Modifier = Modifier,
    label: String,
    @DrawableRes iconId: Int,
    shape: Shape = MaterialTheme.shapes.small,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

@NonRestartableComposable
@Composable
fun sortMethodName(type: FilesSortType): String? {
    return when (type) {
        FilesSortType.NAME -> stringResource(id = R.string.sort_order_by_name)
        FilesSortType.SIZE -> stringResource(id = R.string.sort_order_by_size)
        FilesSortType.MODIFY -> stringResource(id = R.string.sort_order_by_date)
    }
}

context(constrainLayoutScope: ConstraintLayoutScope)
private fun LayoutReference.endMargin(endMargin: Dp) =
    with(constrainLayoutScope) { withHorizontalChainParams(endMargin = endMargin) }