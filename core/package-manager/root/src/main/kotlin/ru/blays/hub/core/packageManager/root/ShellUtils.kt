package ru.blays.hub.core.packageManager.root

import com.topjohnwu.superuser.Shell
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal val Shell.Result.outString
    get() = out.joinToString("\n")

internal val Shell.Result.errString
    get() = err.joinToString("\n")

val isRootGranted: Boolean
    get() = Shell.cmd("su").exec().isSuccess && (isMagiskInstalled || isKSUInstalled)

val isMagiskInstalled: Boolean
    get() = Shell.cmd("magisk -v").exec().isSuccess

val isKSUInstalled: Boolean
    get() = Shell.cmd("/data/adb/ksud -h").exec().isSuccess

internal suspend fun Shell.Job.await(): Shell.Result {
    return suspendCoroutine { continuation ->
        submit {
            continuation.resume(it)
        }
    }
}

fun rebootDevice() {
    Shell.cmd("am start -a android.intent.action.REBOOT").exec()
}

internal class SuException(val stderrOut: String) : Exception(stderrOut)

@Throws(SuException::class)
internal suspend fun Shell.Job.awaitOutputOrThrow(): String {
    return suspendCoroutine { continuation ->
        submit {
            if (it.isSuccess) {
                continuation.resume(it.outString)
            } else {
                continuation.resumeWithException(SuException(it.errString))
            }
        }
    }
}