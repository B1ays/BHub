package ru.blays.preferences.impl

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import ru.blays.preferences.InternalPreferencesApi
import ru.blays.preferences.api.PreferenceReader
import ru.blays.preferences.api.PreferenceValue
import ru.blays.preferences.api.PreferenceWriter
import kotlin.reflect.KProperty

/**
 * [PreferenceValue] Implementation
 */
class PreferenceValueImpl<T>(
    /**
     * [CoroutineScope] used for send [FlowCollector] and write to [SharedPreferences]
     */
    @InternalPreferencesApi
    override val scope: CoroutineScope,
    /**
     * Preference reader
     */
    private val reader: PreferenceReader<T>,
    /**
     * Preference writer
     */
    private val writer: PreferenceWriter<T>
): PreferenceValue<T> {
    /**
     * Set of [FlowCollector]s subscribed to this preference value.
     */
    private val collectors: MutableSet<FlowCollector<T>> = mutableSetOf()

    /**
     * Object used for thread-safe access to [value]
     */
    private val accessLock = Object()

    /**
     * Current value
     */
    override var value: T = reader.read()
        get() = synchronized(accessLock) { field }

    /**
     * values cache.
     * Size always 1
     */
    override val replayCache: List<T>
        get() = listOf(value)

    /**
     * Add [FlowCollector] and wait close
     */
    override suspend fun collect(collector: FlowCollector<T>) = coroutineScope {
        try {
            collectors.add(collector)
            collector.emit(value)
            awaitCancellation()
        } finally {
            collectors.remove(collector)
        }
    }

    /**
     * Thread safe value update
     */
    @OptIn(InternalPreferencesApi::class)
    override fun updateValue(newValue: T) {
        synchronized(accessLock) {
            if(writer.write(newValue)) {
                value = newValue
                scope.launch { collectors.forEach { it.emit(newValue) } }
            }
        }
    }

    /**
     * Read value as delegate
     */
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value

    /**
     * Write new value as delegate
     */
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) =
        updateValue(value)
}