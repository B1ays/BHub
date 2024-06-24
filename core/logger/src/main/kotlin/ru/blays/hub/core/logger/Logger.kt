package ru.blays.hub.core.logger

import android.content.Context
import android.util.Log
import android.util.Log.e
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

object Logger: KoinComponent {
    private val context: Context by inject()
    private val logFile by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        File(context.filesDir, "Manager.log")
    }

    private val _logs: MutableList<String> = object : ArrayList<String>() {
        override fun add(element: String): Boolean {
            return super.add(element) && appendToFile(element)
        }
    }

    val logs: List<String>
        get() = _logs

    val file: File
        get() = logFile

    fun <T: Any> mapLogs(transform: (String) -> T): List<T> {
        return _logs.map(transform)
    }

    override fun toString(): String {
        return _logs.joinToString("\n")
    }

    fun d(tag: String?, message: String) {
        _logs += "${defaultFormatter.currentTime} | Debug | $tag - $message"
        if(BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String?, message: String) {
        Log.e(tag, message)
        _logs += "${defaultFormatter.currentTime} | Error | $tag - $message"
    }
    fun e(tag: String?, throwable: Throwable) {
        e(tag, null, throwable)
        _logs += "${defaultFormatter.currentTime} | Error | $tag - ${throwable.localizedMessage}"
    }
    fun e(tag: String?, message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
        _logs += "${defaultFormatter.currentTime} | Error | $tag - $message\n${throwable.localizedMessage}"
    }

    fun i(tag: String?, message: String) {
        Log.i(tag, message)
        _logs += "${defaultFormatter.currentTime} | Info | $tag - $message"
    }

    fun v(tag: String?, message: String) {
        Log.v(tag, message)
        _logs += "${defaultFormatter.currentTime} | Verbose | $tag - $message"
    }

    fun w(tag: String?, message: String) {
        Log.w(tag, message)
        _logs += "${defaultFormatter.currentTime} | Warning | $tag - $message"
    }
    fun w(tag: String?, throwable: Throwable) {
        Log.w(tag, throwable)
        _logs += "${defaultFormatter.currentTime} | Debug | $tag - ${throwable.localizedMessage}"
    }
    fun w(tag: String?, message: String, throwable: Throwable) {
        Log.w(tag, message, throwable)
        _logs += "${defaultFormatter.currentTime} | Debug | $tag - $message\n${throwable.localizedMessage}"
    }

    private fun appendToFile(log: String) = kotlin.runCatching {
        logFile.appendText("$log\n")
    }.isSuccess

    private fun clearFile() {
        logFile.writeText("")
    }

    init {
        clearFile()
    }
}