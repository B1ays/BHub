package ru.blays.preferences.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadWriteProperty

interface ReadWriteValue <T> {
    /**
     * Current value
     */
    val value: T
    /**
     * Update value
     */
    fun updateValue(newValue: T)
}

/**
 * Observable preference value. Implements [StateFlow]
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface ObservableValue <T> : StateFlow<T> {
    val scope: CoroutineScope
}

/**
 * Preference value
 *
 * Usage as property:
 * ```
 * val value = preferenceValue.value
 * preferenceValue.updateValue(newValue)
 * ```
 * Usage as delegate:
 * ```
 * var delegatedValue by preferenceValue
 * ```
 * Usage as [StateFlow]:
 * ```
 * preferenceValue.collect { newValue ->
 *     // do something
 * }
 */
interface PreferenceValue <T> : ReadWriteValue<T>, ObservableValue<T>, ReadWriteProperty<Any?, T>