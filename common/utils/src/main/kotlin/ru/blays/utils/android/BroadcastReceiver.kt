package ru.blays.utils.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Создание [BroadcastReceiver].
 * @param onReceive колбэк вызываемый при получении бродкаста.
 */
inline fun broadcastReceiver(
    crossinline onReceive: (context: Context, intent: Intent) -> Unit
): BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = onReceive(context, intent)
}