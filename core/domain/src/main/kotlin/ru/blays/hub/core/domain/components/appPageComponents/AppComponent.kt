package ru.blays.hub.core.domain.components.appPageComponents

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.data.models.AppCardModel
import ru.blays.hub.core.domain.data.models.VersionType

@OptIn(ExperimentalDecomposeApi::class)
@Stable
class AppComponent private constructor(
    componentContext: AppComponentContext,
    private val app: AppCardModel,
    private val descriptionComponentFactory: AppDescriptionComponent.Factory,
    private val versionsComponentFactory: AppVersionsListComponent.Factory,
    private val onRefresh: () -> Unit,
    private val onOutput: (Output) -> Unit,
): AppComponentContext by componentContext {
    private val pagesNavigation = PagesNavigation<TabsConfiguration>()

    private var appDescriptionChild: TabsChild.Description? = null
    private var nonRootVersionsChild: TabsChild.NonRoot? = null
    private var rootVersionsChild: TabsChild.Root? = null

    val appName = app.title

    val childPages: Value<ChildPages<TabsConfiguration, TabsChild>> = childPages(
        source = pagesNavigation,
        initialPages = {
            Pages(
                items = buildList {
                    add(TabsConfiguration.Description)
                    app.versions.forEach { type ->
                        when(type) {
                            is VersionType.NonRoot -> add(TabsConfiguration.NonRoot(type))
                            is VersionType.Root -> add(TabsConfiguration.Root(type))
                        }
                    }
                },
                selectedIndex = 0
            )
        },
        serializer = TabsConfiguration.serializer(),
        childFactory = ::pagesChildFactory
    )

    fun onPageSelected(index: Int) {
        pagesNavigation.select(index)
    }

    private fun pagesChildFactory(
        tabsConfiguration: TabsConfiguration,
        componentContext: AppComponentContext
    ): TabsChild {
        return when(tabsConfiguration) {
            TabsConfiguration.Description -> appDescriptionChild ?: TabsChild.Description(
                descriptionComponentFactory(
                    componentContext = componentContext,
                    app = app
                )
            ).also {
                appDescriptionChild = it
            }
            is TabsConfiguration.NonRoot -> nonRootVersionsChild ?: TabsChild.NonRoot(
                versionsComponentFactory(
                    componentContext = componentContext,
                    app = app,
                    versionType = tabsConfiguration.versionType
                )
            ).also {
                nonRootVersionsChild = it
            }
            is TabsConfiguration.Root -> rootVersionsChild ?: TabsChild.Root(
                versionsComponentFactory(
                    componentContext = componentContext,
                    app = app,
                    versionType = tabsConfiguration.versionType
                )
            ).also {
                rootVersionsChild = it
            }
        }
    }

    fun onOutput(output: Output) {
        val childPages = childPages.value
        when(
            val child = childPages.items[childPages.selectedIndex].instance
        ) {
            is TabsChild.NonRoot -> {
                if(!child.component.onOutput(output)) {
                    onOutput.invoke(output)
                }
            }
            is TabsChild.Root -> {
                if(!child.component.onOutput(output)) {
                    onOutput.invoke(output)
                }
            }
            else -> onOutput.invoke(output)
        }
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    class Factory(
        private val descriptionComponentFactory: AppDescriptionComponent.Factory,
        private val versionsComponentFactory: AppVersionsListComponent.Factory,
    ) {
        operator fun invoke(
            componentContext: AppComponentContext,
            app: AppCardModel,
            onRefresh: () -> Unit,
            onOutput: (Output) -> Unit,
        ): AppComponent {
            return AppComponent(
                componentContext = componentContext,
                app = app,
                descriptionComponentFactory = descriptionComponentFactory,
                versionsComponentFactory = versionsComponentFactory,
                onRefresh = onRefresh,
                onOutput = onOutput
            )
        }
    }
}

@Serializable
sealed class TabsConfiguration {
    @Serializable
    data object Description: TabsConfiguration()
    @Serializable
    data class NonRoot(val versionType: VersionType): TabsConfiguration()
    @Serializable
    data class Root(val versionType: VersionType): TabsConfiguration()
}

sealed class TabsChild {
    data class Description(val component: AppDescriptionComponent) : TabsChild()
    data class NonRoot(val component: AppVersionsListComponent) : TabsChild()
    data class Root(val component: AppVersionsListComponent) : TabsChild()
}