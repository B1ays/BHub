package ru.blays.preferences.api

import kotlin.reflect.KType

/**
 * Interface representing a holder for preferences.
 * Provides methods to get and manage preference values.
 */
interface PreferencesHolder {
    /**
     * Get or create new preference value for key
     * @param key preference key
     * @param defaultValue default value
     * @return [PreferenceValue]
     */
    fun <T: Any> getValue(key: String, defaultValue: T, type: KType): PreferenceValue<T>

    /**
     * Get or create new nullable preference value for key
     * @param key preference key
     * @return [PreferenceValue]
     */
    fun <T> getValueNullable(key: String, type: KType): PreferenceValue<T?>

    /**
     * Do action with new or holded value. Not hold created values
     * @param key preference value
     * @param defaultValue default value
     * @param block action with value
     * @return [block] result
     */
    fun <T: Any, R> withValue(key: String, defaultValue : T, type: KType, block: ReadWriteValue<T>.() -> R): R
}