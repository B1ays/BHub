package ru.blays.hub.core.domain.data

import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.hub.core.preferences.proto.DownloadMode
import ru.blays.hub.core.preferences.proto.PMType

val PMType.realType: PackageManagerType
    get() = when(this) {
        PMType.NON_ROOT -> PackageManagerType.NonRoot
        PMType.ROOT -> PackageManagerType.Root
        PMType.SHIZUKU -> PackageManagerType.Shizuku
        PMType.UNRECOGNIZED -> throw IllegalStateException("Unrecognized package manager type")
    }

fun DownloadMode.toDownloaderType(triesNumber: Int): ru.blays.hub.core.downloader.DownloadMode = when(this) {
    DownloadMode.SINGLE_TRY -> ru.blays.hub.core.downloader.DownloadMode.SingleTry
    DownloadMode.MULTIPLE_TRIES -> ru.blays.hub.core.downloader.DownloadMode.MultipleTry(triesNumber)
    DownloadMode.INFINITY_TRIES -> ru.blays.hub.core.downloader.DownloadMode.InfinityTry
    DownloadMode.UNRECOGNIZED -> throw IllegalStateException("Unrecognized download mode")
}