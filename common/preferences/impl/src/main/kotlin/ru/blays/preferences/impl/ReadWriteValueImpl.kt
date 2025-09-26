package ru.blays.preferences.impl

import ru.blays.preferences.api.PreferenceReader
import ru.blays.preferences.api.PreferenceWriter
import ru.blays.preferences.api.ReadWriteValue

/**
 * Implementation of [ReadWriteValue].
 * Designed for simple read/write operations on a parameter.
 *
 * @param T The type of the value.
 * @property reader The [PreferenceReader] to read the parameter.
 * @property writer The [PreferenceWriter] to write the parameter.
 */
class ReadWriteValueImpl<T>(
    /**
     * Preference reader
     */
    private val reader: PreferenceReader<T>,
    /**
     * preference writer
     */
    private val writer: PreferenceWriter<T>
): ReadWriteValue<T> {
    private val lock = Object()

    override var value: T = reader.read()
        get() = synchronized(lock) { field }

    /**
     * Thread safe value update
     */
    override fun updateValue(newValue: T) {
        synchronized(lock) {
            if(writer.write(newValue)) {
                value = newValue
            }
        }
    }
}