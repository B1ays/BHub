package ru.blays.hub.core.domain.components.appPageComponents

import android.content.Context
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.data.models.AppCardModel
import ru.blays.hub.core.domain.data.models.AppType
import ru.blays.hub.core.domain.data.models.AppVersionCard
import ru.blays.hub.core.domain.data.models.VersionType
import ru.blays.hub.core.domain.data.realType
import ru.blays.hub.core.moduleManager.ModuleManager
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.CatalogModel
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepository
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.hub.core.packageManager.api.getPackageManager
import ru.blays.hub.core.preferences.SettingsRepository

class AppVersionsListComponent(
    componentContext: AppComponentContext,
    private val app: AppCardModel,
    private val versionType: VersionType,
    private val appsRepository: AppsRepository,
    private val settingsRepository: SettingsRepository,
    private val moduleManager: ModuleManager,
    private val context: Context,
    private val versionPageComponentFactory: VersionPageComponent.Factory,
): AppComponentContext by componentContext {
    private val packageManager: PackageManager
        get() = getPackageManager(settingsRepository.pmType.realType)

    private val slotNavigation = SlotNavigation<VersionPageSlotConfig>()

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    val versionPageSlot = childSlot(
        source = slotNavigation,
        handleBackButton = true,
        serializer = VersionPageSlotConfig.serializer(),
        childFactory = ::slotChildFactory
    )

    fun sendIntent(intent: Intent) {
        when(intent) {
            is Intent.Refresh -> refresh()
            is Intent.Delete -> delete()
            is Intent.LaunchApp -> launchApp()
        }
    }

    fun onOutput(output: Output) {
        when(output) {
            is Output.OpenVersionPage -> slotNavigation.activate(
                configuration = VersionPageSlotConfig(
                    sourceUrl = app.sourceUrl,
                    versionCard = output.appVersion,
                    appName = app.title,
                    packageName = versionType.packageName
                )
            )
        }
    }

    internal fun onOutput(output: AppComponent.Output): Boolean {
        return if(versionPageSlot.child != null) {
            when(output) {
                AppComponent.Output.NavigateBack -> slotNavigation.dismiss()
            }
            true
        } else {
            false
        }
    }

    private fun slotChildFactory(
        config: VersionPageSlotConfig,
        componentContext: AppComponentContext
    ): VersionPageComponent {
        return versionPageComponentFactory(
            componentContext = componentContext,
            config = config,
            onRefresh = ::refresh,
            onOutput = ::onVersionPageOutput
        )
    }

    private fun refresh() {
        componentScope.launch {
            _state.update { State.Loading }
            when(
                val result = appsRepository.getAppVersions(
                    app.sourceUrl,
                    versionType.catalogHref
                )
            ) {
                is NetworkResult.Failure -> {
                    _state.update {
                        State.Error(
                            result.error.message ?: context.getString(R.string.unknown_error)
                        )
                    }
                }
                is NetworkResult.Success -> {
                    _state.update {
                        State.Loaded(
                            AppType(
                                packageName = versionType.packageName,
                                installed = checkVersionInstalled(),
                                versionsList = result.data
                                    .map(::toVersionCard)
                                    .toPersistentList()
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun checkVersionInstalled(): Boolean {
        return when(versionType) {
            is VersionType.NonRoot -> packageManager.checkPackageInstalled(versionType.packageName)
            is VersionType.Root -> {
                val moduleExists = moduleManager.checkModuleExist(versionType.packageName)
                if(moduleExists) {
                    packageManager.checkPackageInstalled(versionType.packageName)
                } else {
                    false
                }
            }
        }
    }

    private fun onVersionPageOutput(output: VersionPageComponent.Output) {
        when(output) {
            VersionPageComponent.Output.NavigateBack -> slotNavigation.dismiss()
        }
    }

    private fun launchApp() {
        componentScope.launch {
            packageManager.launchApp(versionType.packageName)
        }
    }

    private fun delete() {
        componentScope.launch {
            packageManager.uninstallApp(versionType.packageName)
            if(versionType is VersionType.Root) {
                moduleManager.delete(versionType.packageName)
            }
            refresh()
        }
    }

    private fun deleteModule() {
        app.versions.find { it is VersionType.Root }?.let { version ->
            moduleManager.delete(version.packageName)
        }
    }

    private fun toVersionCard(catalogModel: CatalogModel) = AppVersionCard(
        version = catalogModel.version,
        patchesVersion = catalogModel.patchesVersion,
        buildDate = catalogModel.buildDate,
        changelogHref = catalogModel.changelogHref,
        apkListHref = catalogModel.apkListHref
    )

    init {
        lifecycle.doOnCreate {
            refresh()
        }
    }

    sealed class Intent {
        data object Refresh: Intent()
        data object LaunchApp: Intent()
        data object Delete: Intent()
    }

    sealed class Output {
        data class OpenVersionPage(val appVersion: AppVersionCard): Output()
    }

    sealed class State {
        data object Loading: State()
        data class Error(val message: String): State()
        data class Loaded(val appType: AppType): State()
    }

    class Factory(
        private val appsRepository: AppsRepository,
        private val settingsRepository: SettingsRepository,
        private val moduleManager: ModuleManager,
        private val context: Context,
        private val versionPageComponentFactory: VersionPageComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            versionType: VersionType,
            app: AppCardModel,
        ): AppVersionsListComponent {
            return AppVersionsListComponent(
                componentContext = componentContext,
                app = app,
                versionType = versionType,
                appsRepository = appsRepository,
                settingsRepository = settingsRepository,
                moduleManager = moduleManager,
                context = context,
                versionPageComponentFactory = versionPageComponentFactory,
            )
        }
    }
}

@Serializable
data class VersionPageSlotConfig(
    val sourceUrl: String,
    val versionCard: AppVersionCard,
    val appName: String,
    val packageName: String
)