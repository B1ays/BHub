package ru.blays.hub.core.domain.components.rootComponents

import android.content.Context
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import rikka.shizuku.Shizuku
import ru.blays.hub.core.deviceUtils.ShizukuState
import ru.blays.hub.core.deviceUtils.shizukuState
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.components.IInfoDialogConfig
import ru.blays.hub.core.domain.components.InfoDialogComponent
import ru.blays.hub.core.domain.utils.collectWhile
import ru.blays.hub.core.logger.Logger

class ShizukuDialogComponent private constructor(
    componentContext: AppComponentContext,
    config: Configuration,
    onAction: (Action) -> Unit,
    private val context: Context,
    private val onOutput: (Output) -> Unit
): InfoDialogComponent<ShizukuDialogComponent.Configuration, ShizukuDialogComponent.Action>(
    componentContext,
    config,
    onAction
) {
    init {
        lifecycle.doOnCreate {
            if(config is Configuration.ShizukuNotRunning) {
                val shizukuState = context.shizukuState
                componentScope.launch {
                    shizukuState.collectWhile { stateValue ->
                        Logger.d(TAG, "Shizuku state: $stateValue")
                        if(stateValue is ShizukuState.Running) {
                            if(stateValue.permissionGranted) {
                                Logger.d(TAG, "Shizuku permission granted")
                                onOutput(Output.Close)
                                false
                            } else {
                                Shizuku.requestPermission(9901)
                                true
                            }
                        } else {
                            true
                        }
                    }
                }
            }
        }
    }

    sealed class Action {
        data object Disable: Action()
        data object Download: Action()
        data object Open: Action()
    }

    sealed class Output {
        data object Close: Output()
    }

    @Serializable
    sealed class Configuration: IInfoDialogConfig {
        @Serializable
        data class ShizukuNotInstalled(
            override val title: String,
            override val message: String
        ): Configuration()
        @Serializable
        data class ShizukuNotRunning(
            override val title: String,
            override val message: String
        ): Configuration()
    }

    class Factory(
        private val context: Context,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            config: Configuration,
            onAction: (Action) -> Unit,
            onOutput: (Output) -> Unit
        ): ShizukuDialogComponent {
            return ShizukuDialogComponent(
                componentContext = componentContext,
                config = config,
                onAction = onAction,
                context = context,
                onOutput = onOutput
            )
        }
    }

    companion object {
        private const val TAG = "ShizukuDialogComponent"
    }
}