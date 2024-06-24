package ru.blays.hub.utils.coilDsl

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File

fun imageLoader(
    context: Context,
    block: ImageLoader.Builder.() -> Unit
) = ImageLoader.Builder(context).apply(block).build()

fun ImageLoader.Builder.memoryCache(
    context: Context,
    block: MemoryCache.Builder.() -> Unit
) = memoryCache(
    MemoryCache.Builder(context).apply(block).build()
)

fun ImageLoader.Builder.diskCache(
    folder: File,
    block: DiskCache.Builder.() -> Unit = {}
) = diskCache(
    DiskCache.Builder().apply {
        directory(folder)
        block()
    }.build()
)

var ImageLoader.Builder.allowHardware: Boolean
    get() = throw UnsupportedOperationException("Write only property")
    set(value) {
        allowHardware(value)
    }

var ImageLoader.Builder.allowRgb565: Boolean
    get() = throw UnsupportedOperationException("Write only property")
    set(value) {
        allowRgb565(value)

    }

var ImageLoader.Builder.crossfade: Boolean
    get() = throw UnsupportedOperationException("Write only property")
    set(value) {
        crossfade(value)
    }