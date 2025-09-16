package ru.blays.hub.core.domain.components

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext

open class InfoDialogComponent<CONFIG: IInfoDialogConfig, ACTION: Any> internal constructor(
    componentContext: AppComponentContext,
    val state: CONFIG,
    private val onAction: (ACTION) -> Unit
): AppComponentContext by componentContext {
    fun sendIntent(action: ACTION) {
        onAction(action)
    }

    @Immutable
    data class State(
        val title: String,
        val message: String
    )

    class Factory {
        operator fun <CONFIG: IInfoDialogConfig, ACTION: Any> invoke(
            componentContext: AppComponentContext,
            state: CONFIG,
            onAction: (ACTION) -> Unit
        ): InfoDialogComponent<CONFIG, ACTION> {
            return InfoDialogComponent(
                componentContext = componentContext,
                state = state,
                onAction = onAction
            )
        }
    }
}

interface IInfoDialogConfig {
    val title: String
    val message: String
}

@Serializable
data class InfoDialogConfig(
    override val title: String,
    override val message: String
): IInfoDialogConfig