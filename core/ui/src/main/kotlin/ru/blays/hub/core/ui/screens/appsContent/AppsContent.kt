package ru.blays.hub.core.ui.screens.appsContent

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import ru.blays.hub.core.domain.components.appsComponent.AppsComponent
import ru.blays.hub.core.domain.data.models.AppCardModel
import ru.blays.hub.core.domain.data.models.VersionType
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.indicators.DotIndicator
import ru.blays.hub.core.ui.elements.lazyListItems.groupWithTitle
import ru.blays.hub.core.ui.elements.placeholder.FullscreenPlaceholder
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.theme.greenIndicatorColor
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.utils.withAlpha
import ru.blays.hub.core.ui.values.DefaultPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsContent(component: AppsComponent) {
    val state by component.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val shimmerState = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Scaffold(
        topBar = {
            CollapsingToolbar(
                collapsingTitle = CollapsingTitle(
                    stringResource(id = R.string.app_name_full),
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                ),
            )
        },
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = pullToRefreshState,
            isRefreshing = state == AppsComponent.State.Loading,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = pullToRefreshState,
                    isRefreshing = state == AppsComponent.State.Loading,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            onRefresh = {
                component.sendIntent(
                    AppsComponent.Intent.Refresh
                )
            },
        ) {
            Crossfade(
                targetState = state,
                label = "StateCrossfade"
            ) { currentState ->
                when(currentState) {
                    is AppsComponent.State.Error -> {
                        FullscreenPlaceholder(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            iconId = R.drawable.ic_error,
                            message = stringResource(
                                id = R.string.error_notLoaded_formatted,
                                currentState.message
                            )
                        )
                    }
                    is AppsComponent.State.Loaded -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            currentState.groups.forEach { group ->
                                groupWithTitle(
                                    title = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = 28.dp,
                                                    vertical = 4.dp
                                                ),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            DotIndicator(
                                                size = 8.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = group.catalogName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontSize = 17.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                ) {
                                    items(group.apps) { app ->
                                        AppItem(app = app) {
                                            component.onOutput(
                                                AppsComponent.Output.OpenApp(app)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    AppsComponent.State.Loading -> {
                        Column {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .padding(DefaultPadding.CardDefaultPadding)
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .shimmer(shimmerState)
                                        .background(
                                            color = MaterialTheme.colorScheme.onSurface.withAlpha(
                                                0.3F
                                            ),
                                            shape = CardDefaults.shape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItem(
    app: AppCardModel,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
        ),
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth()
    ) {
        val showDescription = app.description.isNotEmpty()
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (
                indicator,
                description,
                info
            ) = createRefs()

            val indicatorColor by animateColorAsState(
                targetValue = if(app.updateAvailable) {
                    greenIndicatorColor
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(0.3F)
                },
                label = "indicatorColor"
            )
            
            Box(
                modifier = Modifier
                    .background(color = indicatorColor)
                    .constrainAs(indicator) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                        width = Dimension.value(8.dp)
                    }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.constrainAs(info) {
                    top.linkTo(parent.top, margin = 6.dp)
                    if(!showDescription) {
                        bottom.linkTo(parent.bottom, margin = 6.dp)
                    }
                    start.linkTo(indicator.end, 6.dp)
                }
            ) {
                AsyncImage(
                    model = app.iconUrl,
                    contentDescription = stringResource(id = R.string.content_description_application_icon),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(54.dp)
                )
                HorizontalSpacer(width = 8.dp)
                
                Column {
                    Text(
                        text = app.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.W600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    VerticalSpacer(height = 6.dp)
                    app.versions.forEach { versionType ->
                        when (versionType) {
                            is VersionType.NonRoot -> {
                                NonRootVersionItem(versionType = versionType)
                            }
                            is VersionType.Root -> {
                                RootVersionItem(versionType = versionType)
                            }
                        }
                    }
                }
            }
            if(showDescription) {
                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.constrainAs(description) {
                        top.linkTo(info.bottom, 6.dp)
                        start.linkTo(info.start)
                        end.linkTo(parent.end, 12.dp)
                        width = Dimension.fillToConstraints
                    }
                )
            }
        }
    }
}

@Composable
fun NonRootVersionItem(
    modifier: Modifier = Modifier,
    versionType: VersionType.NonRoot,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        versionType.availableVersionName?.let {
            VersionItem(
                versionName = it,
                icon = painterResource(id = R.drawable.ic_cloud_source),
                modifier = Modifier.weight(0.5F, false)
            )
        }
        versionType.installedVersionName?.let {
            Dot()
            VersionItem(
                versionName = it,
                icon = painterResource(id = R.drawable.ic_device_loaded),
                modifier = Modifier.weight(0.5F, false)
            )
        }
    }
}

@Composable
fun RootVersionItem(
    modifier: Modifier = Modifier,
    versionType: VersionType.Root,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primaryColorAtAlpha(0.3F),
                shape = MaterialTheme.shapes.small
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if(
            versionType.availableVersionName != null ||
            versionType.installedVersionName != null
        ) {
            HorizontalSpacer(width = 4.dp)
            Icon(
                painter = painterResource(id = R.drawable.ic_root),
                contentDescription = stringResource(id = R.string.content_description_icon_root),
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(14.dp),
                        shape = MaterialTheme.shapes.small
                    )
            )
            HorizontalSpacer(width = 4.dp)
        }
        versionType.availableVersionName?.let {
            VersionItem(
                versionName = it,
                icon = painterResource(id = R.drawable.ic_cloud_source),
                modifier = Modifier.weight(0.5F, false)
            )
        }
        versionType.installedVersionName?.let {
            Dot()
            VersionItem(
                versionName = it,
                icon = painterResource(id = R.drawable.ic_device_loaded),
                modifier = Modifier.weight(0.5F, false)
            )
        }
    }
}

@Composable
fun VersionItem(
    modifier: Modifier = Modifier,
    versionName: String,
    icon: Painter,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = versionName,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun Dot(modifier: Modifier = Modifier) = DotIndicator(
    size = 6.dp,
    color = MaterialTheme.colorScheme.primary,
    modifier = modifier.padding(6.dp)
)