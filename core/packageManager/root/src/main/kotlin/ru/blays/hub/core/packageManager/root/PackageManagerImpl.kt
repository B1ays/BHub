package ru.blays.hub.core.packageManager.root

import android.content.Context
import android.content.Intent
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.coroutineScope
import ru.blays.hub.core.packageManager.ACTION_APP_INSTALL
import ru.blays.hub.core.packageManager.ACTION_APP_UNINSTALL
import ru.blays.hub.core.packageManager.EXTRA_ACTION_SUCCESS
import ru.blays.hub.core.packageManager.EXTRA_STATUS_MESSAGE
import ru.blays.hub.core.packageManager.LoggerAdapter
import ru.blays.hub.core.packageManager.PackageManager
import ru.blays.hub.core.packageManager.PackageManagerError
import ru.blays.hub.core.packageManager.PackageManagerResult
import ru.blays.hub.core.packageManager.getEnumForInstallFailed
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class PackageManagerImpl(
    private val context: Context,
    private val logger: LoggerAdapter
): PackageManager {
    override suspend fun getVersionCode(packageName: String): PackageManagerResult<Int> = coroutineScope {
        return@coroutineScope try {
            val dumpsys = Shell.cmd("dumpsys package $packageName | grep $VERSION_CODE_KEYWORD").awaitOutputOrThrow()
            val versionCode = dumpsys.substringBefore("minSdk").trim().removePrefix(VERSION_CODE_KEYWORD).toInt()
            PackageManagerResult.Success(versionCode)
        } catch (e: SuException) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_CODE,
                message = e.stderrOut
            )
        } catch (e: NumberFormatException) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_CODE,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun getVersionName(packageName: String): PackageManagerResult<String> = coroutineScope {
        return@coroutineScope try {
            val dumpsys = Shell.cmd("dumpsys package $packageName | grep $VERSION_NAME_KEYWORD").awaitOutputOrThrow()
            val versionName = dumpsys.split('=').last()

            PackageManagerResult.Success(versionName)
        } catch (e: SuException) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_NAME,
                message = e.stderrOut
            )
        }
    }

    override suspend fun checkPackageInstalled(
        packageName: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            val packagesQuery = Shell.cmd("pm list packages | cut -f 2 -d \":\"").exec()

            if(!packagesQuery.isSuccess) {
                logger.e(packagesQuery.errString)
            }

            packageName in packagesQuery.out
        } catch (e: SuException) {
            logger.e(e)
            false
        }
    }

    override suspend fun getInstallationDir(packageName: String): PackageManagerResult<String> = coroutineScope {
        return@coroutineScope try {
            val keyword = "path: "
            val dumpsys = Shell.cmd("dumpsys package $packageName | grep $keyword").awaitOutputOrThrow()
            val installationDir = dumpsys.removePrefix(keyword)

            PackageManagerResult.Success(installationDir)
        } catch (e: SuException) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_DIR,
                message = e.stderrOut
            )
        }
    }

    override suspend fun setInstaller(
        targetPackage: String,
        installerPackage: String
    ): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope try {
            Shell.cmd("pm set-installer $targetPackage $installerPackage").awaitOutputOrThrow()

            PackageManagerResult.Success(null)
        } catch (e: SuException) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.SET_FAILED_INSTALLER,
                message = e.stderrOut
            )
        }
    }

    override suspend fun forceStop(packageName: String): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope try {
            Shell.cmd("am force-stop $packageName").awaitOutputOrThrow()

            PackageManagerResult.Success(null)
        } catch (e: SuException) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.APP_FAILED_FORCE_STOP,
                message = e.stderrOut
            )
        }
    }

    override suspend fun installApp(apk: File): PackageManagerResult<Nothing> = coroutineScope {
        val apkPath = apk.absolutePath

        val size = apk.length()

        val install = Shell.cmd("cat \"$apkPath\" | pm install -d -S $size").exec()

        val success = install.isSuccess

        val broadcastIntent = Intent().apply {
            action = ACTION_APP_INSTALL
            if(!success) {
                putExtra(EXTRA_STATUS_MESSAGE, install.outString)
            }
            putExtra(EXTRA_ACTION_SUCCESS, success)
        }
        context.sendBroadcast(broadcastIntent)

        if (!success) {
            val errString = install.outString
            logger.e(errString)
            return@coroutineScope PackageManagerResult.Error(getEnumForInstallFailed(errString), errString)
        }

        return@coroutineScope PackageManagerResult.Success(null)
    }

    override suspend fun installSplitApp(apks: Array<File>): PackageManagerResult<Nothing> = coroutineScope {
        val sessionId = try {

            val installCreate = Shell.cmd("pm install-create -r").awaitOutputOrThrow()

            installCreate.toInt()
        } catch (e: SuException) {
            return@coroutineScope PackageManagerResult.Error(
                error = PackageManagerError.SESSION_FAILED_CREATE,
                message = e.stderrOut
            )
        } catch (e: NumberFormatException) {
            return@coroutineScope PackageManagerResult.Error(
                error = PackageManagerError.SESSION_INVALID_ID,
                message = e.stackTraceToString()
            )
        }

        for (apk in apks) {
            var tempApk: String? = null
            try {
                tempApk = copyApkToTemp(apk)
                Shell.cmd("pm install-write $sessionId '${apk.name}' '$tempApk'").awaitOutputOrThrow()
            } catch (e: SuException) {
                return@coroutineScope PackageManagerResult.Error(
                    error = PackageManagerError.SESSION_FAILED_WRITE,
                    message = e.stderrOut
                )
            } catch (e: IOException) {
                return@coroutineScope PackageManagerResult.Error(
                    error = PackageManagerError.SESSION_FAILED_COPY,
                    message = e.stackTraceToString()
                )
            } finally {
                tempApk?.let(::deleteTempApk)
            }
        }

        return@coroutineScope try {
            Shell.cmd("pm install-commit $sessionId").awaitOutputOrThrow()

            PackageManagerResult.Success(null)
        } catch (e: SuException) {
            PackageManagerResult.Error(
                error = getEnumForInstallFailed(e.stderrOut),
                message = e.stderrOut
            )
        }
    }

    override suspend fun uninstallApp(packageName: String): PackageManagerResult<Nothing> = coroutineScope {
        val uninstallAction = Shell.cmd("pm uninstall $packageName").exec()

        val message = uninstallAction.outString

        val broadcastIntent = Intent().apply {
            action = ACTION_APP_UNINSTALL
            val success = uninstallAction.isSuccess
            if(!success) {
                putExtra(EXTRA_STATUS_MESSAGE, message)
            }
            putExtra(EXTRA_ACTION_SUCCESS, success)
        }
        context.sendBroadcast(broadcastIntent)

        if(!uninstallAction.isSuccess) {
            logger.e(message)
            return@coroutineScope PackageManagerResult.Error(
                error = PackageManagerError.UNINSTALL_FAILED,
                message = message
            )
        }

        return@coroutineScope PackageManagerResult.Success(null)
    }

    override suspend fun launchApp(packageName: String): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope try {
            val launchCategoryLauncher = Shell.cmd("monkey -p $packageName -c android.intent.category.LAUNCHER 1").exec()
            if (!launchCategoryLauncher.isSuccess) Shell.cmd("monkey -p $packageName -c android.intent.category.DEFAULT 1").exec()
            PackageManagerResult.Success(null)
        } catch (e: SuException) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.LAUNCH_FAILED,
                message = e.stderrOut
            )
        }
    }

    @Throws(
        IOException::class,
        FileNotFoundException::class
    )
    private fun copyApkToTemp(apk: File): String? {
        val apkAbsolutePath = apk.absolutePath

        val tmpPath = "/data/local/tmp/'${apk.name}'"

        val copy = Shell.cmd("cp -f '$apkAbsolutePath' '$tmpPath'").exec()

        return if (copy.isSuccess) tmpPath else null
    }

    private fun deleteTempApk(tmpPath: String) {
        Shell.cmd("rm -f '$tmpPath'")
    }

    companion object {
        const val VERSION_NAME_KEYWORD = "versionName="
        const val VERSION_CODE_KEYWORD = "versionCode="
    }
}