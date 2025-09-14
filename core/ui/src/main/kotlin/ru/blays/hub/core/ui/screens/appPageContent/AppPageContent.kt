@file:OptIn(ExperimentalSharedTransitionApi::class)

package ru.blays.hub.core.ui.screens.appPageContent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.jeziellago.compose.markdowntext.MarkdownText
import ru.blays.hub.core.domain.components.appPageComponents.ApkInstallAction
import ru.blays.hub.core.domain.components.appPageComponents.AppComponent
import ru.blays.hub.core.domain.components.appPageComponents.AppDescriptionComponent
import ru.blays.hub.core.domain.components.appPageComponents.AppVersionsListComponent
import ru.blays.hub.core.domain.components.appPageComponents.TabsChild
import ru.blays.hub.core.domain.components.appPageComponents.TabsConfiguration
import ru.blays.hub.core.domain.components.appPageComponents.VersionPageComponent
import ru.blays.hub.core.domain.data.models.ApkInfoCardModel
import ru.blays.hub.core.domain.data.models.AppVersionCard
import ru.blays.hub.core.domain.utils.openInBrowser
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.buttons.ActionButton
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.decomposePages.Pages
import ru.blays.hub.core.ui.elements.imageSlider.ImageSlider
import ru.blays.hub.core.ui.elements.infoDialog.InfoDialogContent
import ru.blays.hub.core.ui.elements.placeholder.FullscreenPlaceholder
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.utils.shadowPlus
import ru.blays.hub.core.ui.utils.withAlpha
import ru.blays.hub.core.ui.values.DefaultPadding

