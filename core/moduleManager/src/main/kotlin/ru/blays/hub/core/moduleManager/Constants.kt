package ru.blays.hub.core.moduleManager

import org.intellij.lang.annotations.Language

internal const val MODULES_FOLDER_PATH = "/data/adb/modules"

internal const val MANAGER_MODULE_ID = "blays_hub"
internal const val MANAGER_MODULE_NAME = "BHub"
internal const val MODULE_AUTHOR = "Blays"

internal const val MODULE_FILE_PROP = "module.prop"
internal const val MODULE_FILE_SERVICE = "service.sh"
internal const val MODULE_FILE_MOUNT = "mount.sh"
internal const val MODULE_FILE_BASE = "base.apk"

@Language("bash")
internal const val MODULE_SCRIPT = """
#!/system/bin/sh
while [ "$(getprop sys.boot_completed)" != "1" ];
  do sleep 1;
done;

find $MODULES_FOLDER_PATH/$MANAGER_MODULE_ID/app/ -name '$MODULE_FILE_BASE' -exec sh -c 'dir=${'$'}(dirname ${'$'}0); sh "${'$'}dir/$MODULE_FILE_MOUNT"' {} \;
"""

@Language("properties")
internal const val MODULE_PROP = """
id=$MANAGER_MODULE_ID
name=$MANAGER_MODULE_NAME
version=1
versionCode=1
author=$MODULE_AUTHOR
description=BHub module.
"""