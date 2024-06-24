package ru.blays.hub.core.deviceUtils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import ru.blays.hub.core.packageManager.utils.packageInstalled
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

val Context.shizukuState: SharedFlow<ShizukuState> by ShizukuStateDelegate

private data object ShizukuStateDelegate: ReadOnlyProperty<Context, SharedFlow<ShizukuState>> {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val flow: MutableSharedFlow<ShizukuState> by lazy { MutableSharedFlow(replay = 1) }

    private var initialized = false
    private var lastValue: ShizukuState? = null
    private var permissionGranted = false

    private suspend fun emit(value: ShizukuState) {
        flow.emit(value)
        lastValue = value
    }
    private fun tryEmit(value: ShizukuState) {
        flow.tryEmit(value)
        lastValue = value
    }

    override fun getValue(thisRef: Context, property: KProperty<*>): SharedFlow<ShizukuState> {
        if(initialized) {
            return flow.asSharedFlow()
        }

        coroutineScope.launch {
            val isShizukuInstalled = thisRef.packageInstalled(SHIZUKU_PACKAGE_NAME)
            if(isShizukuInstalled) {
                val isRunning = Shizuku.pingBinder()
                if(isRunning) {
                    permissionGranted = checkPermission()
                    emit(ShizukuState.Running(permissionGranted))
                } else {
                    emit(ShizukuState.NotRunning)
                }
                val handler = Handler(thisRef.mainLooper)
                Shizuku.addBinderReceivedListener(
                    {
                        permissionGranted = checkPermission()
                        tryEmit(ShizukuState.Running(permissionGranted))
                    },
                    handler
                )
                Shizuku.addBinderDeadListener(
                    { tryEmit(ShizukuState.NotRunning) },
                    handler
                )
                Shizuku.addRequestPermissionResultListener(
                    { _, result ->
                        permissionGranted = result == PackageManager.PERMISSION_GRANTED
                        if(lastValue is ShizukuState.Running) {
                            tryEmit(ShizukuState.Running(permissionGranted))
                        }
                    },
                    handler
                )
            } else {
                emit(ShizukuState.NotInstalled)
            }
        }
        initialized = true
        return flow.asSharedFlow()
    }

    private fun checkPermission(): Boolean {
        return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }
}


sealed class ShizukuState {
    data object NotInstalled: ShizukuState()
    data object NotRunning: ShizukuState()
    data class Running(val permissionGranted: Boolean): ShizukuState()
}

internal const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
