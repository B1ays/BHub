package ru.blays.hub.core.logic

const val ACTION_MODULE_INSTALL = "ru.blays.hub.core.moduleManager.ACTION_MODULE_INSTALL"

const val FLAG_REINSTALL = 1

internal const val APK_EXTENSION = "apk"

internal const val APPS_HREF = "/apps.json"
internal const val CATALOG_PROPS_HREF = "/catalog.prop"

internal const val VIBRATION_LENGTH = 300L

const val APK_MIME_TYPE = "application/vnd.android.package-archive"

internal const val UPDATES_SOURCE_URL_STABLE = "https://raw.githubusercontent.com/B1ays/BHub/main/update/update_stable.json"
internal const val UPDATES_SOURCE_URL_BETA = "https://raw.githubusercontent.com/B1ays/BHub/main/update/update_beta.json"
internal const val UPDATES_SOURCE_URL_NIGHTLY = "" // TODO replace with real url