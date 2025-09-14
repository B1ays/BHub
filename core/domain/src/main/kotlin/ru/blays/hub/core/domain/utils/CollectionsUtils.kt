package ru.blays.hub.core.domain.utils

import kotlinx.collections.immutable.toPersistentList

internal fun List<Int>.toIntArray() = IntArray(size, ::get)
internal fun List<Long>.toLongArray() = LongArray(size, ::get)
internal fun List<Float>.toFloatArray() = FloatArray(size, ::get)
internal fun List<Double>.toDoubleArray() = DoubleArray(size, ::get)

internal inline fun <reified T> List<T>.mutate(block: MutableList<T>.() -> Unit): List<T> {
    return toMutableList().apply(block).toPersistentList()
}

internal fun <T> MutableList<T>.replace(old: T, new: T): Boolean {
    val index = indexOf(old)
    if (index < 0) return false
    set(index, new)
    return true
}