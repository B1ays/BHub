package ru.blays.preferences.accessor

import ru.blays.preferences.api.ReadWriteValue


/**
 * Updates the value of a [ReadWriteValue] by applying a transformation function.
 * This is an inline function that simplifies the process of reading the current value,
 * modifying it, and then writing it back.
 *
 * @param block A lambda function that takes the current value of type [T]
 * and returns the new value of type [T].
 *
 * @see ReadWriteValue.value
 * @see ReadWriteValue.updateValue
 */
inline fun <reified T> ReadWriteValue<T>.update(block: (T) -> T) {
    return updateValue(block(value))
}