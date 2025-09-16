package ru.blays.logger

import java.io.PrintWriter
import java.io.StringWriter

/**
 * Global app logger
 */
object Logger {
    // Log receivers
    private val _receivers: MutableSet<LogReceiver> = mutableSetOf()

    private var initialized = false

    /**
     * Check if logger can log
     */
    val canLog: Boolean
        get() = _receivers.isNotEmpty() && initialized

    /**
     * Global logger initialization
     */
    @LoggerDsl
    fun start(initializerScope: LoggerInitializerScope.() -> Unit) {
        if(initialized) {
            log(TAG, LogPriority.INFO) { "Logger already initialized" }
            return
        }
        val initializer = object: LoggerInitializerScope {
            override fun receivers(vararg receivers: LogReceiver) {
                receivers.forEach { addReceiver(it) }
            }
            override fun receivers(receivers: List<LogReceiver>) {
                receivers.forEach { addReceiver(it) }
            }
            override fun receivers(receiver: LogReceiver) {
                addReceiver(receiver)
            }
        }
        initializer.apply(initializerScope)
        initialized = true
    }

    /**
     * Add new receiver
     */
    fun addReceiver(receiver: LogReceiver) {
        if (_receivers.add(receiver).not()) {
            log(TAG, LogPriority.INFO) { "Receiver $receiver already added" }
        }
    }

    /**
     * Remove receiver
     */
    fun removeReceiver(receiver: LogReceiver): Boolean {
        return _receivers.remove(receiver)
    }

    /**
     * Get all added receivers
     */
    fun getAllReceivers(): Set<LogReceiver> = _receivers

    /**
     * Internal log send implementation. Must not be use directly
     * @param tag log tag
     * @param priority log priority
     * @param message log text
     */
    @PublishedApi
    internal fun logInternal(
        tag: String,
        priority: LogPriority,
        message: String,
        error: Throwable? = null
    ) {
        for (receiver in _receivers) {
            if (!receiver.canReceive(priority)) continue
            receiver.log(LogValueImpl(priority, tag, message, error))
        }
    }

    private val TAG = logTag("InternalLogger")
}

interface LoggerInitializerScope {
    fun receivers(vararg receivers: LogReceiver)
    fun receivers(receivers: List<LogReceiver>)
    fun receivers(receiver: LogReceiver)
}

/**
 * Public log send implementation
 * @param tag log tag
 * @param priority log priority
 * @param message log message
 */
inline fun log(
    tag: String,
    priority: LogPriority = LogPriority.DEBUG,
    error: Throwable? = null,
    message: () -> String,
) {
    if (Logger.canLog) {
        Logger.logInternal(
            tag = tag,
            priority = priority,
            message = message(),
            error = error
        )
    }
}

/**
 * Public log send implementation. Used class name from call scope as log tag
 * @param priority log priority
 * @param message log message
 */
inline fun Any.log(
    priority: LogPriority = LogPriority.DEBUG,
    error: Throwable? = null,
    message: () -> String,
) {
    val tag = when (val simpleName = this::class.simpleName) {
        null -> {
            val javaClass = this::class.java
            val fullClassName = javaClass.name
            val outerClassName = fullClassName.substringBefore('$')
            val simplerOuterClassName = outerClassName.substringAfterLast('.')
            if (simplerOuterClassName.isEmpty()) {
                fullClassName
            } else {
                simplerOuterClassName.removeSuffix("Kt")
            }
        }
        else -> simpleName
    }
    if (Logger.canLog) {
        Logger.logInternal(
            tag = tag,
            priority = priority,
            message = message(),
            error = error
        )
    }
}

/**
 * Convert [Throwable] to string
 */
fun Throwable.asLog(): String {
    val stringWriter = StringWriter(256)
    val printWriter = PrintWriter(stringWriter, false)
    printStackTrace(printWriter)
    printWriter.flush()
    return stringWriter.toString()
}

/**
 * Generate log tag from parts
 */
fun logTag(vararg parts: String): String {
    return parts.fold("JWizard") { acc, part -> "$acc:$part" }
}