@OptIn(ExperimentalDecomposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppPageContent2(component: AppComponent) {
    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                AppComponent.Output.NavigateBack
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
                    titleText = component.appName
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            Tabs(
                tabs = component.childPages,
                onPageSelect = component::onPageSelected
            )
            VerticalSpacer(height = 4.dp)
            Pages(
                pages = component.childPages,
                onPageSelected = component::onPageSelected,
                scrollAnimation = PagesScrollAnimation.Default
            ) { _, page ->
                when (page) {
                    is TabsChild.Description -> AppDescriptionContent(component = page.component)
                    is TabsChild.NonRoot -> VersionContent(component = page.component)
                    is TabsChild.Root -> VersionContent(component = page.component)
                }
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun Tabs(
    modifier: Modifier = Modifier,
    tabs: Value<ChildPages<TabsConfiguration, TabsChild>>,
    onPageSelect: (index: Int) -> Unit
) {
    val state by tabs.subscribeAsState()
    val selectedIndex = state.selectedIndex
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedIndex,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                val tabPosition = tabPositions[selectedIndex]
                TabRowDefaults.PrimaryIndicator(
                    width = tabPosition.width / 3,
                    shape = indicatorShape,
                    modifier = Modifier.tabIndicatorOffset(tabPosition),
                )
            }
        }
    ) {
        val singleVersionType by remember {
            derivedStateOf {
                state.items.count {
                    it.configuration is TabsConfiguration.NonRoot ||
                    it.configuration is TabsConfiguration.Root
                } == 1
            }
        }

        state.items.forEachIndexed { index, child ->
            Tab(
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .clip(CircleShape),
                selected = index == selectedIndex,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onPageSelect(index) }
            ) {
                Text(
                    text = getTabName(
                        singleVersionType = singleVersionType,
                        tab = child.configuration
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun AppDescriptionContent(
    modifier: Modifier = Modifier,
    component: AppDescriptionComponent
) {
    val state by component.state.collectAsState()

    Crossfade(
        targetState = state,
        label = "stateCrossfade"
    ) { currentState ->
        when(currentState) {
            is AppDescriptionComponent.State.Error -> FullscreenPlaceholder(
                iconId = R.drawable.ic_smile_sad,
                contentDescriptionId = R.string.content_description_icon_smile_sad,
                message = currentState.message
            )
            is AppDescriptionComponent.State.Loaded -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(DefaultPadding.CardDefaultPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (currentState.images.isNotEmpty()) {
                        ImageSlider(images = currentState.images)
                    }
                    Text(
                        text = stringResource(id = R.string.description),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(10.dp)
                    )
                    MarkdownContent(
                        modifier = Modifier,
                        markdown = currentState.readme,
                    )
                }
            }
            is AppDescriptionComponent.State.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxWidth(0.4F)
                    )
                }
            }
            is AppDescriptionComponent.State.NotProvided -> {
                FullscreenPlaceholder(
                    iconId = R.drawable.ic_book_crossed,
                    messageId = R.string.appInfo_notProvided
                )
            }
        }
    }
}

@Composable
private fun VersionContent(
    modifier: Modifier = Modifier,
    component: AppVersionsListComponent
) {
    val pageInstance =
        component.versionPageSlot.subscribeAsState().value.child?.instance

    SharedTransitionLayout {
        AnimatedContent(
            targetState = pageInstance,
            modifier = modifier,
            label = "shared transition",
        ) { instance ->
            if (instance != null) {
                VersionPage(
                    component = instance,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                VersionsList(
                    modifier = Modifier.fillMaxSize(),
                    component = component
                )
            }
        }
    }
}

context(transitionScope: SharedTransitionScope, visibilityScope: AnimatedVisibilityScope)
@Suppress("NonSkippableComposable")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun VersionsList(
    modifier: Modifier = Modifier,
    component: AppVersionsListComponent
) = with(transitionScope) {
    val state by component.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = state == AppVersionsListComponent.State.Loading,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
                isRefreshing = state == AppVersionsListComponent.State.Loading,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        onRefresh = {
            component.sendIntent(
                AppVersionsListComponent.Intent.Refresh
            )
        },
    ) {
        Crossfade(
            targetState = state,
            label = "stateCrossfade"
        ) { state ->
            when (state) {
                is AppVersionsListComponent.State.Error -> {
                    FullscreenPlaceholder(
                        modifier = modifier.verticalScroll(rememberScrollState()),
                        iconId = R.drawable.ic_error,
                        message = stringResource(
                            R.string.error_notLoaded_formatted,
                            state.message
                        )
                    )
                }
                is AppVersionsListComponent.State.Loaded -> {
                    LazyColumn(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (state.appType.installed) {
                            stickyHeader {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(DefaultPadding.CardDefaultPadding)
                                        .background(MaterialTheme.colorScheme.surface),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        8.dp,
                                        Alignment.End
                                    )
                                ) {
                                    VersionActions(
                                        onLaunch = {
                                            component.sendIntent(
                                                AppVersionsListComponent.Intent.LaunchApp
                                            )
                                        },
                                        onDelete = {
                                            component.sendIntent(
                                                AppVersionsListComponent.Intent.Delete
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        items(items = state.appType.versionsList) { version ->
                            VersionItem(
                                version = version,
                                onClick = {
                                    component.onOutput(
                                        AppVersionsListComponent.Output.OpenVersionPage(version)
                                    )
                                },
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState(key = "container-${version.version}_${version.buildDate}"),
                                    animatedVisibilityScope = visibilityScope,
                                    clipInOverlayDuringTransition = OverlayClip(CardDefaults.shape),
                                )
                            )
                        }
                    }
                }
                AppVersionsListComponent.State.Loading -> {
                    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
                    Column(
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(4) {
                            VersionItemPlaceholder(shimmer = shimmer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionActions(
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    ActionButton(
        text = R.string.action_delete,
        icon = R.drawable.ic_delete,
        contentDescription = R.string.content_description_icon_delete,
        onClick = onDelete
    )
    ActionButton(
        text = R.string.action_launch,
        icon = R.drawable.ic_rocket_lunch_filled,
        contentDescription = R.string.content_description_icon_rocket,
        onClick = onLaunch
    )
}

context(transitionScope: SharedTransitionScope, visibilityScope: AnimatedVisibilityScope)
@Suppress("NonSkippableComposable")
@Composable
private fun VersionItem(
    modifier: Modifier = Modifier,
    version: AppVersionCard,
    onClick: () -> Unit
) = with(transitionScope) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(14.dp)
        ),
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
        ) {
            VersionInfo(
                version = version.version,
                patchesVersion = version.patchesVersion,
                buildDate = version.buildDate,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "Info-${version.version}_${version.buildDate}"),
                    animatedVisibilityScope = visibilityScope,
                )
            )
        }
    }
}

@Composable
fun VersionItemPlaceholder(
    modifier: Modifier = Modifier,
    shimmer: Shimmer
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurface.withAlpha(0.3F)
        ),
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
            .shimmer(shimmer)
    ) {
        VerticalSpacer(height = 50.dp)
    }
}

context(transitionScope: SharedTransitionScope, visibilityScope: AnimatedVisibilityScope)
@Suppress("NonSkippableComposable", "UpdateTransitionLabel", "TransitionPropertiesLabel")
@Composable
private fun VersionPage(
    modifier: Modifier = Modifier,
    component: VersionPageComponent
) = with(transitionScope) {
    val state by component.state.collectAsState()

    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    val dialogInstance = component.dialogSlot.subscribeAsState().value.child?.instance

    var apkListExpanded by remember { mutableStateOf(true) }
    var changelogExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = "container-${state.version}_${state.buildDate}"
                ),
                animatedVisibilityScope = visibilityScope,
                clipInOverlayDuringTransition = OverlayClip(CardDefaults.shape),
            )
            .background(
                color = MaterialTheme.colorScheme.surface
            )
    ) {
        Column(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .verticalScroll(rememberScrollState())
        ) {
            VerticalSpacer(height = 8.dp)
            VersionInfo(
                version = state.version,
                patchesVersion = state.patchesVersion,
                buildDate = state.buildDate,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("Info-${state.version}_${state.buildDate}"),
                    animatedVisibilityScope = visibilityScope,
                )
            )

            VerticalSpacer(height = 8.dp)

            val apkListTransition = updateTransition(targetState = apkListExpanded)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 30.dp)
                    .clickable { apkListExpanded = !apkListExpanded }
            ) {
                val iconRotate by apkListTransition.animateFloat {
                    if (it) 180f else 0f
                }
                Text(
                    text = stringResource(id = R.string.files),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalSpacer(width = 4.dp)
                HorizontalDivider(
                    modifier = Modifier.weight(1F),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalSpacer(width = 4.dp)
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_arrow_down
                    ),
                    contentDescription = stringResource(id = R.string.content_description_icon_down),
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(iconRotate)
                )
            }
            VerticalSpacer(height = 4.dp)
            apkListTransition.AnimatedVisibility(
                visible = { it },
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (state.apkListLoading) {
                        repeat(3) {
                            ApkItemPlaceholder(shimmer = shimmer)
                        }

                    } else {
                        state.apkList.forEach { apk ->
                            ApkItem(
                                modifier = Modifier.animateEnterExit(),
                                apk = apk,
                                onClick = {
                                    component.sendIntent(
                                        VersionPageComponent.Intent.InstallApk(apk)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            VerticalSpacer(height = 8.dp)

            val changelogTransition = updateTransition(targetState = changelogExpanded)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(min = 30.dp)
                    .clickable { changelogExpanded = !changelogExpanded }
            ) {
                val iconRotate by changelogTransition.animateFloat {
                    if (it) 180f else 0f
                }
                Text(
                    text = stringResource(id = R.string.changelog),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalSpacer(width = 4.dp)
                HorizontalDivider(
                    modifier = Modifier.weight(1F),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalSpacer(width = 4.dp)
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = stringResource(id = R.string.content_description_icon_down),
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(iconRotate)
                )
            }
            VerticalSpacer(height = 6.dp)
            changelogTransition.AnimatedVisibility(
                visible = { it },
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                LaunchedEffect(key1 = Unit) {
                    component.sendIntent(
                        VersionPageComponent.Intent.LoadChangelog
                    )
                }
                MarkdownContent(
                    markdown = state.changelog,
                    placeholderContent = {
                        ChangelogPlaceholder()
                    }
                )
            }
        }
    }

    if (dialogInstance != null) {
        InfoDialogContent(
            modifier = Modifier
                .shadowPlus(
                    radius = 6.dp,
                    color = MaterialTheme.colorScheme.primaryColorAtAlpha(0.8F),
                    shape = CardDefaults.shape,
                    offset = DpOffset(
                        x = 3.dp,
                        y = 3.dp
                    )
                ),
            component = dialogInstance,
            onDismissRequest = {
                dialogInstance.sendIntent(
                    ApkInstallAction.Cancel
                )
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_warning),
                    contentDescription = stringResource(id = R.string.content_description_icon_warning),
                    modifier = Modifier.size(24.dp)
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogInstance.sendIntent(
                            ApkInstallAction.Cancel
                        )
                    }
                ) {
                    Text(text = stringResource(id = R.string.action_cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dialogInstance.sendIntent(
                            ApkInstallAction.Reinstall
                        )
                    }
                ) {
                    Text(text = stringResource(id = R.string.action_reinstall))
                }
                HorizontalSpacer(width = 6.dp)
                TextButton(
                    onClick = {
                        dialogInstance.sendIntent(
                            ApkInstallAction.Continue
                        )
                    }
                ) {
                    Text(text = stringResource(id = R.string.action_continue))
                }
            }
        )
    }
}

@Composable
fun ApkItem(
    modifier: Modifier = Modifier,
    apk: ApkInfoCardModel,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(14.dp)
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1F),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val showDescription = apk.description.isNotEmpty()
                if (showDescription) {
                    Text(
                        text = apk.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    if (apk.description.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                id = R.string.description_formatted,
                                apk.description
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    VerticalSpacer(height = 3.dp)
                    Text(
                        text = apk.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    VerticalSpacer(height = 3.dp)
                }
            }
            HorizontalSpacer(width = 8.dp)
            Icon(
                painter = painterResource(id = R.drawable.ic_download_filled),
                contentDescription = stringResource(id = R.string.content_description_icon_download),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ApkItemPlaceholder(
    modifier: Modifier = Modifier,
    shimmer: Shimmer
) {
    val backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.withAlpha(0.4F)
    val shape = MaterialTheme.shapes.small
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(14.dp)
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "Apk name placeholder",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = Color.Transparent,
                modifier = Modifier
                    .shimmer(shimmer)
                    .background(backgroundColor, shape)
            )
            Text(
                text = "Apk description placeholder",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Transparent,
                modifier = Modifier
                    .shimmer(shimmer)
                    .background(backgroundColor, shape)
            )
        }
    }
}

@Composable
private fun VersionInfo(
    modifier: Modifier = Modifier,
    version: String,
    patchesVersion: String?,
    buildDate: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = stringResource(id = R.string.version_formatted, version),
            style = MaterialTheme.typography.titleMedium
        )
        patchesVersion?.let {
            Text(
                text = stringResource(id = R.string.patchesVersion_formatted, it),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = stringResource(id = R.string.buildDate_formatted, buildDate),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MarkdownContent(
    modifier: Modifier = Modifier,
    markdown: String? = null,
    placeholderContent: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
                shape = CardDefaults.shape
            )
    ) {
        Crossfade(
            targetState = markdown,
            label = "contentCrossfade"
        ) {
            if (it != null) {
                MarkdownText(
                    markdown = it,
                    truncateOnTextOverflow = false,
                    isTextSelectable = true,
                    disableLinkMovementMethod = true,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    linkColor = MaterialTheme.colorScheme.primary,
                    onLinkClicked = context::openInBrowser,
                    modifier = Modifier.padding(6.dp)
                )
            } else {
                placeholderContent()
            }
        }
    }
}

@Composable
private fun ChangelogPlaceholder(
    modifier: Modifier = Modifier,
    shimmer: Shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.Window)
) {
    Text(
        text = "Placeholder Placeholder\n".repeat(8),
        color = Color.Transparent,
        modifier = modifier.shimmer(shimmer)
    )
}

@NonRestartableComposable
@Composable
private fun getTabName(
    singleVersionType: Boolean,
    tab: TabsConfiguration
): String {
    return when (tab) {
        TabsConfiguration.Description -> stringResource(id = R.string.app_page_tab_description)
        is TabsConfiguration.NonRoot -> if(singleVersionType) {
            stringResource(R.string.versions)
        } else {
            stringResource(id = R.string.app_page_tab_nonRoot)
        }
        is TabsConfiguration.Root -> if(singleVersionType) {
            stringResource(R.string.versions)
        } else {
            stringResource(id = R.string.app_page_tab_root)
        }
    }
}

private val indicatorShape = RoundedCornerShape(
    topStart = 3.dp,
    topEnd = 3.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)