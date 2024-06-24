package ru.blays.hub.core.ui.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity

@get:JvmName("getActivityProperty")
@Suppress("RecursivePropertyAccessor")
val Context.activity: ComponentActivity?
    get() = when(this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.activity
    else -> null
}

fun Context.getActivity(): ComponentActivity? = when(this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}