package ru.blays.hub.core.domain.components.settingsComponents.catalogsSettings

import android.content.Context
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.CATALOG_PROPS_HREF
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.data.models.CatalogModel
import ru.blays.hub.core.domain.utils.PropertyParser
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.okHttpDsl.fullUrlStringOrNull
import ru.blays.hub.core.network.repositories.networkRepository.NetworkRepository

class AddCatalogComponent private constructor(
    componentContext: AppComponentContext,
    private val networkRepository: NetworkRepository,
    private val context: Context,
    private val checkCatalogExists: suspend (url: String) -> Boolean,
    private val onOutput: (Output) -> Unit,
) : AppComponentContext by componentContext {
    private val stackNavigation = StackNavigation<Configuration>()

    val steps = childStack(
        source = stackNavigation,
        initialConfiguration = Configuration.Input(),
        handleBackButton = false,
        serializer = Configuration.serializer(),
        childFactory = ::childFactory
    )

    private fun childFactory(
        configuration: Configuration,
        childContext: AppComponentContext
    ): Step {
        return when (configuration) {
            is Configuration.Error -> Step.Error(
                ErrorComponent(
                    componentContext = childContext,
                    url = configuration.url,
                    errorMessage = configuration.errorMessage
                )
            )
            is Configuration.Info -> Step.Info(
                InfoComponent(
                    componentContext = childContext,
                    catalog = configuration.catalog
                )
            )
            is Configuration.Input -> Step.Input(
                InputComponent(
                    componentContext = childContext,
                    initialValue = configuration.initialValue
                )
            )
            Configuration.Loading -> Step.Loading
            is Configuration.Exists -> Step.Exists(
                CatalogExistsComponent(
                    componentContext = childContext,
                    url = configuration.url
                )
            )
        }
    }

    fun close() {
        onOutput.invoke(Output.Close)
    }

    private fun backToEditUrl(url: String) {
        stackNavigation.replaceAll(
            Configuration.Input(url)
        )
    }

    private fun save(catalog: CatalogModel) {
        onOutput.invoke(
            Output.AddCatalog(catalog)
        )
    }

    private fun loadCatalogInfo(url: String) {
        componentScope.launch {
            if (checkCatalogExists(url)) {
                stackNavigation.bringToFront(Configuration.Exists(url))
                return@launch
            }

            val fullUrl = fullUrlStringOrNull(url, CATALOG_PROPS_HREF)
            if (fullUrl == null) {
                stackNavigation.bringToFront(
                    Configuration.Error(
                        url = url,
                        errorMessage = context.getString(R.string.error_invalidUrl)
                    )
                )
                return@launch
            } else {
                stackNavigation.bringToFront(Configuration.Loading)
            }
            when (
                val inputStreamResult = networkRepository.openStream(fullUrl)
            ) {
                is NetworkResult.Failure -> {
                    stackNavigation.bringToFront(
                        Configuration.Error(
                            url = url,
                            errorMessage = inputStreamResult.error.localizedMessage
                                ?: context.getString(R.string.unknown_error)
                        )
                    )
                    return@launch
                }

                is NetworkResult.Success -> {
                    val properties = PropertyParser.parse(inputStreamResult.data)
                    if (properties == null) {
                        stackNavigation.bringToFront(
                            Configuration.Error(
                                url = url,
                                errorMessage = context.getString(R.string.error_cantParseProperties)
                            )
                        )
                        return@launch
                    }
                    val catalog = CatalogModel(
                        id = 0,
                        name = properties[PROPERTY_NAME]
                            ?: context.getString(R.string.unknown_name),
                        url = url,
                        owner = properties[PROPERTY_OWNER]
                            ?: context.getString(R.string.unknown_owner),
                        enabled = true
                    )
                    stackNavigation.bringToFront(Configuration.Info(catalog))
                }
            }
        }
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data class Input(val initialValue: String = "") : Configuration()

        @Serializable
        data class Error(
            val url: String,
            val errorMessage: String
        ) : Configuration()

        @Serializable
        data object Loading : Configuration()

        @Serializable
        data class Info(val catalog: CatalogModel) : Configuration()
        data class Exists(val url: String) : Configuration()
    }

    sealed class Step {
        data class Input(val component: InputComponent) : Step()
        data class Info(val component: InfoComponent) : Step()
        data class Error(val component: ErrorComponent) : Step()
        data object Loading : Step()
        data class Exists(val component: CatalogExistsComponent) : Step()
    }

    sealed class Output {
        data object Close : Output()
        internal data class AddCatalog(val catalog: CatalogModel) : Output()
    }

    companion object {
        private const val PROPERTY_NAME = "name"
        private const val PROPERTY_OWNER = "owner"
    }

    inner class InputComponent(
        componentContext: AppComponentContext,
        initialValue: String
    ) : AppComponentContext by componentContext {
        private val _state = MutableStateFlow(initialValue)

        val state: StateFlow<String> = _state.asStateFlow()

        fun onTextChange(text: String) {
            _state.value = text
        }

        fun submit() {
            this@AddCatalogComponent.loadCatalogInfo(state.value)
        }

        fun close() {
            this@AddCatalogComponent.close()
        }
    }

    inner class InfoComponent(
        componentContext: AppComponentContext,
        val catalog: CatalogModel
    ) : AppComponentContext by componentContext {
        fun backToEditUrl() {
            this@AddCatalogComponent.backToEditUrl(catalog.url)
        }

        fun save() {
            this@AddCatalogComponent.save(catalog)
        }
    }

    inner class ErrorComponent(
        componentContext: AppComponentContext,
        private val url: String,
        val errorMessage: String
    ) : AppComponentContext by componentContext {
        fun backToEditUrl() {
            this@AddCatalogComponent.backToEditUrl(url)
        }

        fun close() {
            this@AddCatalogComponent.close()
        }
    }

    inner class CatalogExistsComponent(
        componentContext: AppComponentContext,
        url: String
    ) : AppComponentContext by componentContext {
        val message = context.getString(
            R.string.catalog_exists_formatted,
            url
        )

        fun close() {
            this@AddCatalogComponent.close()
        }
    }

    class Factory(
        private val networkRepository: NetworkRepository,
        private val context: Context,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            checkCatalogExists: suspend (url: String) -> Boolean,
            onOutput: (Output) -> Unit,
        ): AddCatalogComponent {
            return AddCatalogComponent(
                componentContext = componentContext,
                networkRepository = networkRepository,
                context = context,
                checkCatalogExists = checkCatalogExists,
                onOutput = onOutput
            )
        }
    }
}