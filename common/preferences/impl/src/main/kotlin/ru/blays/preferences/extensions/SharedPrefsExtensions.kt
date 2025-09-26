@file:OptIn(ExperimentalContracts::class)

package ru.blays.preferences.extensions

import android.content.SharedPreferences
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Wrapper for [SharedPreferences.Editor] for use in Kotlin DSL style.
 *
 * This function allows you to edit SharedPreferences in a more concise and readable way
 * using a lambda with receiver.
 *
 * Example:
 * ```kotlin
 * prefs.edit {
 *     putString("key", "value")
 *     putInt("another_key", 123)
 * }
 * ```
 *
 * @param action A lambda with [SharedPreferences.Editor] as its receiver, where you can call
 *               editor functions like `putString`, `putInt`, etc.
 * @return `true` if the new values were successfully written to persistent storage.
 */
inline fun SharedPreferences.edit(
    action: SharedPreferences.Editor.() -> Unit
): Boolean {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }

    return edit().apply(action).commit()
}