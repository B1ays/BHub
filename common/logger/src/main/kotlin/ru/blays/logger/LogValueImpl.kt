package ru.blays.logger

/**
 * [LogValue] imoplementation
 */
internal data class LogValueImpl(
    override val priority: LogPriority,
    override val tag: String,
    override val message: String,
    override val throwable: Throwable?,
): LogValue {
    constructor(
        priority: LogPriority,
        tag: String,
        message: String
    ): this(
        priority = priority,
        tag = tag,
        message = message,
        throwable = null
    )
}