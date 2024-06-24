package ru.blays.hub.core.preferences

import android.os.Build
import androidx.datastore.core.Serializer
import ru.blays.hub.core.preferences.proto.DownloadMode
import ru.blays.hub.core.preferences.proto.Settings
import ru.blays.hub.core.preferences.proto.downloadModeSetting
import ru.blays.hub.core.preferences.proto.settings
import ru.blays.hub.core.preferences.proto.themeSettings
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer: Serializer<Settings> {
    override val defaultValue: Settings = defaultSettings
    override suspend fun readFrom(input: InputStream): Settings {
        return Settings.parseFrom(input)
    }
    override suspend fun writeTo(t: Settings, output: OutputStream) {
        t.writeTo(output)
    }
}

internal val defaultSettings: Settings
    get() = settings {
        checkUpdates = true
        themeSettings = themeSettings {
            monetColors = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            accentColorIndex = 1
        }
        downloadModeSetting = downloadModeSetting {
            mode = DownloadMode.MULTIPLE_TRIES
            triesNumber = 3
        }

        checkAppsUpdates = true
        checkAppsUpdatesInterval = 12
    }