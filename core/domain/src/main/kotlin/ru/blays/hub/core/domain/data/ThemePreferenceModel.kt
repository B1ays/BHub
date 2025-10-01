package ru.blays.hub.core.domain.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * App theme preferences model
 */
@Serializable
data class ThemePreferenceModel(
    /**
     * Theme type
     */
    @SerialName("theme_type")
    val themeType: ThemeType,
    /**
     * Accent color type
     */
    @SerialName("color_accent_type")
    val colorAccentType: AccentType,
    /**
     * Amoled theme
     */
    @SerialName("amoled_theme")
    val amoledTheme: Boolean,
) {
    /**
     * Theme type
     */
    enum class ThemeType {
        /**
         * Follow system theme
         */
        SYSTEM,

        /**
         * Dark theme
         */
        DARK,

        /**
         * Light theme
         */
        LIGHT;
    }

    /**
     * Accent color type
     */
    @Serializable
    sealed class AccentType {
        /**
         * Dynamic md3 system color
         * @param fallbackIndex index of fallback color if system color is not available
         */
        @Serializable
        data class Dynamic(val fallbackIndex: Int) : AccentType()

        /**
         * Color from app defaults
         */
        @Serializable
        data class Preset(val index: Int) : AccentType()
    }
}

@OptIn(ExperimentalContracts::class)
fun ThemePreferenceModel.AccentType.isMonet(): Boolean {
    contract {
        returns(true) implies (this@isMonet is ThemePreferenceModel.AccentType.Dynamic)
    }
    return this is ThemePreferenceModel.AccentType.Dynamic
}

@OptIn(ExperimentalContracts::class)
fun ThemePreferenceModel.AccentType.isPreset(): Boolean {
    contract {
        returns(true) implies (this@isPreset is ThemePreferenceModel.AccentType.Preset)
    }
    return this is ThemePreferenceModel.AccentType.Preset
}