package ru.blays.hub.utils.workerdsl

import androidx.work.Constraints

@WorkerDslMarker
inline fun constraints(
    block: Constraints.Builder.() -> Unit
): Constraints = Constraints.Builder().apply(block).build()