package ru.blays.utils.kotlinx.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Копия из InternalCoroutinesApi. Собирает значения из [Flow] пока [predicate] не вернёт false
 * @param predicate условие продолжения
 */
suspend inline fun <T> Flow<T>.collectWhile(crossinline predicate: suspend (value: T) -> Boolean) {
    val collector = object : FlowCollector<T> {
        override suspend fun emit(value: T) {
            if (!predicate(value)) {
                throw AbortFlowException(this)
            }
        }
    }
    try {
        collect(collector)
    } catch (e: AbortFlowException) {
        e.checkOwnership(collector)
    }
}

/**
 * Запуск корутины на `Main` диспатчере
 */
suspend fun withMain(block: suspend CoroutineScope.() -> Unit) = withContext(Dispatchers.Main, block)

class AbortFlowException (
    @JvmField @Transient val owner: FlowCollector<*>
) : CancellationException("Flow was aborted, no more elements needed") {

    override fun fillInStackTrace(): Throwable {
        // Prevent Android <= 6.0 bug, #1866
        stackTrace = emptyArray()
        return this
    }

    fun checkOwnership(owner: FlowCollector<*>) {
        if (this.owner !== owner) throw this
    }
}