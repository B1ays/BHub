package ru.blays.hub.core.packageManager.shizuku.utils

import android.os.IBinder
import android.os.IInterface
import rikka.shizuku.ShizukuBinderWrapper

// Taken from LSPatch (https://github.com/LSPosed/LSPatch)
internal fun IBinder.wrap() = ShizukuBinderWrapper(this)
internal fun IInterface.asShizukuBinder() = asBinder().wrap()