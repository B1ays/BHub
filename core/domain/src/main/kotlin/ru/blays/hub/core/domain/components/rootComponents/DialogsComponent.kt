package ru.blays.hub.core.domain.components.rootComponents

import android.content.Context
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.backStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.components.InfoDialogComponent
import ru.blays.hub.core.domain.components.SetupComponent
import ru.blays.hub.core.domain.data.realType
import ru.blays.hub.core.domain.utils.openInBrowser
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.getPackageManager
import ru.blays.hub.core.packageManager.shizuku.SHIZUKU_PACKAGE_NAME
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.PMType
import kotlin.system.exitProcess

class DialogsComponent private constructor(
    componentContext: AppComponentContext,
    configurations: List<Configuration>,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    private val setupComponentFactory: SetupComponent.Factory,
    private val infoComponentFactory: InfoDialogComponent.Factory,
    private val shizukuDialogComponentFactory: ShizukuDialogComponent.Factory,
    private val onOutput: (Output) -> Unit
): AppComponentContext by componentContext {
    private val packageManager: PackageManager
        get() = getPackageManager(settingsRepository.pmType.realType)

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.Default) }
    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,
        initialStack = { configurations },
        handleBackButton = false,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.AddNewDialog -> navigation.pushToFront(intent.configuration)
        }
    }

    private fun childFactory(
        configuration: Configuration,
        childContext: AppComponentContext
    ): Child {
        return when(configuration) {
            is Configuration.Setup -> Child.Setup(
                setupComponentFactory(
                    componentContext = childContext,
                    onOutput = ::onSetupOutput
                )
            )
            is Configuration.RootDialog -> Child.RootDialog(
                infoComponentFactory(
                    componentContext = childContext,
                    state = configuration.dialogConfig,
                    onAction = ::onRootDialogAction

                )
            )
            is Configuration.ShizukuDialog -> Child.ShizukuDialog(
                shizukuDialogComponentFactory(
                    componentContext = childContext,
                    config = configuration.dialogConfig,
                    onAction = ::onShizukuDialogAction,
                    onOutput = ::onShizukuDialogOutput
                )
            )
        }
    }

    private fun onSetupOutput(output: SetupComponent.Output) {
        when(output) {
            SetupComponent.Output.Close -> closeIfLast()
        }
    }

    private fun onRootDialogAction(action: RootPermissionsDialogActions) {
        when(action) {
            RootPermissionsDialogActions.Close -> closeIfLast()
            RootPermissionsDialogActions.DisableRootMode -> {
                disableRootMode()
                closeIfLast()
            }
        }
    }

    private fun onShizukuDialogAction(action: ShizukuDialogComponent.Action) {
        when(action) {
            ShizukuDialogComponent.Action.Download -> openShizukuSite()
            ShizukuDialogComponent.Action.Open -> openShizuku()
            ShizukuDialogComponent.Action.Disable -> {
                disableShizuku()
                closeIfLast()
            }
        }
    }

    private fun onShizukuDialogOutput(output: ShizukuDialogComponent.Output) {
        when(output) {
            ShizukuDialogComponent.Output.Close -> closeIfLast()
        }
    }

    private fun disableRootMode() {
        settingsRepository.setValue {
            rootMode = false
        }
    }

    private fun openShizukuSite() {
        context.openInBrowser(context.getString(R.string.shizuku_official_site))
        exitProcess(0)
    }

    private fun openShizuku() {
        coroutineScope.launch {
            packageManager.launchApp(SHIZUKU_PACKAGE_NAME)
        }
    }

    private fun disableShizuku() {
        settingsRepository.setValue {
            pmType = PMType.NON_ROOT
        }
    }

    private fun closeIfLast() {
        if(childStack.backStack.isEmpty()) {
            onOutput.invoke(Output.Close)
        } else {
            navigation.pop()
        }
    }

    sealed class Intent {
        data class AddNewDialog(val configuration: Configuration): Intent()
    }

    sealed class Output {
        data object Close: Output()
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Setup: Configuration()
        @Serializable
        data class ShizukuDialog(val dialogConfig: ShizukuDialogComponent.Configuration): Configuration()
        @Serializable
        data class RootDialog(val dialogConfig: ShizukuDialogComponent.Configuration): Configuration()
    }
    sealed class Child {
        data class Setup(val component: SetupComponent): Child()
        data class ShizukuDialog(
            val component: InfoDialogComponent<ShizukuDialogComponent.Configuration, ShizukuDialogComponent.Action>
        ): Child()
        data class RootDialog(
            val component: InfoDialogComponent<ShizukuDialogComponent.Configuration, RootPermissionsDialogActions>
        ): Child()
    }

    sealed class RootPermissionsDialogActions {
        data object DisableRootMode: RootPermissionsDialogActions()
        data object Close: RootPermissionsDialogActions()
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val context: Context,
        private val setupComponentFactory: SetupComponent.Factory,
        private val infoComponentFactory: InfoDialogComponent.Factory,
        private val shizukuDialogComponentFactory: ShizukuDialogComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            configurations: List<Configuration>,
            onOutput: (Output) -> Unit
        ): DialogsComponent {
            return DialogsComponent(
                componentContext = componentContext,
                configurations = configurations,
                settingsRepository = settingsRepository,
                context = context,
                setupComponentFactory = setupComponentFactory,
                infoComponentFactory = infoComponentFactory,
                shizukuDialogComponentFactory = shizukuDialogComponentFactory,
                onOutput = onOutput,
            )
        }
    }
}