package ru.blays.hub.core.domain.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

inline fun intent(block: Intent.() -> Unit): Intent {
    return Intent().apply(block)
}

inline val Context.launchSelfIntent: PendingIntent?
    get() {
        val launchIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }