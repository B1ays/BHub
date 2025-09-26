package ru.blays.preferences.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.blays.preferences.api.PreferenceValue
import ru.blays.preferences.api.Preferences
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.preferences.api.ReadWriteValue
import ru.blays.preferences.extensions.createNullableReaderAndWriter
import ru.blays.preferences.extensions.createReaderAndWriter
import kotlin.reflect.KType

/**
 * Implementation of [PreferencesHolder].
 * @param preferences [Preferences] for storing values.
 * @param scope The coroutine scope used for creating preference values.
 */
class PreferencesHolderImpl(
    private val preferences: Preferences,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : PreferencesHolder {
    private val heldValues: MutableMap<String, PreferenceValue<*>> = mutableMapOf()

    /**
     * Creates or retrieves a [PreferenceValue] for the given key.
     * @param key The key for the preference.
     * @param defaultValue The default value for the preference if it doesn't exist.
     * @return [PreferenceValue] for the given key.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> getValue(
        key: String,
        defaultValue: T,
        type: KType
    ): PreferenceValue<T> {
        return heldValues.getOrPut(key) {
            val (reader, writer) = preferences.createReaderAndWriter(
                key = key,
                defaultValue = defaultValue
            )
            PreferenceValueImpl(
                scope = scope,
                reader = reader,
                writer = writer,
            )
        } as PreferenceValue<T>
    }

    /**
     * Creates or retrieves a [PreferenceValue] for the given key.
     * Unlike [getValue], this function returns a [PreferenceValue] that can store null values.
     * @param key The key for the preference.
     * @param type type of preference value
     * @return [PreferenceValue] for the given key.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getValueNullable(key: String, type: KType): PreferenceValue<T?> {
        return heldValues.getOrPut(key) {
            val (reader, writer) =
                preferences.createNullableReaderAndWriter<T>(key = key, type = type)

            PreferenceValueImpl(
                scope = scope,
                reader = reader,
                writer = writer,
            )
        } as PreferenceValue<T?>
    }

    /**
     * Executes a block of code with a [ReadWriteValue] instance.
     *
     * This function allows you to use a [ReadWriteValue] without storing its instance,
     * which is useful for single read/write operations. If a [PreferenceValue] for the given
     * key already exists, it will be used. Otherwise, a temporary [ReadWriteValue]
     * will be created.
     *
     * @param T The type of the preference value.
     * @param R The return type of the [block].
     * @param key The key of the preference.
     * @param defaultValue The default value to use if the preference is not found.
     * @param block A lambda function that takes a [ReadWriteValue] as its receiver and returns a value of type [R].
     * @return The result of executing the [block].
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T: Any, R> withValue(
        key: String,
        defaultValue: T,
        type: KType,
        block: ReadWriteValue<T>.() -> R
    ): R {
        val heldValue = heldValues[key] as? PreferenceValue<T>
        return if(heldValue != null) {
            block(heldValue)
        } else {
            val (reader, writer) = preferences.createReaderAndWriter(
                key = key,
                defaultValue = defaultValue
            )
            ReadWriteValueImpl(reader, writer).run(block)
        }
    }
}