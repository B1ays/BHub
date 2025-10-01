package ru.blays.hub.core.domain.components.rootComponents

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.compose.runtime.Stable
import androidx.core.content.getSystemService
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import rikka.shizuku.Shizuku
import ru.blays.hub.core.deviceUtils.ShizukuState
import ru.blays.hub.core.deviceUtils.shizukuState
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.AppThemeAccessor
import ru.blays.hub.core.domain.PackageManagerAccessor
import ru.blays.hub.core.domain.PreferencesValidator
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.data.ThemePreferenceModel
import ru.blays.hub.core.domain.utils.collectWhile
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.utils.kotlinx.coroutines.withMain


@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Stable
class RootComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val initialConfiguration: Configuration,
    private val context: Context,
    private val preferencesValidator: PreferencesValidator,
    private val tabsComponentFactory: TabsComponent.Factory,
    private val dialogsComponentFactory: DialogsComponent.Factory,
) : AppComponentContext by componentContext {
    private val packageManagerValue = preferencesHolder.getValue(PackageManagerAccessor)

    private val stackNavigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = stackNavigation,
        initialConfiguration = Configuration.Splash,
        handleBackButton = false,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    val themeStateFlow: StateFlow<ThemePreferenceModel> =
        preferencesHolder.getValue(AppThemeAccessor)

    private fun childFactory(
        configuration: Configuration,
        childContext: AppComponentContext,
    ): Child = when (configuration) {
        is Configuration.Tabs -> Child.Tabs(
            tabsComponentFactory(componentContext = childContext)
        )
        is Configuration.Splash -> Child.Splash
        is Configuration.RootDialog -> Child.RootDialog(
            dialogsComponentFactory(
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
        state: ShizukuState,
    ) {
        when (state) {
            is ShizukuState.NotRunning -> {
                if (packageManagerValue.value == PackageManagerType.Shizuku) {
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
        val jobs = mutableListOf<Job>()

        lifecycle.doOnCreate {
            preferencesValidator.validate()

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
                if (packageManagerValue.value == PackageManagerType.Shizuku) {
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

            if (dialogConfigurations.isNotEmpty()) {
                stackNavigation.replaceCurrent(
                    Configuration.RootDialog(dialogConfigurations)
                )
            } else {
                if (packageManagerValue.value == PackageManagerType.Shizuku) {
                    if (shizukuStateValue is ShizukuState.Running) {
                        if (shizukuStateValue.permissionGranted) {
                            stackNavigation.replaceCurrent(initialConfiguration)
                        } else {
                            componentScope.launch {
                                shizukuStateFlow.collectWhile { shizukuState ->
                                    when (shizukuState) {
                                        is ShizukuState.Running -> {
                                            if (shizukuState.permissionGranted) {
                                                withContext(Dispatchers.Main) {
                                                    stackNavigation.replaceCurrent(
                                                        initialConfiguration
                                                    )
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

                withMain {
                    onShizukuStateChange(state)
                }
            }

            jobs += shizukuStateFlow
                .onEach(onShizukuStateChange)
                .launchIn(componentScope)

            jobs += packageManagerValue
                .onEach { type ->
                    if(type == PackageManagerType.Shizuku) {
                        onShizukuStateChange(shizukuStateValue)
                    }
                }
                .launchIn(componentScope)
        }
        lifecycle.doOnDestroy {
            jobs.forEach(Job::cancel)
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object Tabs : Configuration()

        @Serializable
        data object Splash : Configuration()

        @Serializable
        data class RootDialog(val configurations: List<DialogsComponent.Configuration>) :
            Configuration() {
            constructor(configuration: DialogsComponent.Configuration) : this(listOf(configuration))
        }
    }

    sealed class Child {
        data class Tabs(val component: TabsComponent) : Child()
        data class RootDialog(val component: DialogsComponent) : Child()
        data object Splash : Child()
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
        private val context: Context,
        private val preferencesValidator: PreferencesValidator,
        private val tabsComponentFactory: TabsComponent.Factory,
        private val dialogsComponentFactory: DialogsComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            initialConfiguration: Configuration = Configuration.Tabs,
        ): RootComponent {
            return RootComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                initialConfiguration = initialConfiguration,
                context = context,
                preferencesValidator = preferencesValidator,
                tabsComponentFactory = tabsComponentFactory,
                dialogsComponentFactory = dialogsComponentFactory,
            )
        }
    }
}