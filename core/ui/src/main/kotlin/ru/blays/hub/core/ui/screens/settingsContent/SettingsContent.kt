package ru.blays.hub.core.ui.screens.settingsContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.blays.hub.core.logic.components.settingsComponents.SettingsComponent
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingTitle
import ru.blays.hub.core.ui.elements.collapsingToolbar.CollapsingToolbar
import ru.blays.hub.core.ui.elements.settingsElements.SettingsClickableCard

@Composable
fun SettingsContent(component: SettingsComponent) {
    Scaffold(
        topBar = {
            CollapsingToolbar(
                actions = {
                    var menuExpanded by remember {
                        mutableStateOf(false)
                    }
                    IconButton(
                        onClick = { menuExpanded = !menuExpanded }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_vertical),
                            contentDescription = stringResource(id = R.string.content_description_icon_settings),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(text = stringResource(id = R.string.developerMenu))
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_curly_brackets),
                                    contentDescription = stringResource(id = R.string.content_description_icon_settings),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                component.onOutput(
                                    SettingsComponent.Output.DeveloperMenu
                                )
                            }
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(
                    titleText = stringResource(id = R.string.appBar_title_settings)
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                ThemeSetting {
                    component.onOutput(
                        SettingsComponent.Output.ThemeSettings
                    )
                }
            }
            item {
                CatalogsSetting {
                    component.onOutput(
                        SettingsComponent.Output.CatalogsSetting
                    )
                }
            }
            item {
                CommonSetting {
                    component.onOutput(
                        SettingsComponent.Output.MainSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSetting(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SettingsClickableCard(
        modifier = modifier,
        onClick = onClick,
        title = stringResource(R.string.settings_group_theme),
        icon = painterResource(R.drawable.ic_brush)
    )
}

@Composable
private fun CatalogsSetting(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SettingsClickableCard(
        modifier = modifier,
        onClick = onClick,
        title = stringResource(R.string.setting_title_catalogs),
        icon = painterResource(R.drawable.ic_code_branch)
    )
}

@Composable
private fun CommonSetting(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SettingsClickableCard(
        modifier = modifier,
        onClick = onClick,
        title = stringResource(R.string.settings_group_main),
        icon = painterResource(R.drawable.ic_sliders)
    )
}