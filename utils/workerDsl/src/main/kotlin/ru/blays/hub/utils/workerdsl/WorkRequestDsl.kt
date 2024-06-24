package ru.blays.hub.utils.workerdsl

import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import kotlin.time.Duration
import kotlin.time.toJavaDuration


inline fun <reified T: ListenableWorker> oneTimeWorkRequest(
    block: OneTimeWorkRequest.Builder.() -> Unit = {}
): OneTimeWorkRequest = OneTimeWorkRequestBuilder<T>()
    .apply(block)
    .build()

inline fun <reified T: ListenableWorker> periodicWorkRequest(
    repeatInterval: Duration,
    block: PeriodicWorkRequest.Builder.() -> Unit = {}
): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<T>(repeatInterval.toJavaDuration())
        .apply(block)
        .build()

inline fun <reified T: ListenableWorker> periodicWorkRequest(
    repeatInterval: Duration,
    flexTimeInterval: Duration,
    block: PeriodicWorkRequest.Builder.() -> Unit = {}
): PeriodicWorkRequest =
    PeriodicWorkRequestBuilder<T>(
        repeatInterval.toJavaDuration(),
        flexTimeInterval.toJavaDuration()
    )
        .apply(block)
        .build()