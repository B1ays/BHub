package ru.blays.hub.core.domain

import android.content.Context
import android.widget.Toast
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.packageManager.api.PackageManagerType
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder

class PreferencesValidator(
    preferencesHolder: PreferencesHolder,
    private val context: Context,
) {
    private var rootModeValue by preferencesHolder.getValue(RootModeAccessor)
    private var packageManagerValue by preferencesHolder.getValue(PackageManagerAccessor)

    fun validate() {
        val rootGranted = DeviceInfo.isRootGranted

        when {
            rootModeValue && !rootGranted -> {
                if(packageManagerValue == PackageManagerType.Root) {
                    packageManagerValue = PackageManagerType.NonRoot
                }
                rootModeValue = false
                Toast.makeText(
                    context,
                    context.getString(R.string.error_root_not_granted),
                    Toast.LENGTH_LONG,
                ).show()
            }
            !rootModeValue -> {
                if(packageManagerValue == PackageManagerType.Root) {
                    packageManagerValue = PackageManagerType.NonRoot
                }
            }
        }
    }
}