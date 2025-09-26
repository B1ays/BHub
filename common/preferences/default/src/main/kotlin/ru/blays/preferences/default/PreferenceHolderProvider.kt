package ru.blays.preferences.default

import ru.blays.preferences.api.PreferencesHolder

/**
 * Default preferences holder provider
 */
data object PreferenceHolderProvider {
    internal lateinit var preferencesHolder: PreferencesHolder

    /**
     * Provider initialization
     */
    internal fun initialize(preferencesHolder: PreferencesHolder) {
        this.preferencesHolder = preferencesHolder
    }

    /**
     * Get preferences holder
     */
    fun getInstance(): PreferencesHolder = preferencesHolder
}