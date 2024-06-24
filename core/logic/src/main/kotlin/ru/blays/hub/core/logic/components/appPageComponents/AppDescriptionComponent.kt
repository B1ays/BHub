package ru.blays.hub.core.logic.components.appPageComponents

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.logic.R
import ru.blays.hub.core.logic.data.LocalizedMessage
import ru.blays.hub.core.logic.data.models.AppCardModel
import ru.blays.hub.core.logic.utils.currentLanguage
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.okHttpDsl.fullUrlString
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository

class AppDescriptionComponent(
    componentContext: ComponentContext,
    private val app: AppCardModel,
): ComponentContext by componentContext, KoinComponent {
    private val networkRepository: NetworkRepository by inject()
    private val context: Context by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
        if(state.value == State.NotProvided) return
        coroutineScope.launch {
            _state.update { State.Loading }
            val appInfo = app.appInfo ?: return@launch
            val readmeResult = networkRepository.getString(
                fullUrlString(app.sourceUrl, appInfo.readmeHref)
            )
            when(readmeResult) {
                is NetworkResult.Failure -> {
                    _state.update {
                        State.Error(
                            readmeResult.error.message ?: context.getString(R.string.error_root_not_granted)
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
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
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
}