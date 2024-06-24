package ru.blays.hub.core.logic.components.settingsComponents.catalogsSettings

import android.content.Context
import android.widget.Toast
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.hub.core.data.repositories.CatalogsRepository
import ru.blays.hub.core.logic.R
import ru.blays.hub.core.logic.data.models.CatalogModel
import ru.blays.hub.core.logic.utils.mutate
import ru.blays.hub.core.logic.utils.replace

class CatalogsSettingComponent(
    componentContext: ComponentContext,
    private val onOutput: (output: Output) -> Unit
): ComponentContext by componentContext, KoinComponent {
    private val catalogsRepository: CatalogsRepository by inject()
    private val context: Context by inject()

    private val slotNavigation: SlotNavigation<Unit> = SlotNavigation()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val _state = MutableStateFlow(State())

    val state: StateFlow<State> = _state.asStateFlow()

    val childSlot = childSlot(
        source = slotNavigation,
        initialConfiguration = { null },
        serializer = Unit.serializer(),
    ) { _, childContext ->
        AddCatalogComponent(
            componentContext = childContext,
            onOutput = ::onAddCatalogOutput,
            checkCatalogExists = ::checkCatalogExists
        )
    }

    fun sendIntent(intent: Intent) {
        when(intent) {
            Intent.Refresh -> refresh()
            Intent.AddCatalog -> slotNavigation.activate(Unit)
            is Intent.ChangeCatalogEnabled -> changeCatalogEnabled(
                intent.catalog,
                intent.enabled
            )
            Intent.ClearCatalogs -> clearCatalogs()
            is Intent.DeleteCatalog -> deleteCatalog(intent.catalog)
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    private fun onAddCatalogOutput(output: AddCatalogComponent.Output) {
        when(output) {
            is AddCatalogComponent.Output.AddCatalog -> {
                addCatalog(output.catalog)
                slotNavigation.dismiss()
            }
            AddCatalogComponent.Output.Close -> slotNavigation.dismiss()
        }
    }

    private fun addCatalog(catalog: CatalogModel) {
        coroutineScope.launch {
            val rowId = catalogsRepository.addCatalog(
                catalog.toDbEntity()
            )
            val updatedModel = catalog.copy(id = rowId)
            _state.update {
                it.copy(
                    catalogs = it.catalogs + updatedModel
                )
            }
        }
    }

    private fun changeCatalogEnabled(
        catalog: CatalogModel,
        enabled: Boolean
    ) {
        coroutineScope.launch {
            if(state.value.catalogs.size == 1) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        R.string.unable_disable_catalog,
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@launch
            }
            val newModel = catalog.copy(enabled = enabled)
            catalogsRepository.updateCatalog(
                newModel.toDbEntity()
            )
            _state.update {
                it.copy(
                    catalogs = it.catalogs.mutate {
                        replace(catalog, newModel)
                    }
                )
            }
        }
    }

    private fun deleteCatalog(catalog: CatalogModel) {
        coroutineScope.launch {
            catalogsRepository.deleteCatalog(catalog.id)
            _state.update {
                it.copy(
                    catalogs = it.catalogs - catalog
                )
            }
        }
    }

    private fun clearCatalogs() {
        coroutineScope.launch {
            catalogsRepository.clearCatalogs()
            _state.update {
                it.copy(
                    catalogs = emptyList()
                )
            }
        }
    }

    private suspend fun checkCatalogExists(url: String): Boolean {
        return catalogsRepository.checkCatalogExists(url)
    }

    private fun refresh() {
        coroutineScope.launch {
            _state.update { it.copy(loading = true) }
            val catalogs = catalogsRepository
                .getAllCatalogs()
                .map(CatalogModel::fromEntity)
            _state.update {
                it.copy(
                    catalogs = catalogs,
                    loading = false
                )
            }
        }
    }

    init {
        lifecycle.doOnCreate { refresh() }
        lifecycle.doOnDestroy { coroutineScope.cancel() }
    }

    data class State(
        val catalogs: List<CatalogModel> = emptyList(),
        val loading: Boolean = false
    )

    sealed class Intent {
        data object Refresh: Intent()
        data object AddCatalog: Intent()
        data class ChangeCatalogEnabled(
            val catalog: CatalogModel,
            val enabled: Boolean
        ): Intent()
        data class DeleteCatalog(val catalog: CatalogModel): Intent()
        data object ClearCatalogs: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
    }
}