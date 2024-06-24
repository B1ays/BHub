package ru.blays.hub.core.logic.components.appPageComponents

import androidx.compose.runtime.Stable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import ru.blays.hub.core.logic.data.models.AppCardModel
import ru.blays.hub.core.logic.data.models.VersionType

@OptIn(ExperimentalDecomposeApi::class)
@Stable
class AppComponent(
    componentContext: ComponentContext,
    private val app: AppCardModel,
    private val onRefresh: () -> Unit,
    private val onOutput: (Output) -> Unit,
): ComponentContext by componentContext, KoinComponent {
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
        componentContext: ComponentContext
    ): TabsChild {
        return when(tabsConfiguration) {
            TabsConfiguration.Description -> appDescriptionChild ?: TabsChild.Description(
                component = AppDescriptionComponent(
                    componentContext = componentContext,
                    app = app
                )
            ).also {
                appDescriptionChild = it
            }
            is TabsConfiguration.NonRoot -> nonRootVersionsChild ?: TabsChild.NonRoot(
                component = AppVersionsListComponent(
                    componentContext = componentContext,
                    app = app,
                    versionType = tabsConfiguration.versionType
                )
            ).also {
                nonRootVersionsChild = it
            }
            is TabsConfiguration.Root -> rootVersionsChild ?: TabsChild.Root(
                component = AppVersionsListComponent(
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