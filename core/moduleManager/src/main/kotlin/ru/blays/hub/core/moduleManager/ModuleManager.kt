package ru.blays.hub.core.moduleManager

import kotlinx.coroutines.flow.StateFlow

interface ModuleManager {
    fun install(request: InstallRequest): StateFlow<InstallStatus>
    fun delete(packageName: String): Boolean
    fun checkModuleExist(packageName: String): Boolean
}