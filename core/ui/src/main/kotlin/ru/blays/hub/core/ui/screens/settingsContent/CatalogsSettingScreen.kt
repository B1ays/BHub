package ru.blays.hub.core.ui.screens.settingsContent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import ru.blays.hub.core.domain.components.settingsComponents.catalogsSettings.AddCatalogComponent
import ru.blays.hub.core.domain.components.settingsComponents.catalogsSettings.CatalogsSettingComponent
import ru.blays.hub.core.domain.data.models.CatalogModel
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.autoscaleText.AutoscaleText
import ru.blays.hub.core.ui.elements.buttons.ActionButton
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.shapes.squircleShape.CornerSmoothing
import ru.blays.hub.core.ui.elements.shapes.squircleShape.SquircleShape
import ru.blays.hub.core.ui.elements.spacers.HorizontalSpacer
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.theme.colorDisabled
import ru.blays.hub.core.ui.theme.colorEnabled
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.values.DefaultPadding
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogsSettingScreen(
    modifier: Modifier = Modifier,
    component: CatalogsSettingComponent
) {
    val state by component.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val dialogInstance = component.childSlot.subscribeAsState().value.child?.instance

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                 CatalogsSettingComponent.Output.NavigateBack
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
                    titleText = stringResource(id = R.string.appBar_title_catalogs)
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    component.sendIntent(
                        CatalogsSettingComponent.Intent.AddCatalog
                    )
                },
                shape = SquircleShape(cornerSmoothing = CornerSmoothing.High),
                containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.7F),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = stringResource(R.string.content_description_icon_plus),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(padding),
            isRefreshing = state.loading,
            state = pullToRefreshState,
            onRefresh = {
                component.sendIntent(
                    CatalogsSettingComponent.Intent.Refresh
                )
            }
        ) {
            if(state.loading) {
                Box(
                    modifier = modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                ) {
                    items(state.catalogs) { catalog ->
                        CatalogItem(
                            catalog = catalog,
                            onClick = {
                                component.sendIntent(
                                    CatalogsSettingComponent.Intent.ChangeCatalogEnabled(
                                        catalog = catalog,
                                        enabled = !catalog.enabled
                                    )
                                )
                            },
                            onDelete = {
                                component.sendIntent(
                                    CatalogsSettingComponent.Intent.DeleteCatalog(catalog)
                                )
                            }
                        )
                    }
                }
            }
        }

    }

    dialogInstance?.let { dialogComponent ->
        BasicAlertDialog(
            onDismissRequest = dialogComponent::close,
            properties = DialogProperties(
                dismissOnClickOutside = false
            )
        ) {
            AddCatalogDialog(component = dialogComponent)
        }
    }
}

@Composable
private fun CatalogItem(
    modifier: Modifier = Modifier,
    catalog: CatalogModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1F)
                ) {
                    AutoscaleText(
                        text = catalog.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.owner_name_formatted, catalog.owner),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalSpacer(10.dp)
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .padding(8.dp)
                ) {
                    AnimatedContent(
                        targetState = catalog.enabled,
                        transitionSpec = {
                            fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                        },
                        label = "indicator animation"
                    ) { enabled ->
                        Icon(
                            painter = if(enabled) {
                                painterResource(R.drawable.ic_check)
                            } else {
                                painterResource(R.drawable.ic_cancel)
                            },
                            contentDescription = null,
                            tint = if(enabled) colorEnabled else colorDisabled,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            VerticalSpacer(6.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                ActionButton(
                    text = R.string.action_delete,
                    icon = R.drawable.ic_delete,
                    contentDescription = R.string.content_description_icon_delete,
                    onClick = onDelete
                )
            }
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun AddCatalogDialog(
    modifier: Modifier = Modifier,
    component: AddCatalogComponent
) {
    Card(
        modifier = modifier
    ) {
        Children(
            stack = component.steps,
            animation = stackAnimation(),
        ) {
            Column(
                modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingLarge)
            ) {
                Text(
                    text = stringResource(R.string.add_catalog),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(10.dp)
                )
                when(val child = it.instance) {
                    is AddCatalogComponent.Step.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CardDefaults.shape
                                )
                                .padding(DefaultPadding.CardDefaultPadding)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.error_formatted,
                                    child.component.errorMessage
                                )
                            )
                        }
                        VerticalSpacer(6.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                        ) {
                            TextButton(
                                onClick = child.component::backToEditUrl
                            ) {
                                Text(stringResource(R.string.action_back))
                            }
                            TextButton(
                                onClick = child.component::close
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    }
                    is AddCatalogComponent.Step.Info -> {
                        val catalog = child.component.catalog
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CardDefaults.shape
                                )
                                .padding(DefaultPadding.CardDefaultPadding)
                        ) {
                            Text(
                                text = stringResource(R.string.name_formatted, catalog.name),
                                style = MaterialTheme.typography.titleMedium
                            )
                            VerticalSpacer(4.dp)
                            Text(
                                text = stringResource(R.string.owner_name_formatted, catalog.owner),
                                style = MaterialTheme.typography.titleMedium
                            )
                            VerticalSpacer(4.dp)
                            Text(
                                text = stringResource(R.string.url_formatted, catalog.url),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        VerticalSpacer(6.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                        ) {
                            TextButton(
                                onClick = child.component::backToEditUrl
                            ) {
                                Text(stringResource(R.string.action_back))
                            }
                            TextButton(
                                onClick = child.component::save
                            ) {
                                Text(stringResource(R.string.action_add))
                            }
                        }
                    }
                    is AddCatalogComponent.Step.Input -> {
                        val textStateFlow = child.component.state
                        val text by textStateFlow.collectAsState()

                        val isTextNotEmpty by textStateFlow
                            .debounce(1.seconds)
                            .filter(String::isNotEmpty)
                            .map { it.toHttpUrlOrNull() != null }
                            .collectAsState(false)

                        OutlinedTextField(
                            value = text,
                            onValueChange = child.component::onTextChange,
                            label = {
                                Text(text = stringResource(id = R.string.setting_sourceUrl_label))
                            },
                            placeholder = {
                                Text(text = stringResource(id = R.string.setting_sourceUrl_placeholder))
                            },
                            singleLine = true,
                            shape = CardDefaults.shape,
                            modifier = Modifier.fillMaxWidth()
                        )
                        VerticalSpacer(6.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                        ) {
                            TextButton(
                                onClick = child.component::close
                            ) {
                                Text(
                                    text = stringResource(R.string.action_cancel)
                                )
                            }
                            TextButton(
                                onClick = child.component::submit,
                                enabled = isTextNotEmpty
                            ) {
                                Text(
                                    text = stringResource(R.string.action_submit)
                                )
                            }
                        }
                    }
                    AddCatalogComponent.Step.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(44.dp)
                            )
                            HorizontalSpacer(8.dp)
                            Text(
                                text = stringResource(R.string.loading),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    is AddCatalogComponent.Step.Exists -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    shape = CardDefaults.shape
                                )
                                .padding(DefaultPadding.CardDefaultPadding)
                        ) {
                            Text(
                                text = child.component.message
                            )
                        }
                        VerticalSpacer(6.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                        ) {
                            TextButton(
                                onClick = child.component::close
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    }
                }
            }
        }
    }
}