package ru.blays.utils.android

import android.content.pm.PackageInfo
import android.os.Build

/**
 * Получение versionCode в формате [Long] для всех версий android
 */
@get:Suppress("DEPRECATION")
val PackageInfo.versionCodeLong: Long
    get() = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        versionCode.toLong()
    }