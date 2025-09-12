package ru.blays.hub.core.logic.components.rootComponents

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.deviceUtils.DeviceUtils
import ru.blays.hub.core.logic.ACTION_MODULE_INSTALL
import ru.blays.hub.core.logic.R
import ru.blays.hub.core.logic.components.AboutComponent
import ru.blays.hub.core.logic.components.InfoDialogComponent
import ru.blays.hub.core.logic.components.InfoDialogConfig
import ru.blays.hub.core.logic.components.SelfUpdateComponent
import ru.blays.hub.core.logic.components.appsComponent.AppsRootComponent
import ru.blays.hub.core.logic.components.downloadComponents.DownloadsListComponent
import ru.blays.hub.core.logic.components.settingsComponents.SettingsRootComponent
import ru.blays.hub.core.logic.receivers.packageManagerReceiver
import ru.blays.hub.core.packageManager.ACTION_APP_INSTALL
import ru.blays.hub.core.packageManager.ACTION_APP_UNINSTALL

@OptIn(ExperimentalDecomposeApi::class)
@SuppressLint("UnspecifiedRegisterReceiverFlag")
class TabsComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext, KoinComponent {
    private val stackNavigation = StackNavigation<Configuration>()
    private val pmDialogNavigation = SlotNavigation<InfoDialogConfig>()
    private val mmDialogNavigation = SlotNavigation<InfoDialogConfig>()

    val childStack = childStack(
        source = stackNavigation,
        initialConfiguration = Configuration.Apps,
        handleBackButton = true,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    val pmResultDialog = childSlot(
        source = pmDialogNavigation,
        initialConfiguration = { null },
        serializer = InfoDialogConfig.serializer(),
        key = "pmResultDialog"
    ) { config, childContext ->
        InfoDialogComponent<InfoDialogConfig, AppInstallDialogActions>(
            componentContext = childContext,
            state = config,
            onAction = { action ->
                when (action) {
                    AppInstallDialogActions.Close -> pmDialogNavigation.dismiss()
                }
            }
        )
    }
    val mmResultDialog = childSlot(
        source = mmDialogNavigation,
        initialConfiguration = { null },
        serializer = InfoDialogConfig.serializer(),
        key = "mmResultDialog"
    ) { config, childContext ->
        InfoDialogComponent<InfoDialogConfig, ModuleInstallDialogActions>(
            componentContext = childContext,
            state = config,
            onAction = { action ->
                when (action) {
                    ModuleInstallDialogActions.Close -> mmDialogNavigation.dismiss()
                    ModuleInstallDialogActions.Reboot -> DeviceUtils.rebootDevice()
                }
            }
        )
    }
    val selfUpdateComponent = SelfUpdateComponent(
        componentContext = childContext("selfUpdateComponent"),
        checkOnCreate = true
    )

    fun selectPage(configuration: Configuration) {
        stackNavigation.bringToFront(configuration)
    }

    private fun childFactory(
        configuration: Configuration,
        childContext: ComponentContext,
    ): Child = when (configuration) {
        is Configuration.Apps -> Child.Apps(
            AppsRootComponent(childContext)
        )

        is Configuration.Settings -> Child.Settings(
            SettingsRootComponent(childContext)
        )

        is Configuration.About -> Child.About(
            AboutComponent(
                componentContext = childContext,
                onOutput = ::onAboutOutput
            )
        )

        is Configuration.Downloads -> Child.Downloads(
            DownloadsListComponent(childContext)
        )
    }

    private fun onAboutOutput(output: AboutComponent.Output) {
        when (output) {
            is AboutComponent.Output.NavigateBack -> stackNavigation.pop()
        }
    }

    init {
        val context: Context by inject()
        val receiver = packageManagerReceiver { action, success, message ->
            when(action) {
                ACTION_APP_INSTALL -> {
                    val config = InfoDialogConfig(
                        title = context.getString(R.string.app_installer_dialog_title),
                        message = if (success) {
                            context.getString(R.string.app_installer_dialog_success)
                        } else {
                            context.getString(
                                R.string.app_installer_dialog_error_formatted,
                                message
                            )
                        },
                    )
                    pmDialogNavigation.activate(
                        configuration = config
                    )
                }
                ACTION_APP_UNINSTALL -> {
                    val config = InfoDialogConfig(
                        title = context.getString(R.string.app_uninstaller_dialog_title),
                        message = if (success) {
                            context.getString(R.string.app_uninstaller_dialog_success)
                        } else {
                            context.getString(
                                R.string.app_uninstaller_dialog_error_formatted,
                                message
                            )
                        },
                    )
                    pmDialogNavigation.activate(
                        configuration = config
                    )
                }
                ACTION_MODULE_INSTALL -> {
                    val config = InfoDialogConfig(
                        title = context.getString(R.string.module_installer_dialog_title),
                        message = if (success) {
                            context.getString(R.string.module_installer_dialog_success)
                        } else {
                            context.getString(
                                R.string.module_installer_dialog_error_formatted,
                                message
                            )
                        },
                    )
                    mmDialogNavigation.activate(
                        configuration = config
                    )
                }
                else -> Unit
            }
        }

        lifecycle.doOnCreate {
            val intentFilter = IntentFilter().apply {
                addAction(ACTION_APP_INSTALL)
                addAction(ACTION_APP_UNINSTALL)
                addAction(ACTION_MODULE_INSTALL)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    receiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    receiver,
                    intentFilter
                )
            }
        }
        lifecycle.doOnDestroy {
            context.unregisterReceiver(receiver)
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Apps : Configuration()

        @Serializable
        data object Settings : Configuration()

        @Serializable
        data object About : Configuration()

        @Serializable
        data object Downloads : Configuration()

        companion object {
            internal val all: List<Configuration> = listOf(
                Apps,
                Settings,
                About,
                Downloads
            )
        }
    }

    sealed class Child {
        data class Apps(val component: AppsRootComponent) : Child()
        data class Settings(val component: SettingsRootComponent) : Child()
        data class About(val component: AboutComponent) : Child()
        data class Downloads(val component: DownloadsListComponent) : Child()
    }

    sealed class AppInstallDialogActions {
        data object Close : AppInstallDialogActions()
    }

    sealed class ModuleInstallDialogActions {
        data object Close : ModuleInstallDialogActions()
        data object Reboot : ModuleInstallDialogActions()
    }
}