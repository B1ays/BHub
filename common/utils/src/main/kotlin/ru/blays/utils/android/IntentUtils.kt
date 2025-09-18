package ru.blays.utils.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

/**
 * Создание [Intent] для открытия настроек.
 *
 * Флаги по умолчанию: [Intent.FLAG_ACTIVITY_NEW_TASK]
 * @param action один из [Settings]
 * @param packageName имя пакета приложения для которого нужно открыть страницу настроек.
 * Может не поддерживаться, необходимо контролировать этот момент
 */
fun createSettingsIntent(action: String, packageName: String): Intent {
    return createActionIntent(action).apply {
        data = getPackageUri(packageName)
    }
}

/**
 * Создание [Intent] для переданного [action]
 *
 * Флаги по умолчанию: [Intent.FLAG_ACTIVITY_NEW_TASK]
 */
fun createActionIntent(action: String): Intent {
    return Intent(action).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

/**
 * Создание [Intent] для открытия переданного [Uri]
 *
 * Флаги по умолчанию: [Intent.FLAG_ACTIVITY_NEW_TASK]
 */
fun createViewIntent(uri: Uri): Intent {
    return createActionIntent(Intent.ACTION_VIEW).apply {
        data = uri
    }
}

/**
 * Создание [Intent] для отправки файла
 *
 * Флаги по умолчанию: [Intent.FLAG_ACTIVITY_NEW_TASK], [Intent.FLAG_GRANT_READ_URI_PERMISSION]
 */
fun createSendIntent(context: Context, uri: Uri): Intent {
    val sendIntent = createActionIntent(Intent.ACTION_SEND).apply {
        setDataAndType(
            uri,
            context.contentResolver.getType(uri)
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    return Intent.createChooser(sendIntent, "Sharing a file...")
}

/**
 * Создание package uri для переданного [packageName]
 */
private fun getPackageUri(packageName: String): Uri {
    return "package:$packageName".toUri()
}