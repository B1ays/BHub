package ru.blays.preferences.api

import kotlin.reflect.KType

/**
 * Abstract interface for reading and writing preferences.
 */
interface Preferences {
    /**
     * Create preference reader
     * @param key preference key
     * @param defaultValue default value
     */
    fun <T> createReader(key: String, defaultValue: T, type: KType,): PreferenceReader<T>

    /**
     * Create preference reader
     * @param key preference key
     */
    fun <T> createNullableReader(key: String, type: KType): PreferenceReader<T?>

    /**
     * Create preference writer
     * @param key preference key
     */
    fun <T> createWriter(key: String, type: KType): PreferenceWriter<T>

    /**
     * Create nullable preference writer
     * @param key preference key
     */
    fun <T> createNullableWriter(key: String, type: KType): PreferenceWriter<T?>

    /**
     * Check if preference contains value
     * @param key preference key
     */
    fun containsKey(key: String): Boolean

    /**
     * Delete value associated with key
     * @param key preference key
     * @return true if deleted else false
     */
    fun removeKey(key: String): Boolean
}