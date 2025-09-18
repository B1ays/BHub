package ru.blays.utils.android

import android.content.Context
import android.content.Intent
import java.util.Locale

/**
 * Свойство, возвращющее [Intent] для запуска основного Activity приложения
 */
val Context.launchSelfIntent: Intent?
    get() = packageManager.getLaunchIntentForPackage(packageName)

/**
 * Текущая локаль приложения
 */
inline val Context.locale: Locale
    get() = resources.configuration.locales[0]

/**
 * Код текущего языка приложения
 */
inline val Context.language: String
    get() = locale.language

/**
 * Авторизация для файлового провайдера
 */
inline val Context.providerAuthority: String
    get() = "$packageName.provider"

/**
 * Код версии приложения
 */
inline val Context.versionCode: Long
    get() = packageManager
        .getPackageInfo(packageName, 0)
        .versionCodeLong

/**
 * Имя версии приложения
 */
inline val Context.versionName: String
    get() = packageManager
        .getPackageInfo(packageName, 0)
        .versionName
        .orEmpty()