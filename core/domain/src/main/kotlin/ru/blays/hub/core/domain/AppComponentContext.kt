package ru.blays.hub.core.domain

import com.arkivanov.decompose.GenericComponentContext
import kotlinx.coroutines.CoroutineScope

/**
 * Application component context
 */
interface AppComponentContext: GenericComponentContext<AppComponentContext> {
    /**
     * Default component [CoroutineScope]
     */
    val componentScope: CoroutineScope
}