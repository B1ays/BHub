package ru.blays.hub.core.packageManager.api

const val ACTION_APP_INSTALL = "ACTION_APP_INSTALL"
const val ACTION_APP_UNINSTALL = "ACTION_APP_UNINSTALL"

const val EXTRA_ACTION_SUCCESS = "EXTRA_INSTALL_SUCCESS"
const val EXTRA_STATUS_MESSAGE = "EXTRA_INSTALL_STATUS_MESSAGE"

enum class PackageManagerType {
    NonRoot,
    Root,
    Shizuku
}