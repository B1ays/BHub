package ru.blays.logger

/**
 * Logs receiver
 */
interface LogReceiver {
    /**
     * Check if the receiver is accepted with this priority
     * @param priority log priority
     * @return true if receiver can receive this priority
     */
    fun canReceive(priority: LogPriority): Boolean = true

    /**
     * Send log
     */
    fun log(logValue: LogValue)
}