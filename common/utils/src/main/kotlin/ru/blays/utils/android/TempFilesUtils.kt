package ru.blays.utils.android

import android.content.Context
import java.nio.file.Path

/**
 * Создание временного файла
 * @param fileName имя файла
 * @return путь к файлу
 */
fun Context.createTempFile(fileName: String): Path {
    val cachePath = cacheDir.toPath()
    return cachePath.resolve(fileName)
}