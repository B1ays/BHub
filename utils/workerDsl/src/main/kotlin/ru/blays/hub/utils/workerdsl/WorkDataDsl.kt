package ru.blays.hub.utils.workerdsl

import androidx.work.Data

@WorkerDslMarker
inline fun workData(
    block: Data.Builder.() -> Unit
): Data = Data.Builder().apply(block).build()