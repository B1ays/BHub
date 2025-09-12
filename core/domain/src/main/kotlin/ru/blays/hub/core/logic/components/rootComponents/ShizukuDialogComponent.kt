package ru.blays.hub.core.logic.components.rootComponents

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rikka.shizuku.Shizuku
import ru.blays.hub.core.deviceUtils.ShizukuState
import ru.blays.hub.core.deviceUtils.shizukuState
import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.logic.components.IInfoDialogConfig
import ru.blays.hub.core.logic.components.InfoDialogComponent
import ru.blays.hub.core.logic.utils.collectWhile

class ShizukuDialogComponent(
    componentContext: ComponentContext,
    config: Configuration,
    onAction: (Action) -> Unit,
    private val onOutput: (Output) -> Unit
): InfoDialogComponent<ShizukuDialogComponent.Configuration, ShizukuDialogComponent.Action>(
    componentContext,
    config,
    onAction
), KoinComponent {
    private val context: Context by inject()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        lifecycle.doOnCreate {
            if(config is Configuration.ShizukuNotRunning) {
                val shizukuState = context.shizukuState
                coroutineScope.launch {
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
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
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

    companion object {
        private const val TAG = "ShizukuDialogComponent"
    }
}