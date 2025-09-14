package ru.blays.hub.core.domain.utils

import android.annotation.SuppressLint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

internal val File.readableDate: String
    get() = dateFormat.format(Date(lastModified()))

internal val File.readableSize: String
    get() = formatSize(length())

@SuppressLint("SimpleDateFormat")
private val dateFormat = SimpleDateFormat("yyyy-MM-dd")