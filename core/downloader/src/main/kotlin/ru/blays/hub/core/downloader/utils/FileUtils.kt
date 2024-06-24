package ru.blays.hub.core.downloader.utils

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

internal fun File.lengthNotZero(): Boolean = length().isNotZero

internal fun File.checkFileExists(): Boolean = exists() && lengthNotZero()

internal fun File.createChannel(mode: RWMode): FileChannel  {
    val randomAccessFile = RandomAccessFile(this, mode.code)
    return randomAccessFile.channel
}

internal fun File.moveTo(path: Path, vararg options: StandardCopyOption) {
    Files.move(toPath(), path, *options)
}
internal fun File.moveTo(file: File, vararg options: StandardCopyOption) {
    moveTo(file.toPath(), *options)
}

internal var FileChannel.position: Long
    get() = position()
    set(value) { position(value) }

internal enum class RWMode(val code: String) {
    READ_ONLY("r"),
    WRITE_ONLY("w"),
    READ_WRITE("rw");
}