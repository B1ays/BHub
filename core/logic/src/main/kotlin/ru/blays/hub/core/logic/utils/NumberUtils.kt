@file:Suppress("NOTHING_TO_INLINE")

package ru.blays.hub.core.logic.utils

inline fun String.toIntOrDefault(defaultValue: Int) = toIntOrNull() ?: defaultValue
inline fun String.toIntOrElse(block: () -> Int) = toIntOrNull() ?: block()