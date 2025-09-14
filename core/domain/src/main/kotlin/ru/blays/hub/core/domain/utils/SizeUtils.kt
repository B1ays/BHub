package ru.blays.hub.core.domain.utils

import android.annotation.SuppressLint
import java.lang.Long.numberOfLeadingZeros

@SuppressLint("DefaultLocale")
fun formatSize(v: Long): String {
    if (v < 1024) return "$v B"
    val z = (63 - numberOfLeadingZeros(v)) / 10
    return String.format(
        "%.1f %sB",
        Math.scalb(v.toFloat(), z * -10),
        SIZE_SUFFIX_ARRAY[z]
    )
}

private const val SIZE_SUFFIX_ARRAY = " KMGTPE"