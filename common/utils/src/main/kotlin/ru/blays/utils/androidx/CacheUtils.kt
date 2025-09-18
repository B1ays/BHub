package ru.blays.utils.androidx

import androidx.collection.LruCache

/**
 * Возвращает значение из кэша, если оно существует или вычисляет его и сохраняет в кэш.
 * @param key Ключ.
 * @param defaultValue Функция для вычисления значения, если оно отсутствует в кэше.
 * @return Значение из кэша или вычисленное значение.
 */
inline fun <K: Any, V: Any> LruCache<K, V>.getOrPut(
    key: K,
    defaultValue: () -> V
): V {
    val value = get(key)
    return if(value == null) {
        val answer = defaultValue()
        put(key, answer)
        answer
    } else {
        value
    }
}