package ru.blays.hub.core.moduleManager

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.blays.hub.core.modulemanager.R

class ModuleManagerImpl2(
    private val loggerAdapter: LoggerAdapter,
    private val context: Context,
): ModuleManager {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private fun getString(@StringRes id: Int) = context.getString(id)

    private suspend fun MutableStateFlow<InstallStatus>.updateStatus(status: InstallStatus) {
        emit(status)
        when(status) {
            is InstallStatus.Failed -> loggerAdapter.e(status.error)
            is InstallStatus.InProgress -> loggerAdapter.d(status.message)
            InstallStatus.Success -> loggerAdapter.i(getString(R.string.Install_complete))
        }
    }

    override fun install(request: InstallRequest): StateFlow<InstallStatus> {
        val statusFlow: MutableStateFlow<InstallStatus> = MutableStateFlow(
            InstallStatus.InProgress(
                getString(R.string.Install_progress_starting)
            )
        )

        coroutineScope.launch {
            statusFlow.updateStatus(
                InstallStatus.InProgress(
                    getString(R.string.Install_progress_checkModuleExists)
                )
            )
            if (!checkMainModuleExists()) {
                if (!createMainModule()) {
                    statusFlow.updateStatus(
                        InstallStatus.Failed(
                            ModuleManagerException("Unable to create main module")
                        )
                    )
                    return@launch
                }
            }

            val appFolderPath = getAppFolderPath(request.packageName)
            val modApkPath = "$appFolderPath$MODULE_FILE_BASE"
            val mountScriptPath = "$appFolderPath/$MODULE_FILE_MOUNT"
            val mountScript = createMountScript(modApkPath, request.packageName)

            statusFlow.updateStatus(
                InstallStatus.InProgress(
                    getString(R.string.Install_progress_createAppFolder)
                )
            )
            val createFolderResult = createFolder(appFolderPath)
            if (!createFolderResult.isSuccess) {
                statusFlow.updateStatus(
                    InstallStatus.Failed(
                        ModuleManagerException("Unable to create app folder")
                    )
                )
                return@launch
            }
            val writeScriptSuccess = writeToPath(mountScript, mountScriptPath)
            if (!writeScriptSuccess) {
                statusFlow.updateStatus(
                    InstallStatus.Failed(
                        ModuleManagerException("Unable to write mount script")
                    )
                )
                return@launch
            }
            val copyModApkSuccess = copyFile(request.filePath, modApkPath)
            if (!copyModApkSuccess) {
                statusFlow.updateStatus(
                    InstallStatus.Failed(
                        ModuleManagerException("Unable to copy apk file")
                    )
                )
                return@launch
            }
            unmountApp(request.packageName)
            mountApp(modApkPath, request.packageName)
            statusFlow.updateStatus(InstallStatus.Success)
        }
        return statusFlow
    }

    override fun delete(packageName: String): Boolean {
        val appPath = getAppFolderPath(packageName)
        val deleteResult = deleteFile(appPath)
        val deleteSuccess = deleteResult.isSuccess
        if (deleteSuccess) unmountApp(packageName)
        return deleteSuccess
    }

    override fun checkModuleExist(packageName: String): Boolean {
        return checkFolderExist(getAppFolderPath(packageName)).isSuccess
    }

    private fun checkMainModuleExists(): Boolean {
        val folderResult = checkFolderExist("$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/")
        val propResult = checkFileExist("$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/$MODULE_FILE_PROP")
        val scriptResult =
            checkFileExist("$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/$MODULE_FILE_SERVICE")
        return folderResult.isSuccess && propResult.isSuccess && scriptResult.isSuccess
    }

    private fun getAppFolderPath(packageName: String): String {
        return "$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/app/$packageName/"
    }

    private fun createMainModule(): Boolean {
        val folderPath = "$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/"
        val propFilePath = "$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/$MODULE_FILE_PROP"
        val scriptFilePath = "$MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/$MODULE_FILE_SERVICE"

        val createResult = createFolder(folderPath)
        if (!createResult.isSuccess) return false

        val writeModulePropSuccess = writeToPath(MODULE_PROP, propFilePath)
        if (!writeModulePropSuccess) return false

        val writeModuleScriptSuccess = writeToPath(MODULE_SCRIPT, scriptFilePath)
        if (!writeModuleScriptSuccess) return false

        return true
    }

    private fun createMountScript(modPath: String, packageName: String): String {
        val dumpsys = exec("dumpsys package $packageName | grep versionCode=")
        val versionCode = dumpsys.out.firstOrNull()
            ?.trim()
            ?.split(' ')
            ?.firstOrNull()
            ?.substringAfter('=')
            ?.toIntOrNull()
            ?: Int.MAX_VALUE

        return buildString {
            appendLine("#!/system/bin/sh")
            appendLine("currentVersionCode=\$(dumpsys package $packageName | grep versionCode= | awk -F' ' '{print \$1}' | awk -F'=' '{print \$2}')")
            appendLine("modVersionCode=$versionCode")
            appendLine("if [ \$currentVersionCode = \$modVersionCode ]; then")
            appendLine("    origApkPath=\$(pm path $packageName | cut -d \":\" -f2- | grep \"base.apk\")")
            appendLine("    mount -o bind $modPath \$origApkPath")
            appendLine("else")
            val notificationText = context.getString(R.string.notification_text_versionsWarning, packageName, versionCode)
            appendLine("    su -lp 2000 -c \"cmd notification post -S bigtext -t 'BHub' 'Warning' '$notificationText'\"")
            appendLine("fi")
        }
    }

    private fun unmountApp(packageName: String) {
        forceStop(packageName)
        exec("grep $packageName /proc/mounts | cut -d \" \" -f 2 | sed \"s/apk.*/apk/\" | xargs -r umount -vl")
    }

    private fun mountApp(
        modPath: String,
        packageName: String
    ) {
        val origApkPath = getInstalledApkPath(packageName) ?: return

        setChmod(modPath, 644) IF_SUCCESS {
            setChown(modPath, "system:system") IF_SUCCESS {
                setChcon(modPath, "u:object_r:apk_data_file:s0") IF_SUCCESS {
                    exec("su --mount-master -c mount -vo bind $modPath $origApkPath")
                }
            }
        }
        forceStop(packageName)
    }

    private fun forceStop(packageName: String) {
        exec("am force-stop $packageName")
    }
}