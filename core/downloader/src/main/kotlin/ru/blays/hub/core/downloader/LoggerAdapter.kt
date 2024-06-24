package ru.blays.hub.core.downloader

interface LoggerAdapter {
    val TAG: String
    fun d(message: String)
    fun i(message: String)
    fun w(message: String)
    fun e(message: String)
    fun e(throwable: Throwable)
}