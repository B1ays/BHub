package ru.blays.hub.core.packageManager.utils

import android.content.IIntentReceiver
import android.content.IIntentSender
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.IBinder
import java.lang.reflect.InvocationTargetException

fun intentSender(onSend: (Intent) -> Unit): IntentSender {
    return object : IIntentSender.Stub() {
        override fun send(
            code: Int,
            intent: Intent,
            resolvedType: String?,
            finishedReceiver: IIntentReceiver?,
            requiredPermission: String?,
            options: Bundle?
        ): Int {
            onSend(intent)
            return 0
        }

        override fun send(
            code: Int,
            intent: Intent,
            resolvedType: String?,
            whitelistToken: IBinder?,
            finishedReceiver: IIntentReceiver?,
            requiredPermission: String?,
            options: Bundle?
        ) {
            onSend(intent)
        }
    }.createInstance()
}

@Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
fun IIntentSender.createInstance(): IntentSender {
    return IntentSender::class.java.getConstructor(IIntentSender::class.java).newInstance(this);
}