package ru.blays.hub.core.packageManager.nonRoot

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.coroutineScope
import ru.blays.hub.core.packageManager.ACTION_APP_INSTALL
import ru.blays.hub.core.packageManager.ACTION_APP_UNINSTALL
import ru.blays.hub.core.packageManager.EXTRA_ACTION_SUCCESS
import ru.blays.hub.core.packageManager.EXTRA_STATUS_MESSAGE
import ru.blays.hub.core.packageManager.LoggerAdapter
import ru.blays.hub.core.packageManager.PackageManager
import ru.blays.hub.core.packageManager.PackageManagerError
import ru.blays.hub.core.packageManager.PackageManagerResult
import ru.blays.hub.core.packageManager.nonRoot.utils.doubleUnionTryCatch
import ru.blays.hub.core.packageManager.nonRoot.utils.tripleUnionTryCatch
import ru.blays.hub.core.packageManager.utils.intentSender
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import android.content.pm.PackageManager as AndroidPackageManager

class PackageManagerImpl(
    private val context: Context,
    private val logger: LoggerAdapter
): PackageManager {
    private fun getPackageInfo(packageName: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(packageName, FLAG_NOTHING)
        } else {
            context.packageManager.getPackageInfo(packageName, 0)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getVersionCode(packageName: String): PackageManagerResult<Int> = coroutineScope {
        return@coroutineScope try {
            val packageInfo = getPackageInfo(packageName)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.and(VERSION_IGNORE_MAJOR).toInt()
            } else {
                packageInfo.versionCode
            }
            PackageManagerResult.Success(versionCode)
        } catch (e: NameNotFoundException) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_CODE,
                message = e.stackTraceToString()
            )
        }
    }

    @SuppressLint("WrongConstant")
    override suspend fun getVersionName(packageName: String): PackageManagerResult<String> = coroutineScope {
        return@coroutineScope try {
            val packageInfo = getPackageInfo(packageName)
            val versionName = packageInfo.versionName
            PackageManagerResult.Success(versionName)
        } catch (e: NameNotFoundException) {
            PackageManagerResult.Error(
                error = PackageManagerError.GET_FAILED_PACKAGE_VERSION_NAME,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun checkPackageInstalled(packageName: String): Boolean = coroutineScope {
        val packageManager = context.packageManager
        return@coroutineScope try {
            packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: NameNotFoundException) {
            false
        }
    }

    override suspend fun getInstallationDir(packageName: String): PackageManagerResult<String> = coroutineScope {
        return@coroutineScope try {
            val packageInfo = getPackageInfo(packageName)
            val installationDir = packageInfo.applicationInfo.sourceDir
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

    override suspend fun forceStop(packageName: String): PackageManagerResult<Nothing> = coroutineScope {
        return@coroutineScope PackageManagerResult.Error(
            error = PackageManagerError.APP_FAILED_FORCE_STOP,
            message = "Unsupported"
        )
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

    @Suppress("DEPRECATION")
    override suspend fun uninstallApp(packageName: String): PackageManagerResult<Nothing> = coroutineScope {
        val packageInstaller = context.packageManager.packageInstaller

        return@coroutineScope try {
            var success = false
            var message: String? = null

            val countDownLatch = CountDownLatch(1)

            val intentSender = intentSender { intent ->
                val status = intent.getIntExtra(
                    PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE
                )
                logger.d("Uninstall IntentSender: status = $status")
                if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    val actionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    } else {
                        intent.getParcelableExtra(Intent.EXTRA_INTENT)
                    }
                    actionIntent?.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    actionIntent?.let(context::startActivity)
                    return@intentSender
                }
                if (status == PackageInstaller.STATUS_SUCCESS) {
                    success = true
                } else {
                    success = false
                    message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                }
                countDownLatch.countDown()
            }

            packageInstaller.uninstall(packageName, intentSender)

            countDownLatch.await()

            val broadcastIntent = Intent().apply {
                action = ACTION_APP_UNINSTALL
                putExtra(EXTRA_ACTION_SUCCESS, success)
                if (!success) {
                    putExtra(EXTRA_STATUS_MESSAGE, message)
                }
            }
            context.sendBroadcast(broadcastIntent)

            return@coroutineScope if (success) {
                PackageManagerResult.Success(null)
            } else {
                PackageManagerResult.Error(
                    error = PackageManagerError.UNINSTALL_FAILED,
                    message = message ?: ""
                )
            }
        } catch (e: Exception) {
            logger.e(e)
            PackageManagerResult.Error(
                error = PackageManagerError.UNINSTALL_FAILED,
                message = e.stackTraceToString()
            )
        }
    }

    override suspend fun launchApp(packageName: String): PackageManagerResult<Nothing> {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            context.startActivity(intent)
            PackageManagerResult.Success(null)
        } else PackageManagerResult.Error(
            error = PackageManagerError.LAUNCH_FAILED,
            message = "App launch failed"
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Suppress("DEPRECATION")
    private suspend inline fun createInstallationSession(
        crossinline block: suspend PackageInstaller.Session.() -> Unit
    ): PackageManagerResult<Nothing> = coroutineScope {
        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams = PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL
        ).applySessionParams()

        val newSessionId = tripleUnionTryCatch<IOException, SecurityException, IllegalArgumentException, Int>(
            onCatch = {
                logger.e(it)
                return@coroutineScope PackageManagerResult.Error(
                    error = PackageManagerError.SESSION_FAILED_CREATE,
                    message = it.stackTraceToString()
                )
            }
        ) {
            packageInstaller.createSession(sessionParams)
        }

        val session = doubleUnionTryCatch<IOException, SecurityException, PackageInstaller.Session>(
            onCatch = {
                logger.e(it)
                return@coroutineScope PackageManagerResult.Error(
                    error = PackageManagerError.SESSION_FAILED_CREATE,
                    message = it.stackTraceToString()
                )
            },
            onTry = {
                packageInstaller.openSession(newSessionId)
            }
        )

        logger.d("Install: Created session with id: $newSessionId")

        var success = false
        var message: String? = null

        val countDownLatch = CountDownLatch(1)

        val intentSender = intentSender { intent ->
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            logger.d("Install IntentSender: status = $status")
            if(status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                val actionIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                actionIntent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                actionIntent?.let(context::startActivity)
                return@intentSender
            }
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

        if (success) {
            return@coroutineScope PackageManagerResult.Success(null)
        } else {
            return@coroutineScope PackageManagerResult.Error(
                PackageManagerError.INSTALL_FAILED_UNKNOWN,
                message ?: ""
            )
        }
    }

    private suspend fun PackageInstaller.Session.writeApkToSession(apk: File) = coroutineScope {
        apk.inputStream().use { inputStream ->
            openWrite(apk.name, 0, apk.length()).use { outputStream ->
                inputStream.copyTo(outputStream, byteArraySize)
                fsync(outputStream)
            }
        }
    }

    private fun SessionParams.applySessionParams(): SessionParams = apply {
        setInstallReason(AndroidPackageManager.INSTALL_REASON_USER)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRequireUserAction(SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setRequestUpdateOwnership(true)
        }
    }

    private companion object {
        private const val TAG = "NonRootPackageManager"

        private const val byteArraySize = 1024 * 1024

        private const val VERSION_IGNORE_MAJOR = 0xFFFFFFFF

        @delegate:RequiresApi(Build.VERSION_CODES.TIRAMISU)
        val FLAG_NOTHING by lazy { AndroidPackageManager.PackageInfoFlags.of(0) }
    }
}