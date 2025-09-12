package ru.blays.hub.core.logic.utils

import android.content.Context
import android.os.Environment
import ru.blays.hub.core.logic.APK_EXTENSION
import java.io.File

private var cachedDownloadsFolder: File? = null
internal val Context.downloadsFolder: File
    get() {
        return cachedDownloadsFolder
            ?: getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?.also { cachedDownloadsFolder = it }
            ?: throw IllegalStateException("Downloads folder not found")
    }

internal inline val File.isApkFile: Boolean
    get() = extension == APK_EXTENSION