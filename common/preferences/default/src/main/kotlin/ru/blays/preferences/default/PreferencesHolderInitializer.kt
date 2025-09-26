package ru.blays.preferences.default

import android.content.Context
import android.content.SharedPreferences
import androidx.startup.Initializer
import ru.blays.preferences.android.AndroidPreferences
import ru.blays.preferences.api.PreferencesHolder
import ru.blays.preferences.impl.PreferencesHolderImpl

/**
 * Preferences holder initializer
 * @see AndroidPreferences
 * @see PreferencesHolderImpl
 */
internal class PreferencesHolderInitializer: Initializer<PreferencesHolder> {
    override fun create(context: Context): PreferencesHolder {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )

        val androidPreferences = AndroidPreferences(sharedPreferences)
        val preferencesHolder = PreferencesHolderImpl(androidPreferences)

        PreferenceHolderProvider.initialize(preferencesHolder)

        return preferencesHolder
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}