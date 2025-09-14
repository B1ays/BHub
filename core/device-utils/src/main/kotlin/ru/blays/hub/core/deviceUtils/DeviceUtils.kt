package ru.blays.hub.core.deviceUtils

import com.topjohnwu.superuser.Shell

object DeviceUtils {
    fun rebootDevice(): Boolean {
        if(!DeviceInfo.isRootGranted) return false
        return Shell.cmd("am start -a android.intent.action.REBOOT").exec().isSuccess
    }
}