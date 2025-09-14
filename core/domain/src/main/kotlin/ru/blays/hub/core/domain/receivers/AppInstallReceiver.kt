package ru.blays.hub.core.domain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.blays.hub.core.packageManager.api.EXTRA_ACTION_SUCCESS
import ru.blays.hub.core.packageManager.api.EXTRA_STATUS_MESSAGE

fun packageManagerReceiver(
    onReceive: (
        action: String?,
        success: Boolean,
        message: String?,
    ) -> Unit
) = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val success = intent.getBooleanExtra(EXTRA_ACTION_SUCCESS, false)
        val message = intent.getStringExtra(EXTRA_STATUS_MESSAGE)
        onReceive(intent.action, success, message)
    }
}