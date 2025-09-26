package ru.blays.preferences.accessor

import ru.blays.preferences.api.PreferenceValue
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.preferences.api.ReadWriteValue

/**
 * Создание или получение [PreferenceValue] для переданного ключа.
 * @param accessor объект предоставляющий ключ и значение по умолчанию.
 * @return [PreferenceValue]
 */
inline fun <reified T: Any> PreferencesHolder.getValue(
    accessor: PreferenceAccessor<T>
): PreferenceValue<T> = getValue(
    key = accessor.key,
    defaultValue = accessor.defaultValue
)

/**
 * Использование [ReadWriteValue] без сохранения экземляра, если он уже не сохранён.
 * Полезна для еденичных операций чтения/записи параметра
 * @param accessor объект предоставляющий ключ и значение по умолчанию.
 * @param block функция, принимающая [ReadWriteValue] и возвращающая значение
 * @return результат выполнения [block]
 */
inline fun <reified T: Any, reified R> PreferencesHolder.withValue(
    accessor: PreferenceAccessor<T>,
    noinline block: ReadWriteValue<T>.() -> R
): R = withValue(
    key = accessor.key,
    defaultValue = accessor.defaultValue,
    block = block
)

/**
 * Чтение значения из [PreferenceValue]
 * @param accessor объект предоставляющий ключ и значение по умолчанию.
 * @return сохранённое значение, либо `defaultValue`, если его нет
 */
inline fun <reified T: Any> PreferencesHolder.readValue(
    accessor: PreferenceAccessor<T>
): T = withValue(accessor, ReadWriteValue<T>::value)

/**
 * Запись значения в [PreferenceValue]
 * @param accessor объект предоставляющий ключ и значение по умолчанию.
 * @param newValue новое значение
 */
inline fun <reified T: Any> PreferencesHolder.writeValue(
    accessor: PreferenceAccessor<T>,
    newValue: T,
): Unit = withValue(accessor) { updateValue(newValue) }