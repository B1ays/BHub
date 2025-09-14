package ru.blays.hub.core.moduleManager

import android.util.Log

interface LoggerAdapter {
    val TAG: String

    fun d(message: String)

    fun i(message: String)

    fun w(message: String)

    fun e(message: String)
    fun e(throwable: Throwable)
}

internal class DefaultLoggerAdapter: LoggerAdapter {
    override val TAG: String = "ModuleManager"

    override fun d(message: String) {
        Log.d(TAG, message)
    }

    override fun i(message: String) {
        Log.i(TAG, message)
    }

    override fun w(message: String) {
        Log.w(TAG, message)
    }

    override fun e(message: String) {
        Log.e(TAG, message)
    }

    override fun e(throwable: Throwable) {
        Log.e(TAG, "", throwable)
    }
}