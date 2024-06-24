/*
package ru.blays.revanced.core.logic.components.moduleInstallerComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import kotlinx.serialization.Serializable
import ru.blays.revanced.core.logic.data.models.ApkFile

typealias FilePath = String

class ModuleInstallerRootComponent(
    componentContext: ComponentContext,
    private val modApk: FilePath,
    private val origApk: FilePath? = null,
    private val onOutput: (Output) -> Unit
): ComponentContext by componentContext {
    private val navigation = StackNavigation<Configuration>()

    val childStack = childStack(
        source = navigation,

    )

    @Serializable
    sealed class Configuration {
        @Serializable
        data class ReadyToInstall(
            val modApk: FilePath,
            val origApk: FilePath
        )
        @Serializable
        data class PickOrig(
            val modApk: FilePath
        )
    }

    sealed class Output {
        data object NavigateBack: Output()
    }
}*/
