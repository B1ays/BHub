package ru.blays.hub.core.domain.components.appPageComponents

import android.content.Context
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.data.LocalizedMessage
import ru.blays.hub.core.domain.data.models.AppCardModel
import ru.blays.hub.core.domain.utils.currentLanguage
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.okHttpDsl.fullUrlString
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository

class AppDescriptionComponent(
    componentContext: AppComponentContext,
    private val app: AppCardModel,
    private val networkRepository: NetworkRepository,
    private val context: Context,
): AppComponentContext by componentContext {
    private val _state: MutableStateFlow<State> = MutableStateFlow(
        if(app.appInfo != null) State.Loading else State.NotProvided
    )

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    fun sendIntent(intent: Intent) {
        when(intent) {
            Intent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        val appInfo = app.appInfo ?: return
        if(state.value == State.NotProvided) return
        componentScope.launch {
            _state.update { State.Loading }
            val readmeResult = networkRepository.getString(
                fullUrlString(app.sourceUrl, appInfo.readmeHref)
            )
            when(readmeResult) {
                is NetworkResult.Failure -> {
                    _state.update {
                        State.Error(
                            readmeResult.error.message ?: context.getString(R.string.unknown_error)
                        )
                    }
                }
                is NetworkResult.Success -> {
                    val localizedMessage = LocalizedMessage(readmeResult.data)
                    val readme = if(localizedMessage.isValid) {
                        localizedMessage.getForLanguageOrDefault(context.currentLanguage)
                    } else {
                        readmeResult.data
                    }
                    _state.update {
                        State.Loaded(
                            app = app,
                            readme = readme,
                            images = appInfo.images.toPersistentList(),
                        )
                    }
                }
            }
        }
    }

    init {
        lifecycle.doOnCreate {
            if(state.value != State.NotProvided) {
                refresh()
            }
        }
    }

    sealed class Intent {
        data object Refresh: Intent()
    }

    sealed class State {
        data object Loading: State()
        data class Error(val message: String): State()
        data object NotProvided: State()
        data class Loaded(
            val app: AppCardModel,
            val readme: String?,
            val images: PersistentList<*>,
        ): State()
    }

    class Factory(
        private val networkRepository: NetworkRepository,
        private val context: Context
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            app: AppCardModel,
        ): AppDescriptionComponent {
            return AppDescriptionComponent(
                componentContext = componentContext,
                app = app,
                networkRepository = networkRepository,
                context = context
            )
        }
    }
}