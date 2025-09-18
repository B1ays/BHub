package ru.blays.utils.android

import android.content.Context

/**
 * Получение системного сервиса или ошибки, если его нет
 * @throws IllegalStateException если сервис не найден
 */
inline fun <reified T: Any> Context.getSystemServiceOrThrow(): T {
    val service = getSystemService(T::class.java)
    return service
        ?: throw ServiceNotFoundException("Service ${T::class.simpleName} not found")
}

class ServiceNotFoundException(message: String): IllegalStateException(message)