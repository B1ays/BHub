package ru.blays.logger

/**
 * Log message sent to receiver
 */
interface LogValue {
    val priority: LogPriority

    val tag: String

    val message: String

    val throwable: Throwable?
}