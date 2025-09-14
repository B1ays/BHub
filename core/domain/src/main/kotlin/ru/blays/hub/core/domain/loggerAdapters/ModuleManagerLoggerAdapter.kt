package ru.blays.hub.core.domain.loggerAdapters

import ru.blays.hub.core.logger.Logger
import ru.blays.hub.core.moduleManager.LoggerAdapter

class ModuleManagerLoggerAdapter: LoggerAdapter {
    override val TAG: String = "ModuleManager"

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