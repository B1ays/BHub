package ru.blays.hub.core.domain

import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.coroutines.CoroutineScope

class DefaultAppComponentContext private constructor(
    override val lifecycle: Lifecycle,
    override val stateKeeper: StateKeeper,
    override val instanceKeeper: InstanceKeeper,
    override val backHandler: BackHandler,
    override val componentContextFactory: ComponentContextFactory<AppComponentContext>,
    override val componentScope: CoroutineScope
): AppComponentContext {
    constructor(
        scope: CoroutineScope,
        lifecycle: Lifecycle,
        stateKeeper: StateKeeper? = null,
        instanceKeeper: InstanceKeeper? = null,
        backHandler: BackHandler? = null,
    ): this(
        lifecycle = lifecycle,
        stateKeeper = stateKeeper ?: StateKeeperDispatcher(),
        instanceKeeper = instanceKeeper ?: InstanceKeeperDispatcher().apply {
            lifecycle.doOnDestroy(::destroy)
        },
        backHandler = backHandler ?: BackDispatcher(),
        componentContextFactory = AppComponentContextFactory(scope),
        componentScope = scope,
    )

    class AppComponentContextFactory(
        private val scope: CoroutineScope,
    ): ComponentContextFactory<AppComponentContext> {
        override fun invoke(
            lifecycle: Lifecycle,
            stateKeeper: StateKeeper,
            instanceKeeper: InstanceKeeper,
            backHandler: BackHandler,
        ): AppComponentContext {
            return DefaultAppComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                instanceKeeper = instanceKeeper,
                backHandler = backHandler,
                componentContextFactory = this,
                componentScope = scope,
            )
        }
    }
}