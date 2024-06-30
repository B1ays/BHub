package ru.blays.hub.core.logic.data

@JvmInline
value class LocalizationsMap(
    private val map: Map<String, String>
) {
    operator fun get(language: String): String? = map[language]

    fun getOrDefault(language: String): String = map.getOrDefault(
        language,
        map[DEFAULT_LOCALE_KEY] ?: DEFAULT_VALUE
    )

    companion object {
        private const val DEFAULT_VALUE = "No default locale"
        private const val DEFAULT_LOCALE_KEY = "default"
    }
}