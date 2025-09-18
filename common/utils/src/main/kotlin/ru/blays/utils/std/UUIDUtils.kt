@file:Suppress("NOTHING_TO_INLINE")

package ru.blays.utils.std

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * UUID в виде строки
 */
@OptIn(ExperimentalUuidApi::class)
inline fun stringUUID(): String = Uuid.random().toString()