package ru.blays.hub.core.logic.components

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable

open class InfoDialogComponent<CONFIG: IInfoDialogConfig, ACTION: Any> internal constructor(
    componentContext: ComponentContext,
    val state: CONFIG,
    private val onAction: (ACTION) -> Unit
): ComponentContext by componentContext {
    fun sendIntent(action: ACTION) {
        onAction(action)
    }

    @Immutable
    data class State(
        val title: String,
        val message: String
    )
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