package ru.blays.preferences.api

fun interface PreferenceReader <T> {
    /**
     * Reads value from preferences
     */
    fun read(): T
}