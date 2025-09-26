package ru.blays.preferences.api

/**
 * Preference value writer
 */
fun interface PreferenceWriter <T> {
    fun write(value: T): Boolean
}