package ru.blays.hub.core.logger

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("ConstantLocale")
val defaultFormatter = SimpleDateFormat(
    "yyyy-MM-dd HH-mm-ss",
    Locale.getDefault()
)

val currentTime: Date get() = Calendar.getInstance().time

val DateFormat.currentTime: String
    get() = format(ru.blays.hub.core.logger.currentTime)