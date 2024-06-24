package ru.blays.hub.core.moduleManager

sealed class InstallStatus {
    data class InProgress(val message: String): InstallStatus()
    data object Success: InstallStatus()
    data class Failed(val error: Throwable): InstallStatus()
}