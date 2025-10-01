package ru.blays.hub.core.domain.components.downloadComponents

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.SortReverseOrderAccessor
import ru.blays.hub.core.domain.SortTypeAccessor
import ru.blays.hub.core.domain.data.FilesSortType
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder

class DownloadsMenuComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val onIntent: (Intent) -> Unit,
) : AppComponentContext by componentContext {
    private val sortTypeValue = preferencesHolder.getValue(SortTypeAccessor)
    private val sortReverseOrderValue = preferencesHolder.getValue(SortReverseOrderAccessor)

    val state: StateFlow<State> = combine(
        sortTypeValue,
        sortReverseOrderValue
    ) { sortType, reverseOrder ->
        State(
            filesSortType = sortType,
            reverseOrder = reverseOrder
        )
    }.stateIn(
        scope = componentScope,
        started = SharingStarted.Eagerly,
        initialValue = State(
            filesSortType = sortTypeValue.value,
            reverseOrder = sortReverseOrderValue.value
        )
    )

    fun sendIntent(intent: Intent) = onIntent.invoke(intent)


    data class State(
        val filesSortType: FilesSortType,
        val reverseOrder: Boolean,
    )

    sealed class Intent {
        data object Refresh : Intent()
        data object ClearAll : Intent()
        data class ChangeSortSetting(val type: FilesSortType) : Intent()
        data class ChangeSortOrder(val reverse: Boolean) : Intent()
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onIntent: (Intent) -> Unit,
        ): DownloadsMenuComponent {
            return DownloadsMenuComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                onIntent = onIntent
            )
        }
    }
}