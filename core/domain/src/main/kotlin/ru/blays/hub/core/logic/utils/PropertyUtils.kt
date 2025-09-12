package ru.blays.hub.core.logic.utils

import java.io.InputStream
import java.io.Reader
import java.util.Properties

@JvmInline
value class PropertyParser private constructor(private val property: Properties) {
    operator fun get(key: String): String? {
        return property.getProperty(key)
    }
    operator fun get(key: String, defaultValue: String): String {
        return property.getProperty(key) ?: defaultValue
    }

    companion object {
        fun parse(inputStream: InputStream) = kotlin.runCatching {
            PropertyParser(
                Properties().apply {
                    inputStream.use {
                        load(it)
                    }
                }
            )
        }.getOrNull()

        fun parse(reader: Reader) = kotlin.runCatching {
            PropertyParser(
                Properties().apply {
                    reader.use {
                        load(it)
                    }
                }
            )
        }.getOrNull()

        fun parse(text: String) = kotlin.runCatching {
            PropertyParser(
                Properties().apply {
                    text.reader().use {
                        load(it)
                    }
                }
            )
        }.getOrNull()
    }
}