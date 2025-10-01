package ru.blays.preferences.android

import android.R.attr.defaultValue
import android.content.SharedPreferences
import kotlinx.serialization.serializerOrNull
import ru.blays.preferences.DefaultJson
import ru.blays.preferences.api.PreferenceReader
import ru.blays.preferences.api.PreferenceWriter
import ru.blays.preferences.api.Preferences
import ru.blays.preferences.extensions.edit
import kotlin.reflect.KClass
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
    @Suppress("UNCHECKED_CAST", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    override fun <T> createReader(
        key: String,
        defaultValue: T,
        type: KType,
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
                    val name = sharedPreferences.getString(key, defaultValue.name) ?: defaultValue.name
                    java.lang.Enum.valueOf(defaultValue::class.java, name) as T
                }
                else -> {
                    val serializer = DefaultJson.serializersModule.serializerOrNull(type)
                    if(serializer != null) {
                        val defaultValue = DefaultJson.encodeToString(serializer, defaultValue)
                        val json = sharedPreferences.getString(key, defaultValue) ?: defaultValue
                        DefaultJson.decodeFromString(serializer, json) as T
                    } else {
                        throw IllegalArgumentException("Unsupported type")
                    }
                }
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
                when(type.classifier) {
                    BooleanType.classifier -> {
                        sharedPreferences.getBoolean(key, false) as T
                    }
                    StringType.classifier -> {
                        sharedPreferences.getString(key, null) as T
                    }
                    IntType.classifier -> {
                        sharedPreferences.getInt(key, 0) as T
                    }
                    LongType.classifier -> {
                        sharedPreferences.getLong(key, 0) as T
                    }
                    FloatType.classifier -> {
                        sharedPreferences.getFloat(key, 0F) as T
                    }
                    EnumType.classifier -> {
                        val name = sharedPreferences.getString(key, null)
                            ?: return@PreferenceReader null
                        val kClass = type.classifier as? KClass<*>
                            ?: return@PreferenceReader null
                        java.lang.Enum.valueOf(kClass.java as Class<out Enum<*>>, name) as T
                    }
                    else -> {
                        val serializer = DefaultJson.serializersModule.serializerOrNull(type)
                        if(serializer != null) {
                            val defaultValue = DefaultJson.encodeToString(serializer, defaultValue)
                            val json = sharedPreferences.getString(key, defaultValue) ?: defaultValue
                            DefaultJson.decodeFromString(serializer, json) as T
                        } else {
                            throw IllegalArgumentException("Unsupported type")
                        }
                    }
                }
            } else {
                null
            }
        }
    }

    override fun <T> createWriter(key: String, type: KType): PreferenceWriter<T> {
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
                else -> {
                    val serializer = DefaultJson.serializersModule.serializerOrNull(type)
                    if(serializer != null) {
                        val json = DefaultJson.encodeToString(serializer, value)
                        sharedPreferences.edit {
                            putString(key, json)
                        }
                    } else false
                }
            }
        }
    }

    override fun <T> createNullableWriter(key: String, type: KType): PreferenceWriter<T?> {
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
                else -> {
                    val serializer = DefaultJson.serializersModule.serializerOrNull(type)
                    if(serializer != null) {
                        val json = DefaultJson.encodeToString(serializer, value)
                        sharedPreferences.edit {
                            putString(key, json)
                        }
                    } else false
                }
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