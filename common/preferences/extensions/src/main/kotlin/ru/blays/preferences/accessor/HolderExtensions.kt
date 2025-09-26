package ru.blays.preferences.accessor

import ru.blays.preferences.api.PreferenceValue
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.preferences.api.ReadWriteValue
import kotlin.reflect.typeOf

inline fun <reified T: Any> PreferencesHolder.getValue(key: String, defaultValue: T): PreferenceValue<T> =
    getValue(key, defaultValue, typeOf<T>())

inline fun <reified T> PreferencesHolder.getValueNullable(key: String): PreferenceValue<T?> =
    getValueNullable(key, typeOf<T>())

inline fun <reified T: Any, R> PreferencesHolder.withValue(
    key: String,
    defaultValue : T,
    noinline block: ReadWriteValue<T>.() -> R
): R = withValue(key, defaultValue, typeOf<T>(), block)