package ru.blays.logger

/**
 * Log priority
 */
enum class LogPriority(val value: Int) {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    WARN(4),
    ERROR(5),
    WTF(6);

    companion object {
        fun getByValue(value: Int): LogPriority = entries.first { it.value == value }
    }
}