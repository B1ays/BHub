package ru.blays.hub.core.ui.screens.rootContent

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.blays.hub.core.logic.components.rootComponents.TabsComponent
import ru.blays.hub.core.ui.R
import ru.blays.hub.core.ui.elements.bubbleTabBar.BubbleTabBar
import ru.blays.hub.core.ui.elements.bubbleTabBar.BubbleTabBarDefaults
import ru.blays.hub.core.ui.elements.bubbleTabBar.BubbleTabConfig
import ru.blays.hub.core.ui.elements.bubbleTabBar.rememberBubbleTabBarState
import ru.blays.hub.core.ui.elements.infoDialog.InfoDialogContent
import ru.blays.hub.core.ui.elements.spacers.VerticalSpacer
import ru.blays.hub.core.ui.screens.aboutContent.AboutContent
import ru.blays.hub.core.ui.screens.appsContent.AppsRootContent
import ru.blays.hub.core.ui.screens.downloadsContent.DownloadsContent
import ru.blays.hub.core.ui.screens.settingsContent.SettingsRootContent
import ru.blays.hub.core.ui.utils.primaryColorAtAlpha
import ru.blays.hub.core.ui.utils.shadowPlus
import ru.blays.hub.core.ui.values.LocalStackAnimator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsContent(
    modifier: Modifier = Modifier,
    component: TabsComponent
) {
    val tabs = createTabs()
    val bubbleTabBarState = rememberBubbleTabBarState(tabs)

    val pmResultDialogInstance =
        component.pmResultDialog.subscribeAsState().value.child?.instance
    val mmResultDialogInstance =
        component.mmResultDialog.subscribeAsState().value.child?.instance
    val selfUpdateDialogInstance =
        component.selfUpdateComponent.dialog.subscribeAsState().value.child?.instance

    Scaffold(
        bottomBar = {
            BubbleTabBar(
                state = bubbleTabBarState,
                onValueChange = { _, configuration ->
                    (configuration.key as? TabsComponent.Configuration)?.let {
                        component.selectPage(it)
                    }
                },
            )
        },
        modifier = Modifier.systemBarsPadding()
    ) { padding ->
        Children(
            stack = component.childStack,
            animation = stackAnimation(LocalStackAnimator.current),
            modifier = modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            LaunchedEffect(it.configuration) {
                tabs.find { tab ->
                    tab.key == it.configuration
                }?.let { config ->
                    bubbleTabBarState.selectTab(config)
                }
            }

            when (val child = it.instance) {
                is TabsComponent.Child.Apps -> AppsRootContent(component = child.component)
                is TabsComponent.Child.Settings -> SettingsRootContent(component = child.component)
                is TabsComponent.Child.About -> AboutContent(component = child.component)
                is TabsComponent.Child.Downloads -> DownloadsContent(component = child.component)
            }
        }
    }

    Crossfade(
        targetState = selfUpdateDialogInstance,
        label = "bottomSheetCrossfade"
    ) {
        it?.let { selfUpdateDialogComponent ->
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxSize()
                    .background(
                        color = Color.Black.copy(0.6F)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = BottomSheetDefaults.ExpandedShape,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Column {
                        VerticalSpacer(height = 10.dp)
                        SelfUpdateContent(component = selfUpdateDialogComponent)
                    }
                }
            }
        }
    }

    pmResultDialogInstance?.let { dialogComponent ->
        val onDismissRequest = {
            dialogComponent.sendIntent(
                TabsComponent.AppInstallDialogActions.Close
            )
        }
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
            component = dialogComponent,
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.action_ok))
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_device_loaded),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
    }
    mmResultDialogInstance?.let { dialogComponent ->
        val onDismissRequest = {
            dialogComponent.sendIntent(
                TabsComponent.ModuleInstallDialogActions.Close
            )
        }
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
            component = dialogComponent,
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.action_ok))
                }
            },
            dismissButton = {
                /*TextButton(
                    onClick = {
                        dialogComponent.sendIntent(TabsComponent.ModuleInstallDialogActions.Reboot)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(text = stringResource(id = R.string.action_reboot))
                }*/
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_boxes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
    }
}

@Composable
private fun createTabs(): List<BubbleTabConfig> {
    val context = LocalContext.current
    val colors = BubbleTabBarDefaults.tabColors()

    return remember(colors) {
        listOf(
            BubbleTabConfig(
                title = context.getString(R.string.bottomBar_tab_apps),
                key = TabsComponent.Configuration.Apps,
                unselectedIcon = R.drawable.ic_home_outlined,
                selectedIcon = R.drawable.ic_home_filled,
                contentDescription = null,
                colors = colors
            ),
            BubbleTabConfig(
                title = context.getString(R.string.bottomBar_tab_downloads),
                key = TabsComponent.Configuration.Downloads,
                unselectedIcon = R.drawable.ic_download_outlined,
                selectedIcon = R.drawable.ic_download_filled,
                contentDescription = null,
                colors = colors,
            ),
            BubbleTabConfig(
                title = context.getString(R.string.bottomBar_tab_settings),
                key = TabsComponent.Configuration.Settings,
                unselectedIcon = R.drawable.ic_settings_outlined,
                selectedIcon = R.drawable.ic_settings_filled,
                contentDescription = null,
                colors = colors
            ),
            BubbleTabConfig(
                title = context.getString(R.string.bottomBar_tab_about),
                key = TabsComponent.Configuration.About,
                unselectedIcon = R.drawable.ic_info_outlined,
                selectedIcon = R.drawable.ic_info_filled,
                contentDescription = null,
                colors = colors
            )
        )
    }
}