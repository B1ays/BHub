package ru.blays.utils.android

import android.content.ComponentName
import android.content.Context

/**
 * Создание [ComponentName] с типом [T]
 */
inline fun <reified T> Context.componentName(): ComponentName {
    return ComponentName(this, T::class.java)
}
