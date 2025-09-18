@file:Suppress("FunctionName")

package ru.blays.utils.kotlinx.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.Enum as JavaEnum
import kotlin.Enum as KotlinEnum

/**
 * Сериализатор для Enum типов. Сериализует Enum как строку с именем значения.
 */
inline fun <reified T: KotlinEnum<T>> EnumSerializer(): KSerializer<T> = object : KSerializer<T> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Name", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): T {
        return JavaEnum.valueOf(
            T::class.java,
            decoder.decodeString()
        )
    }
}

/**
 * Сериализатор для Enum типов. Сериализует Enum как строку с именем значения.
 */
inline fun <reified T: KotlinEnum<T>> T.serializer(): KSerializer<T> = object : KSerializer<T> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Name", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): T {
        return JavaEnum.valueOf(
            T::class.java,
            decoder.decodeString()
        )
    }
}