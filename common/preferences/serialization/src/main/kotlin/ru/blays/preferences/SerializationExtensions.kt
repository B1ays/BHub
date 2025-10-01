package ru.blays.preferences

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import ru.blays.preferences.accessor.PreferenceAccessor
import ru.blays.preferences.accessor.TwoWayConverter
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.accessor.map
import ru.blays.preferences.accessor.withValue
import ru.blays.preferences.api.PreferenceValue
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.preferences.api.ReadWriteValue

/**
 * Create [PreferenceValue] with serialization [T] type support
 */
inline fun <reified T: Any> PreferencesHolder.getSerializableValue(
    key: String,
    defaultValue: T,
    serializer: KSerializer<T>
): PreferenceValue<T> {
    val defaultJson = DefaultJson.encodeToString(serializer, defaultValue)
    val value: PreferenceValue<String> = getValue(key, defaultJson)
    return value.map(JsonConverter(serializer))
}

/**
 * Create [PreferenceValue] with serialization [T] type support
 */
inline fun <reified T: Any> PreferencesHolder.getSerializableValue(
    accessor: PreferenceAccessor<T>,
): PreferenceValue<T> = getSerializableValue(
    key = accessor.key,
    defaultValue = accessor.defaultValue,
    serializer = DefaultJson.serializersModule.serializer()
)

/**
 * Create [PreferenceValue] with serialization [T] type support
 */
inline fun <reified T: Any> PreferencesHolder.getSerializableValue(
    key: String,
    serializer: KSerializer<T>
): PreferenceValue<T?> {
    val value: PreferenceValue<String> = getValue(key, "")
    return value.map(NullableJsonConverter(serializer))
}

/**
 * Executes actions with a [PreferenceValue] that supports serialization of type [T].
 * @param key The key for the preference.
 * @param defaultValue The default value if the preference is not set.
 * @param serializer The serializer for type [T].
 * @param block A function that takes a [ReadWriteValue] and returns a value.
 * @return The result of executing the [block].
 */
inline fun <reified T: Any, reified R> PreferencesHolder.withSerializableValue(
    key: String,
    defaultValue: T,
    serializer: KSerializer<T>,
    crossinline block: ReadWriteValue<T>.() -> R
): R {
    val defaultJson = DefaultJson.encodeToString(serializer, defaultValue)
    return withValue(key, defaultJson) {
        map(JsonConverter(serializer)).run(block)
    }
}

/**
 * A [TwoWayConverter] that converts an object of type [T] to its JSON string representation and vice-versa.
 *
 * @param T The type of the object to convert.
 * @property serializer The [KSerializer] to use for serialization and deserialization.
 */
@PublishedApi
internal class JsonConverter<T: Any>(
    private val serializer: KSerializer<T>
): TwoWayConverter<String, T> {
    override fun from(value: String): T {
        return DefaultJson.decodeFromString(serializer, value)
    }

    override fun to(value: T): String {
        return DefaultJson.encodeToString(serializer, value)
    }
}

/**
 * A [TwoWayConverter] that converts between a JSON string and a nullable object of type [T].
 * If the input string is empty or cannot be decoded, it returns null.
 * If the input object is null, it returns an empty string.
 *
 * @param T The type of the object to convert.
 * @property serializer The [KSerializer] to use for serialization and deserialization.
 */
@PublishedApi
internal class NullableJsonConverter<T: Any>(
    private val serializer: KSerializer<T>
): TwoWayConverter<String, T?> {
    override fun from(value: String): T? {
        if(value.isEmpty()) return null
        return try {
            DefaultJson.decodeFromString(serializer, value)
        } catch (_: Exception) {
            null
        }
    }

    override fun to(value: T?): String {
        if(value == null) return ""
        return try {
            DefaultJson.encodeToString(serializer, value)
        } catch (_: Exception) {
            ""
        }
    }
}