package ru.blays.utils.androidx.worker

import androidx.work.Constraints

@WorkerDslMarker
inline fun constraints(
    block: Constraints.Builder.() -> Unit
): Constraints = Constraints.Builder().apply(block).build()