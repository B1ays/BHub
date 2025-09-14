package ru.blays.hub.core.packageManager.shizuku

import android.app.IActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageInstaller
import android.content.pm.IPackageInstallerSession
import android.content.pm.IPackageManager
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstallerHidden
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.PackageManagerHidden
import android.os.Build
import android.os.Process
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.coroutineScope
import rikka.shizuku.Shizuku
import rikka.shizuku.SystemServiceHelper
import ru.blays.hub.core.packageManager.api.ACTION_APP_INSTALL
import ru.blays.hub.core.packageManager.api.ACTION_APP_UNINSTALL
import ru.blays.hub.core.packageManager.api.EXTRA_ACTION_SUCCESS
import ru.blays.hub.core.packageManager.api.EXTRA_STATUS_MESSAGE
import ru.blays.hub.core.packageManager.api.LoggerAdapter
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.PackageManagerError
import ru.blays.hub.core.packageManager.api.PackageManagerResult
import ru.blays.hub.core.packageManager.shizuku.utils.asShizukuBinder
import ru.blays.hub.core.packageManager.shizuku.utils.wrap
import ru.blays.hub.core.packageManager.api.utils.intentSender
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch

class PackageManagerImpl(
    private val context: Context,
    private val logger: LoggerAdapter
): PackageManager {
    private val myUserId: Int
        get() = Process.myUserHandle().hashCode()
    private val isRoot : Boolean
        get() = Shizuku.getUid() == 0
    private val userId: Int
        get() = if (isRoot) myUserId else 0
    private val callerPackage: String
        get() = if (isRoot) context.packageName else SHELL_PACKAGE_NAME

    private val iActivityManager by lazy {
        IActivityManager.Stub.asInterface(SystemServiceHelper.getSystemService("activity").wrap())
    }

    private val iPackageManager: IPackageManager by lazy {
        IPackageManager.Stub.asInterface(SystemServiceHelper.getSystemService("package").wrap())
    }

    private val iPackageInstaller: IPackageInstaller by lazy {
        IPackageInstaller.Stub.asInterface(iPackageManager.packageInstaller.asShizukuBinder())
    }

    private val packageInstaller: PackageInstaller by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Refine.unsafeCast(
                PackageInstallerHidden(iPackageInstaller, callerPackage, null, userId)
            )
        } else {
            Refine.unsafeCast(
                PackageInstallerHidden(iPackageInstaller, callerPackage, userId)
            )
        }
    }

    private val packageManager: android.content.pm.PackageManager
        get() = context.packageManager

    private fun getPackageInfo(packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            iPackageManager.getPackageInfo(packageName, 0L, userId)
        } else {
            iPackageManager.getPackageInfo(packageName, 0, userId)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getVersionCode(
        packageName: String
    ): PackageManagerResult<Int> = coroutineScope {
        return@coroutineScope try {
            val packageInfo = getPackageInfo(packageName)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (packageInfo.longVersionCode and VERSION_IGNORE_MAJOR).toInt()
            } else {
                packageInfo.versionCode
            }
            PackageManagerResult.Success(versionCode)
        } catch (e: Exception) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_CODE,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun getVersionName(
        packageName: String
    ): PackageManagerResult<String> = coroutineScope {
        return@coroutineScope try {
            val packageInfo = getPackageInfo(packageName)
            val versionName = packageInfo.versionName
            PackageManagerResult.Success(versionName)
        } catch (e: Exception) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_NAME,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun checkPackageInstalled(
        packageName: String
    ): Boolean = coroutineScope {
        return@coroutineScope try {
            getPackageInfo(packageName)
            true
        } catch (e: NameNotFoundException) {
            logger.e(e)
            false
        }
    }

    override suspend fun getInstallationDir(
        packageName: String
    ): PackageManagerResult<String> = coroutineScope {
        return@coroutineScope try {
            val packageInfo = getPackageInfo(packageName)
            val installationDir = packageInfo.applicationInfo?.sourceDir
            PackageManagerResult.Success(installationDir)
        } catch (e: NameNotFoundException) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_DIR,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun setInstaller(
        targetPackage: String,
        installerPackage: String
    ): PackageManagerResult<Nothing> {
        return PackageManagerResult.Error(
            error = PackageManagerError.SET_FAILED_INSTALLER,
            message = "Unsupported"
        )
    }

    override suspend fun forceStop(
        packageName: String
    ): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope try {
            iActivityManager.forceStopPackage(packageName, userId)
            PackageManagerResult.Success(null)
        } catch (e: Exception) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.APP_FAILED_FORCE_STOP,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun installApp(apk: File): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope createInstallationSession {
            writeApkToSession(apk)
        }
    }

    override suspend fun installSplitApp(apks: Array<File>): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope createInstallationSession {
            apks.forEach { apk -> writeApkToSession(apk) }
        }
    }

    private suspend inline fun createInstallationSession(
        crossinline block: suspend PackageInstaller.Session.() -> Unit
    ): PackageManagerResult<Nothing> = coroutineScope {
        val params = PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL
        )
        val paramsHidden = Refine.unsafeCast<PackageInstallerHidden.SessionParamsHidden>(params)
        val newFlags =  paramsHidden.installFlags or
                PackageManagerHidden.INSTALL_REPLACE_EXISTING or
                INSTALL_ALLOW_DOWNGRADE or
                PackageManagerHidden.INSTALL_ALLOW_TEST
        paramsHidden.installFlags = newFlags

        val sessionId = packageInstaller.createSession(params)
        val iSession = IPackageInstallerSession.Stub.asInterface(
            iPackageInstaller.openSession(sessionId).asShizukuBinder()
        )
        val session = Refine.unsafeCast<PackageInstaller.Session>(
            PackageInstallerHidden.SessionHidden(iSession)
        )

        var success = false
        var message: String? = null

        val countDownLatch = CountDownLatch(1)
        val intentSender = intentSender { intent ->
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            if(status == PackageInstaller.STATUS_SUCCESS) {
                success = true
            } else {
                success = false
                message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
            }
            countDownLatch.countDown()
        }

        try {
            session.use {
                it.block()
                it.commit(intentSender)
                logger.d("Install: Session committed")
            }
        } catch (e: IOException) {
            logger.e(e)
            return@coroutineScope PackageManagerResult.Error(
                error = PackageManagerError.SESSION_FAILED_WRITE,
                message = e.stackTraceToString()
            )
        } catch (e: SecurityException) {
            logger.e(e)
            return@coroutineScope PackageManagerResult.Error(
                error = PackageManagerError.SESSION_FAILED_COMMIT,
                message = e.stackTraceToString()
            )
        } finally {
            session.close()
        }

        countDownLatch.await()

        session.abandon()

        val broadcastIntent = Intent().apply {
            action = ACTION_APP_INSTALL
            putExtra(EXTRA_ACTION_SUCCESS, success)
            if(!success) {
                putExtra(EXTRA_STATUS_MESSAGE, message)
            }
        }
        context.sendBroadcast(broadcastIntent)

        return@coroutineScope if(success) {
            PackageManagerResult.Success(null)
        } else {
            return@coroutineScope PackageManagerResult.Error(
                error = PackageManagerError.INSTALL_FAILED_UNKNOWN,
                message = message ?: "Unknown error"
            )
        }
    }

    private suspend fun PackageInstaller.Session.writeApkToSession(apk: File) = coroutineScope {
        apk.inputStream().use { inputStream ->
            openWrite(apk.name, 0, apk.length()).use { outputStream ->
                inputStream.copyTo(outputStream)
                fsync(outputStream)
            }
        }
    }

    override suspend fun uninstallApp(
        packageName: String
    ): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope try {
            val countDownLatch = CountDownLatch(1)

            var success = false
            var message: String? = null

            val intentSender = intentSender { intent ->
                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                if(status == PackageInstaller.STATUS_SUCCESS) {
                    success = true
                } else {
                    success = false
                    message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                }
                countDownLatch.countDown()
            }

            packageInstaller.uninstall(packageName, intentSender)

            countDownLatch.await()

            Intent().apply {
                action = ACTION_APP_UNINSTALL
                putExtra(EXTRA_ACTION_SUCCESS, success)
                if(!success) {
                    putExtra(EXTRA_STATUS_MESSAGE, message)
                }
            }.also(context::sendBroadcast)

            PackageManagerResult.Success(null)
        } catch (e: Exception) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.UNINSTALL_FAILED,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun launchApp(packageName: String): PackageManagerResult<Nothing> {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            context.startActivity(intent)
            PackageManagerResult.Success(null)
        } else PackageManagerResult.Error(
            error = PackageManagerError.LAUNCH_FAILED,
            message = "App launch failed"
        )
    }

    companion object {
        private const val VERSION_IGNORE_MAJOR = 0xFFFFFFFF
    }
}