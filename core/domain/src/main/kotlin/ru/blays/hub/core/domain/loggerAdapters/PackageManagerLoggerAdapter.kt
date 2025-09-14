package ru.blays.hub.core.domain.loggerAdapters

import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.packageManager.api.LoggerAdapter

class PackageManagerLoggerAdapter: LoggerAdapter {
    override val TAG: String = "PackageManager"

    override fun d(message: String) {
        Logger.d(TAG, message)
    }
    override fun i(message: String) {
        Logger.i(TAG, message)
    }
    override fun w(message: String) {
        Logger.w(TAG, message)
    }
    override fun e(message: String) {
        Logger.e(TAG, message)
    }
    override fun e(throwable: Throwable) {
        Logger.e(TAG, throwable)
    }
}