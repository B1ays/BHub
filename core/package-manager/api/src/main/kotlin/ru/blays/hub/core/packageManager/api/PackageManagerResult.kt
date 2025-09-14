package ru.blays.hub.core.packageManager.api

enum class PackageManagerError {
    SET_FAILED_INSTALLER,
    GET_FAILED_PACKAGE_DIR,
    GET_FAILED_PACKAGE_VERSION_NAME,
    GET_FAILED_PACKAGE_VERSION_CODE,

    APP_FAILED_FORCE_STOP,

    SESSION_FAILED_CREATE,
    SESSION_FAILED_COMMIT,
    SESSION_FAILED_WRITE,
    SESSION_FAILED_COPY,
    SESSION_FAILED_OPEN,
    SESSION_INVALID_ID,

    INSTALL_FAILED_ABORTED,
    INSTALL_FAILED_ALREADY_EXISTS,
    INSTALL_FAILED_CPU_ABI_INCOMPATIBLE,
    INSTALL_FAILED_INSUFFICIENT_STORAGE,
    INSTALL_FAILED_INVALID_APK,
    INSTALL_FAILED_VERSION_DOWNGRADE,
    INSTALL_FAILED_PARSE_NO_CERTIFICATES,
    INSTALL_FAILED_UNKNOWN,

    UNINSTALL_FAILED,

    LAUNCH_FAILED,

    LINK_FAILED_UNMOUNT,
    LINK_FAILED_MOUNT,

    PATCH_FAILED_COPY,
    PATCH_FAILED_CHMOD,
    PATCH_FAILED_CHOWN,
    PATCH_FAILED_CHCON,
    PATCH_FAILED_DESTROY,

    SCRIPT_FAILED_SETUP_POST_FS,
    SCRIPT_FAILED_SETUP_SERVICE_D,
    SCRIPT_FAILED_DESTROY_POST_FS,
    SCRIPT_FAILED_DESTROY_SERVICE_D,
}

fun getEnumForInstallFailed(outString: String): PackageManagerError {
    return when {
        outString.contains("INSTALL_FAILED_ABORTED") -> PackageManagerError.INSTALL_FAILED_ABORTED
        outString.contains("INSTALL_FAILED_ALREADY_EXISTS") -> PackageManagerError.INSTALL_FAILED_ALREADY_EXISTS
        outString.contains("INSTALL_FAILED_CPU_ABI_INCOMPATIBLE") -> PackageManagerError.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE
        outString.contains("INSTALL_FAILED_INSUFFICIENT_STORAGE") -> PackageManagerError.INSTALL_FAILED_INSUFFICIENT_STORAGE
        outString.contains("INSTALL_FAILED_INVALID_APK") -> PackageManagerError.INSTALL_FAILED_INVALID_APK
        outString.contains("INSTALL_FAILED_VERSION_DOWNGRADE") -> PackageManagerError.INSTALL_FAILED_VERSION_DOWNGRADE
        outString.contains("INSTALL_PARSE_FAILED_NO_CERTIFICATES") -> PackageManagerError.INSTALL_FAILED_PARSE_NO_CERTIFICATES
        else -> PackageManagerError.INSTALL_FAILED_UNKNOWN
    }
}

sealed class PackageManagerResult<out V> {
    data class Success<out V>(val value: V?) : PackageManagerResult<V>()
    data class Error(val error: PackageManagerError, val message: String) : PackageManagerResult<Nothing>()

    inline fun getOrElse(
        onError: (Error) -> @UnsafeVariance V?
    ): V? {
        return when (this) {
            is Success -> this.value
            is Error -> onError(this)
        }
    }

    fun getValueOrNull(): V? = getOrElse { null }

    val isError
        get() = this is Error

    val isSuccess
        get() = this is Success
}

