package ru.blays.hub.core.logic.components.rootComponents

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import androidx.compose.runtime.Stable
import androidx.core.content.getSystemService
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rikka.shizuku.Shizuku
import ru.blays.hub.core.deviceUtils.ShizukuState
import ru.blays.hub.core.deviceUtils.shizukuState
import ru.blays.hub.core.logic.R
import ru.blays.hub.core.logic.utils.collectWhile
import ru.blays.hub.core.logic.utils.validateSettings
import ru.blays.hub.core.preferences.SettingsRepository
import ru.blays.hub.core.preferences.proto.PMType
import ru.blays.hub.core.preferences.proto.ThemeSettings


@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Stable
class RootComponent internal constructor(
    componentContext: ComponentContext,
    private val initialConfiguration: Configuration = Configuration.Tabs
) : ComponentContext by componentContext, KoinComponent {
    constructor(componentContext: ComponentContext): this(
        componentContext,
        Configuration.Tabs
    )

    private val settingsRepository: SettingsRepository by inject()
    private val context: Context by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val stackNavigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = stackNavigation,
        initialConfiguration = Configuration.Splash,
        handleBackButton = false,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    val themeStateFlow: StateFlow<ThemeSettings>
        get() = settingsRepository.themeSettingsFlow

    private fun childFactory(
        configuration: Configuration,
        childContext: ComponentContext
    ): Child = when (configuration) {
        is Configuration.Tabs -> Child.Tabs(
            TabsComponent(childContext)
        )
        is Configuration.Splash -> Child.Splash
        is Configuration.RootDialog -> Child.RootDialog(
            DialogsComponent(
                componentContext = childContext,
                configurations = configuration.configurations,
                onOutput = ::onDialogsComponentOutput
            )
        )
    }

    private fun onDialogsComponentOutput(output: DialogsComponent.Output) {
        when (output) {
            DialogsComponent.Output.Close -> {
                stackNavigation.replaceCurrent(Configuration.Tabs)
            }
        }
    }

    private fun onShizukuStateChange(
        state: ShizukuState
    ) {
        when (state) {
            is ShizukuState.NotRunning -> {
                if (settingsRepository.pmType == PMType.SHIZUKU) {
                    val configuration = DialogsComponent.Configuration.ShizukuDialog(
                        ShizukuDialogComponent.Configuration.ShizukuNotRunning(
                            title = context.getString(R.string.shizuku_dialog_notRunning_title),
                            message = context.getString(R.string.shizuku_dialog_notRunning_message)
                        )
                    )
                    when (val child = childStack.active.instance) {
                        is Child.RootDialog -> {
                            child.component.sendIntent(
                                DialogsComponent.Intent.AddNewDialog(configuration)
                            )
                        }
                        else -> {
                            stackNavigation.replaceCurrent(
                                Configuration.RootDialog(configuration)
                            )
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    private fun sendNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun checkBatteryNotOptimized(): Boolean {
        val powerManager = context.getSystemService<PowerManager>()
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    init {
        val handler by lazy { Handler(context.mainLooper) }
        lifecycle.doOnCreate {
            settingsRepository.validateSettings(context)

            val sendNotificationsGranted = sendNotificationsGranted()
            val installRequestGranted = context.packageManager.canRequestPackageInstalls()
            val batteryNotOptimized = checkBatteryNotOptimized()

            val shizukuStateFlow = context.shizukuState
            var shizukuStateValue: ShizukuState = runBlocking { shizukuStateFlow.first() }

            val dialogConfigurations: List<DialogsComponent.Configuration> = buildList {
                if (
                    !sendNotificationsGranted ||
                    !installRequestGranted ||
                    !batteryNotOptimized
                    ) {
                    add(DialogsComponent.Configuration.Setup)
                }
                if (settingsRepository.pmType == PMType.SHIZUKU) {
                    if (shizukuStateValue == ShizukuState.NotInstalled) {
                        add(
                            DialogsComponent.Configuration.ShizukuDialog(
                                ShizukuDialogComponent.Configuration.ShizukuNotInstalled(
                                    title = context.getString(R.string.shizuku_dialog_notInstalled_title),
                                    message = context.getString(R.string.shizuku_dialog_notInstalled_message)
                                )
                            )
                        )
                    }
                    if (shizukuStateValue == ShizukuState.NotRunning) {
                        add(
                            DialogsComponent.Configuration.ShizukuDialog(
                                ShizukuDialogComponent.Configuration.ShizukuNotRunning(
                                    title = context.getString(R.string.shizuku_dialog_notRunning_title),
                                    message = context.getString(R.string.shizuku_dialog_notRunning_message)
                                )
                            )
                        )
                    }
                }
            }

            if(dialogConfigurations.isNotEmpty()) {
                stackNavigation.replaceCurrent(
                    Configuration.RootDialog(dialogConfigurations)
                )
            } else {
                if(settingsRepository.pmType == PMType.SHIZUKU) {
                    if(shizukuStateValue is ShizukuState.Running) {
                        if(shizukuStateValue.permissionGranted) {
                            stackNavigation.replaceCurrent(initialConfiguration)
                        } else {
                            coroutineScope.launch {
                                shizukuStateFlow.collectWhile { shizukuState ->
                                    when(shizukuState) {
                                        is ShizukuState.Running -> {
                                            if(shizukuState.permissionGranted) {
                                                handler.post {
                                                    stackNavigation.replaceCurrent(initialConfiguration)
                                                }
                                                false
                                            } else {
                                                Shizuku.requestPermission(9011)
                                                true
                                            }
                                        }
                                        else -> true
                                    }
                                }
                            }
                        }
                    }
                } else {
                    stackNavigation.replaceCurrent(initialConfiguration)
                }
            }

            val onShizukuStateChange: suspend (ShizukuState) -> Unit = { state ->
                shizukuStateValue = state
                handler.post { onShizukuStateChange(state) }
            }

            coroutineScope.launch {
                shizukuStateFlow.collect(onShizukuStateChange)
            }
            coroutineScope.launch {
                settingsRepository.pmTypeFlow.collect { type ->
                    if (type == PMType.SHIZUKU) {
                        onShizukuStateChange(shizukuStateValue)
                    }
                }
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Tabs : Configuration()

        @Serializable
        data object Splash : Configuration()

        @Serializable
        data class RootDialog(val configurations: List<DialogsComponent.Configuration>) : Configuration() {
            constructor(configuration: DialogsComponent.Configuration) : this(listOf(configuration))
        }
    }

    sealed class Child {
        data class Tabs(val component: TabsComponent) : Child()
        data class RootDialog(val component: DialogsComponent) : Child()
        data object Splash : Child()
    }
}