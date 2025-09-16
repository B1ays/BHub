package ru.blays.hub.core.domain.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.PowerManager
import android.widget.Toast
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.preferences.SettingsRepository

class SetupComponent private constructor(
    componentContext: AppComponentContext,
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val onOutput: (Output) -> Unit
) : AppComponentContext by componentContext {
    private val powerManager = context.getSystemService<PowerManager>()!!

    private val _state = MutableStateFlow(State())

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.ChangeNotificationsSendStatus -> _state.update {
                it.copy(
                    notificationsSendGranted = intent.granted
                )
            }
            Intent.EnableRootMode -> {
                if(DeviceInfo.isRootGranted) {
                    _state.update {
                        it.copy(rootMode = true)
                    }
                    settingsRepository.setValue {
                        rootMode = true
                    }
                } else {
                    Toast.makeText(
                        context,
                        R.string.error_root_not_granted,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            Intent.RecheckPermissions -> checkPermissions()
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    private fun checkPermissions() {
        val notificationsSendGranted = if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val installAppsGranted = context.packageManager.canRequestPackageInstalls()
        val rootGranted = DeviceInfo.isRootGranted
        val batteryNotOptimized = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        _state.update {
            State(
                notificationsSendGranted = notificationsSendGranted,
                installAppsGranted = installAppsGranted,
                rootMode = rootGranted,
                batteryNotOptimized = batteryNotOptimized
            )
        }
    }

    sealed class Intent {
        data class ChangeNotificationsSendStatus(val granted: Boolean) : Intent()
        data object EnableRootMode : Intent()
        data object RecheckPermissions : Intent()
    }

    sealed class Output {
        data object Close : Output()
    }

    data class State(
        val notificationsSendGranted: Boolean = false,
        val installAppsGranted: Boolean = false,
        val rootMode: Boolean = false,
        val batteryNotOptimized: Boolean = false
    )

    class Factory(
        private val context: Context,
        private val settingsRepository: SettingsRepository,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit
        ): SetupComponent {
            return SetupComponent(
                componentContext = componentContext,
                context = context,
                settingsRepository = settingsRepository,
                onOutput = onOutput
            )
        }
    }
}