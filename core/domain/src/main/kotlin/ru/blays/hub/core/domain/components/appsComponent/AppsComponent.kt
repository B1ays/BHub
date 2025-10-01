package ru.blays.hub.core.domain.components.appsComponent

import android.content.Context
import androidx.compose.runtime.Stable
import com.arkivanov.essenty.lifecycle.doOnCreate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.blays.hub.core.data.repositories.CatalogsRepository
import ru.blays.hub.core.data.room.entities.CatalogEntity
import ru.blays.hub.core.domain.APPS_HREF
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.PackageManagerAccessor
import ru.blays.hub.core.domain.PackageManagerResolver
import ru.blays.hub.core.domain.R
import ru.blays.hub.core.domain.RootModeAccessor
import ru.blays.hub.core.domain.data.LocalizationsMap
import ru.blays.hub.core.domain.data.models.AppCardModel
import ru.blays.hub.core.domain.data.models.VersionType
import ru.blays.hub.core.domain.data.models.VersionType.NonRoot
import ru.blays.hub.core.domain.data.models.VersionType.Root
import ru.blays.hub.core.domain.utils.VersionName.Companion.toVersionName
import ru.blays.hub.core.domain.utils.currentLanguage
import ru.blays.hub.core.moduleManager.ModuleManager
import ru.blays.hub.core.network.NetworkResult
import ru.blays.hub.core.network.models.AppModel
import ru.blays.hub.core.network.okHttpDsl.fullUrlString
import ru.blays.hub.core.network.repositories.appsRepository.AppsRepository
import ru.blays.hub.core.packageManager.api.PackageManager
import ru.blays.preferences.accessor.getValue
import ru.blays.preferences.api.PreferencesHolder

@Stable
class AppsComponent private constructor(
    componentContext: AppComponentContext,
    preferencesHolder: PreferencesHolder,
    private val catalogsRepository: CatalogsRepository,
    private val appsRepository: AppsRepository,
    private val moduleManager: ModuleManager,
    private val context: Context,
    private val packageManagerResolver: PackageManagerResolver,
    private val onOutput: (Output) -> Unit
) : AppComponentContext by componentContext {
    private val packageManagerValue = preferencesHolder.getValue(PackageManagerAccessor)
    private val rootModeValue = preferencesHolder.getValue(RootModeAccessor)

    private val packageManager: PackageManager
        get() = packageManagerResolver.getPackageManager(packageManagerValue.value)

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)

    private val catalogsFlow = catalogsRepository
        .getEnabledCatalogsAsFlow()
        .distinctUntilChanged()
        .stateIn(
            scope = componentScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val state: StateFlow<State>
        get() = _state.asStateFlow()

    fun sendIntent(intent: Intent) {
        when (intent) {
            Intent.Refresh -> refresh(catalogsFlow.value)
        }
    }

    fun onOutput(output: Output) = onOutput.invoke(output)

    private fun refresh(catalogs: List<CatalogEntity>) {
        componentScope.launch {
            _state.value = State.Loading
            val groupsDeferred = catalogs
                .map { catalog ->
                    async {
                        val result = appsRepository.getApps(catalog.url, APPS_HREF)
                        val apps = when(result) {
                            is NetworkResult.Failure -> {
                                return@async null
                            }
                            is NetworkResult.Success -> transformAppModel(result.data, catalog.url)
                        }
                        State.Loaded.CatalogGroup(
                            catalogName = catalog.name,
                            catalogUrl = catalog.url,
                            apps = apps,
                        )
                    }
                }

            val groups = groupsDeferred.mapNotNull {
                it.await()
            }

            if (groups.isEmpty()) {
                _state.update {
                    State.Error(
                        message = context.getString(R.string.unknown_error)
                    )
                }
            } else {
                _state.update {
                    State.Loaded(
                        groups = groups.toPersistentList()
                    )
                }
            }
        }
    }

    private suspend fun transformAppModel(
        apps: List<AppModel>,
        sourceUrl: String
    ): List<AppCardModel> = coroutineScope {
        return@coroutineScope apps.map { app ->
            async {
                val versions = buildList {
                    app.nonRoot?.let {
                        val installedVersionName = packageManager
                            .getVersionName(it.packageName)
                            .getValueOrNull()
                        val availableVersion = appsRepository
                            .getAppVersions(sourceUrl, it.catalogHref)
                            .getOrNull()
                            ?.firstOrNull()
                            ?.version

                        add(
                            NonRoot(
                                packageName = it.packageName,
                                catalogHref = it.catalogHref,
                                availableVersionName = availableVersion,
                                installedVersionName = installedVersionName
                            )
                        )
                    }
                    if(rootModeValue.value) {
                        app.root?.let {
                            val installedVersionName = if(
                                moduleManager.checkModuleExist(it.packageName)
                            ) {
                                packageManager
                                    .getVersionName(it.packageName)
                                    .getValueOrNull()
                            } else null
                            val availableVersion = appsRepository
                                .getAppVersions(sourceUrl, it.catalogHref)
                                .getOrNull()
                                ?.firstOrNull()
                                ?.version

                            add(
                                Root(
                                    packageName = it.packageName,
                                    catalogHref = it.catalogHref,
                                    availableVersionName = availableVersion,
                                    installedVersionName = installedVersionName
                                )
                            )
                        }
                    }
                }
                val updateAvailable = hasUpdates(versions)

                val localizationsMap = LocalizationsMap(app.descriptionLocales)

                AppCardModel(
                    title = app.title,
                    description = localizationsMap.getOrDefault(context.currentLanguage),
                    iconUrl = fullUrlString(sourceUrl, app.iconHref),
                    versions = versions,
                    updateAvailable = updateAvailable,
                    appInfo = app.appInfo,
                    sourceUrl = sourceUrl

                )
            }
        }.awaitAll()
    }

    private fun hasUpdates(versions: List<VersionType>): Boolean {
        return versions.fold(false) { acc, version ->
            when {
                version.installedVersionName == null || version.availableVersionName == null -> acc
                else -> {
                    val installedVersion =
                        version.installedVersionName?.toVersionName() ?: return@fold acc
                    val availableVersion =
                        version.availableVersionName?.toVersionName() ?: return@fold acc
                    acc || (installedVersion < availableVersion)
                }
            }
        }
    }

    init {
        lifecycle.doOnCreate {
            componentScope.launch {
                catalogsFlow.collect {
                    refresh(it)
                }
            }
        }
    }

    sealed class Intent {
        data object Refresh : Intent()
    }

    sealed class Output {
        data class OpenApp(val app: AppCardModel) : Output()
    }

    sealed class State {
        data object Loading : State()
        data class Error(val message: String) : State()
        data class Loaded(val groups: PersistentList<CatalogGroup>) : State() {
            data class CatalogGroup(
                val catalogName: String,
                internal val catalogUrl: String,
                val apps: List<AppCardModel>
            )
        }
    }

    class Factory(
        private val preferencesHolder: PreferencesHolder,
        private val catalogsRepository: CatalogsRepository,
        private val appsRepository: AppsRepository,
        private val moduleManager: ModuleManager,
        private val context: Context,
        private val packageManagerResolver: PackageManagerResolver,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            onOutput: (Output) -> Unit,
        ): AppsComponent {
            return AppsComponent(
                componentContext = componentContext,
                preferencesHolder = preferencesHolder,
                catalogsRepository = catalogsRepository,
                appsRepository = appsRepository,
                moduleManager = moduleManager,
                packageManagerResolver = packageManagerResolver,
                context = context,
                onOutput = onOutput
            )
        }
    }
}