package ru.blays.hub.core.domain.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext


// Internal building block for non-tailcalling flow-truncating operators
internal suspend inline fun <T> Flow<T>.collectWhile(crossinline predicate: suspend (value: T) -> Boolean) {
    val collector = object : FlowCollector<T> {
        override suspend fun emit(value: T) {
            // Note: we are checking predicate first, then throw. If the predicate does suspend (calls emit, for example)
            // the the resulting code is never tail-suspending and produces a state-machine
            if (!predicate(value)) {
                throw AbortFlowException(this)
            }
        }
    }
    try {
        collect(collector)
    } catch (e: AbortFlowException) {
        if(e.owner !== collector) throw e
    }
}

inline fun <T, M> StateFlow<T>.map(
    coroutineScope : CoroutineScope,
    crossinline mapper : (value : T) -> M
): StateFlow<M> = map(mapper).stateIn(
    coroutineScope,
    SharingStarted.Eagerly,
    mapper(value)
)

context(scope: CoroutineScope)
inline fun <T, M> StateFlow<T>.map(
    crossinline mapper : (value : T) -> M
): StateFlow<M> = map(mapper).stateIn(
    scope,
    SharingStarted.Eagerly,
    mapper(value)
)

suspend fun <R> runOnUiThread(block: suspend CoroutineScope.() -> R): R {
    return withContext(Dispatchers.Main, block)
}

internal data class AbortFlowException(
    @JvmField @Transient val owner: Any
) : CancellationException("Flow was aborted, no more elements needed")