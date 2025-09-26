package ru.blays.preferences.android

import android.content.SharedPreferences
import ru.blays.preferences.api.PreferenceReader
import ru.blays.preferences.api.PreferenceWriter
import ru.blays.preferences.api.Preferences
import ru.blays.preferences.extensions.edit
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Implementation of [Preferences] using [SharedPreferences].
 * @param sharedPreferences [SharedPreferences] for storing settings.
 * @see SharedPreferences
 */
class AndroidPreferences(
    private val sharedPreferences: SharedPreferences
): Preferences {
    @Suppress("UNCHECKED_CAST")
    override fun <T> createReader(
        key: String,
        defaultValue: T,
    ): PreferenceReader<T> {
        return PreferenceReader {
            when (defaultValue) {
                is Boolean -> {
                    sharedPreferences.getBoolean(key, defaultValue) as T
                }
                is String -> {
                    sharedPreferences.getString(key, defaultValue) as T
                }
                is Int -> {
                    sharedPreferences.getInt(key, defaultValue) as T
                }
                is Long -> {
                    sharedPreferences.getLong(key, defaultValue) as T
                }
                is Float -> {
                    sharedPreferences.getFloat(key, defaultValue) as T
                }
                is Enum<*> -> {
                    sharedPreferences.getString(key, defaultValue.name) as T
                }
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> createNullableReader(
        key: String,
        type: KType,
    ): PreferenceReader<T?> {
        return PreferenceReader {
            if(containsKey(key)) {
                when(type) {
                    BooleanType -> {
                        sharedPreferences.getBoolean(key, false) as T
                    }
                    StringType -> {
                        sharedPreferences.getString(key, null) as T
                    }
                    IntType -> {
                        sharedPreferences.getInt(key, 0) as T
                    }
                    LongType -> {
                        sharedPreferences.getLong(key, 0) as T
                    }
                    FloatType -> {
                        sharedPreferences.getFloat(key, 0F) as T
                    }
                    EnumType -> {
                        sharedPreferences.getString(key, null) as T
                    }
                    else -> throw IllegalArgumentException("Unsupported type")
                }
            } else {
                null
            }
        }
    }

    override fun <T> createWriter(key: String): PreferenceWriter<T> {
        return PreferenceWriter { value ->
            when(value) {
                is Boolean -> sharedPreferences.edit {
                    putBoolean(key, value)
                }
                is String -> sharedPreferences.edit {
                    putString(key, value)
                }
                is Int -> sharedPreferences.edit {
                    putInt(key, value)
                }
                is Long -> sharedPreferences.edit {
                    putLong(key, value)
                }
                is Float -> sharedPreferences.edit {
                    putFloat(key, value)
                }
                is Enum<*> -> sharedPreferences.edit {
                    putString(key, value.name)
                }
                else -> false
            }
        }
    }

    override fun <T> createNullableWriter(key: String): PreferenceWriter<T?> {
        return PreferenceWriter { value ->
            when(value) {
                is Boolean -> sharedPreferences.edit {
                    putBoolean(key, value)
                }
                is String -> sharedPreferences.edit {
                    putString(key, value)
                }
                is Int -> sharedPreferences.edit {
                    putInt(key, value)
                }
                is Long -> sharedPreferences.edit {
                    putLong(key, value)
                }
                is Float -> sharedPreferences.edit {
                    putFloat(key, value)
                }
                is Enum<*> -> sharedPreferences.edit {
                    putString(key, value.name)
                }
                null -> removeKey(key)
                else -> false
            }
        }
    }

    override fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    override fun removeKey(key: String): Boolean {
        return sharedPreferences.edit { remove(key) }
    }

    private companion object {
        private val BooleanType = typeOf<Boolean>()
        private val StringType = typeOf<String>()
        private val IntType = typeOf<Int>()
        private val LongType = typeOf<Long>()
        private val FloatType = typeOf<Float>()
        private val EnumType = typeOf<Enum<*>>()
    }
}