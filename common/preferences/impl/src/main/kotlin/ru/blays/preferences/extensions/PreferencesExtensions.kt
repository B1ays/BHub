package ru.blays.preferences.extensions

import ru.blays.preferences.api.PreferenceReader
import ru.blays.preferences.api.PreferenceWriter
import ru.blays.preferences.api.Preferences
import kotlin.reflect.KType

/**
 * Create a pair of [PreferenceReader] and [PreferenceWriter] for the given [key].
 * @param key The key to use for the preference.
 * @param defaultValue The default value to use if the preference is not found.
 * @return A pair of [PreferenceReader] and [PreferenceWriter].
 */
fun <T> Preferences.createReaderAndWriter(
    key: String,
    defaultValue: T,
    type: KType,
): Pair<PreferenceReader<T>, PreferenceWriter<T>> {
    return Pair(
        first = createReader(key, defaultValue, type),
        second = createWriter(key, type)
    )
}

/**
 * Creates a pair of reader and writer for the given [key].
 * @param key preference key
 * @param type preference type
 */
fun <T> Preferences.createNullableReaderAndWriter(
    key: String,
    type: KType,
): Pair<PreferenceReader<T?>, PreferenceWriter<T?>> {
    return Pair(
        first = createNullableReader(key, type),
        second = createNullableWriter(key, type)
    )
}