@file:OptIn(ExperimentalContracts::class)

package ru.blays.utils.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Преобразует [List] в [MutableList] и выполняет над нам переданное действие
 * @param mutator действие над [MutableList]
 * @return новый [List]
 */
inline fun <reified T> List<T>.mutate(
    mutator: MutableList<T>.() -> Unit
): List<T> {
    contract {
        callsInPlace(mutator, InvocationKind.EXACTLY_ONCE)
    }
    return toMutableList().apply(mutator)
}

/**
 * Преобразует [Map] в [MutableMap] и выполняет над нам переданное действие
 * @param mutator действие над [MutableMap]
 * @return новая [Map]
 */
inline fun <reified K, reified V> Map<K, V>.mutate(
    mutator: MutableMap<K, V>.() -> Unit
): Map<K, V> {
    contract {
        callsInPlace(mutator, InvocationKind.EXACTLY_ONCE)
    }
    return toMutableMap().apply(mutator)
}

/**
 * Замена айтема в коллекции по индексу
 * @param index индекс айтема
 * @param onReplace принимает старое значение и возвращает новое
 */
inline fun <reified T> MutableList<T>.replaceAt(index: Int, onReplace: (T) -> T) {
    contract {
        callsInPlace(onReplace, InvocationKind.AT_MOST_ONCE)
    }
    if(index !in indices) return
    this[index] = onReplace(this[index])
}