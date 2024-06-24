package ru.blays.hub.core.moduleManager

import com.topjohnwu.superuser.Shell

internal fun setChmod(filePath: String, chmod: Int): Shell.Result {
    return exec("chmod $chmod $filePath")
}
internal fun setChown(filePath: String, chown: String): Shell.Result {
    return exec("chown $chown $filePath")
}
internal fun setChcon(filePath: String, chcon: String): Shell.Result {
    return exec("chcon $chcon $filePath")
}

internal fun deleteFile(filePath: String): Shell.Result {
    return exec("rm -rf $filePath")
}

internal fun createFolder(folderPath: String): Shell.Result {
    return exec("mkdir -p $folderPath")
}

internal fun checkFolderExist(folderPath: String): Shell.Result {
    return exec("test -d $folderPath")
}
internal fun checkFileExist(filePath: String): Shell.Result {
    return exec("test -f $filePath")
}

internal fun writeToPath(data: String, path: String): Boolean {
    val deleteResult = deleteFile(path)
    return if(deleteResult.isSuccess) {
        data.trim('\n')
            .lines()
            .fold(true) { acc, line ->
                if (!acc) return false
                exec("echo ${line.escapeAll()} >> $path").isSuccess
            }
    } else {
        false
    }
}

internal fun copyFile(from: String, to: String): Boolean {
    return exec("cp -f $from $to").isSuccess
}

internal fun getInstalledApkPath(packageName: String): String? {
    val pathToOrigAppResult =
        exec("pm path $packageName | cut -d \":\" -f2- | grep \"base.apk\"")
    if (!pathToOrigAppResult.isSuccess) return null
    return pathToOrigAppResult.out.firstOrNull()
}