package ru.blays.hub.core.deviceUtils

import android.os.Build
import com.topjohnwu.superuser.Shell

object DeviceInfo {
    val sdkVersion: Int
        get() = Build.VERSION.SDK_INT

    val supportedABIs: Array<String>
        get() = Build.SUPPORTED_ABIS

    val name: String
        get() = "${Build.BRAND} ${Build.MODEL}"

    val isRootGranted: Boolean
        get() = Shell.cmd("su").exec().isSuccess

    val isMagiskInstalled: Boolean
        get() = Shell.cmd("magisk -v").exec().isSuccess

    val isKSUInstalled: Boolean
        get() = Shell.cmd("/data/adb/ksud -h").exec().isSuccess

}