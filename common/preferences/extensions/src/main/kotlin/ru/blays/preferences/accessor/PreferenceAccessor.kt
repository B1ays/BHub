package ru.blays.preferences.accessor

/**
 * An interface representing an accessor for a preference.
 * It encapsulates the key and default value for a specific preference.
 *
 * @param T The type of the preference value.
 */
interface PreferenceAccessor<out T: Any> {
    val key: String
    val defaultValue: T
}

/**
 * [PreferenceAccessor] implementation
 *
 * @param key preference key
 * @param defaultValue default value
 * @param T preference type
 */
private data class PreferenceAccessorImpl<out T: Any>(
    override val key: String,
    override val defaultValue: T
): PreferenceAccessor<T>

fun <T: Any> preferenceAccessor(
    key: String,
    defaultValue: T
): PreferenceAccessor<T> {
    return PreferenceAccessorImpl(
        key = key,
        defaultValue = defaultValue
    )
}