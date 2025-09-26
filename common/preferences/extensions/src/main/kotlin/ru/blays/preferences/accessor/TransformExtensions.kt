package ru.blays.preferences.accessor

import ru.blays.preferences.api.PreferenceValue
import ru.blays.preferences.api.ReadWriteValue
import ru.blays.preferences.impl.PreferenceValueImpl
import ru.blays.preferences.impl.ReadWriteValueImpl

/**
 * Трансформация значения в [ReadWriteValue] из [T] в [R]
 * @param converter конвертер значений
 */
inline fun <reified T: Any, reified R: Any?> ReadWriteValue<T>.map(
    converter: TwoWayConverter<T, R>
): ReadWriteValue<R> {
    return ReadWriteValueImpl(
        reader = { converter.from(value) },
        writer = { newValue ->
            updateValue(converter.to(newValue))
            true
        }
    )
}

/**
 * Трансформация значения в [PreferenceValue] из [T] в [R]
 * @param converter конвертер значений
 */
inline fun <reified T: Any, reified R: Any?> PreferenceValue<T>.map(
    converter: TwoWayConverter<T, R>
): PreferenceValue<R> {
    return PreferenceValueImpl(
        scope = scope,
        reader = { converter.from(value) },
        writer = { newValue ->
            updateValue(converter.to(newValue))
            true
        }
    )
}

/**
 * Создание [TwoWayConverter]
 * @param from трансфортмация в новый тип
 * @param to трансформация в изначальный тип
 */
fun <T: Any, R: Any?> twoWayConverter(
    from: (value: T) -> R,
    to: (value: R) -> T
) = object : TwoWayConverter<T, R> {
    override fun from(value: T): R = from(value)
    override fun to(value: R): T = to(value)
}

/**
 * Конвертер значений в [PreferenceValue].
 * @param from трансфортмация в новый тип
 * @param to трансформация в изначальный тип
 */
interface TwoWayConverter <T: Any, R: Any?> {
    fun from(value: T): R

    fun to(value: R): T
}
