package ru.blays.utils.androidx.worker

import androidx.work.Data

@WorkerDslMarker
inline fun workData(
    block: Data.Builder.() -> Unit
): Data = Data.Builder().apply(block).build()