package ru.blays.hub.core.moduleManager

import com.topjohnwu.superuser.Shell

internal fun exec(command: String): Shell.Result {
    return Shell.cmd(command).exec()
}

internal infix fun Shell.Result.IF_SUCCESS(block: (result: Shell.Result) -> Shell.Result): Shell.Result {
    return if (isSuccess) block(this) else this
}