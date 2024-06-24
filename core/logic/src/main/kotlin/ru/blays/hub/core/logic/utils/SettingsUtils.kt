package ru.blays.hub.core.logic.utils

import android.content.Context
import android.widget.Toast
import ru.blays.hub.core.deviceUtils.DeviceInfo
import ru.blays.hub.core.logic.R
import ru.blays.hub.core.preferences.proto.PMType
import ru.blays.hub.core.preferences.SettingsRepository

context(Context)
internal fun SettingsRepository.validateSettings() {
    SettingsValidator.validateSettings(this, this@Context)
}

internal fun SettingsRepository.validateSettings(context: Context)  {
    SettingsValidator.validateSettings(this, context)
}

private data object SettingsValidator {
    private var rootGranted: Boolean? = null

    fun validateSettings(repository: SettingsRepository, context: Context) {
        if(rootGranted == null) {
            rootGranted = DeviceInfo.isRootGranted
        }
        if(repository.rootMode) {
            if(rootGranted == false) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_root_not_granted),
                    Toast.LENGTH_LONG,
                ).show()
                repository.setValue {
                    pmType = PMType.NON_ROOT
                    rootMode = false
                }
            }
        } else {
            if(repository.pmType == PMType.ROOT) {
                repository.setValue {
                    pmType = PMType.NON_ROOT
                }
            }
        }
    }
}