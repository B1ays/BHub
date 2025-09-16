package ru.blays.hub

import android.annotation.SuppressLint
import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.instancekeeper.instanceKeeper
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.essentyLifecycle
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.arkivanov.essenty.lifecycle.subscribe
import com.arkivanov.essenty.statekeeper.SerializableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.essenty.statekeeper.stateKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.builtins.serializer
import ru.blays.hub.core.domain.AppComponentContext
import ru.blays.hub.core.domain.DefaultAppComponentContext

fun <T> ComponentActivity.retainedComponent(
    scope: CoroutineScope,
    key: String = "RootRetainedComponent",
    handleBackButton: Boolean = true,
    discardSavedState: Boolean = false,
    isStateSavingAllowed: () -> Boolean = { true },
    factory: (AppComponentContext) -> T,
): T =
    retainedComponent(
        scope = scope,
        key = key,
        onBackPressedDispatcher = if (handleBackButton) onBackPressedDispatcher else null,
        discardSavedState = discardSavedState,
        isStateSavingAllowed = isStateSavingAllowed,
        isChangingConfigurations = ::isChangingConfigurations,
        factory = factory,
    )

internal fun <T, O> O.retainedComponent(
    scope: CoroutineScope,
    key: String,
    onBackPressedDispatcher: OnBackPressedDispatcher?,
    discardSavedState: Boolean,
    isStateSavingAllowed: () -> Boolean,
    isChangingConfigurations: () -> Boolean,
    factory: (AppComponentContext) -> T,
): T where O : LifecycleOwner, O : SavedStateRegistryOwner, O : ViewModelStoreOwner {
    check(savedStateRegistry.getSavedStateProvider(key = key) == null) {
        "Another retained component is already registered with the key: $key"
    }

    val lifecycle = essentyLifecycle()
    val stateKeeper = stateKeeper(key = key, discardSavedState = discardSavedState, isSavingAllowed = isStateSavingAllowed)
    val instanceKeeper = instanceKeeper()

    val discardRetainedInstance = stateKeeper.consume(key = KEY_STATE_MARKER, strategy = Boolean.serializer()) == null
    stateKeeper.register(key = KEY_STATE_MARKER, strategy = Boolean.serializer()) { true }

    if (discardRetainedInstance) {
        (instanceKeeper.remove(key = key) as RetainedComponentHolder<*>?)?.also { holder ->
            holder.lifecycle.destroy()
            holder.onDestroy()
        }
    }

    val holder =
        instanceKeeper.getOrCreate(key = key) {
            RetainedComponentHolder(
                scope = scope,
                savedState = stateKeeper.consume(key = key, strategy = SerializableContainer.serializer()),
                factory = factory,
            )
        }

    lifecycle.subscribe(
        onCreate = { holder.lifecycle.create() },
        onStart = { holder.lifecycle.start() },
        onResume = { holder.lifecycle.resume() },
        onPause = {
            if (!isChangingConfigurations()) {
                holder.lifecycle.pause()
            }
        },
        onStop = {
            if (!isChangingConfigurations()) {
                holder.lifecycle.stop()
            }
        },
        onDestroy = {
            if (!isChangingConfigurations()) {
                holder.lifecycle.destroy()
            }
        },
    )

    stateKeeper.register(
        key = key,
        strategy = SerializableContainer.serializer()
    ) {
        holder.stateKeeper.save()
    }

    if(onBackPressedDispatcher != null) {
        val onBackPressedCallback = DelegateOnBackPressedCallback(holder.onBackPressedDispatcher)
        holder.onBackEnabledChangedListener = { onBackPressedCallback.isEnabled = it }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    return holder.component
}

private class RetainedComponentHolder<out T>(
    scope: CoroutineScope,
    savedState: SerializableContainer?,
    factory: (AppComponentContext) -> T,
) : InstanceKeeper.Instance {
    val lifecycle: LifecycleRegistry = LifecycleRegistry()
    val stateKeeper: StateKeeperDispatcher = StateKeeperDispatcher(savedState = savedState)
    private val instanceKeeper: InstanceKeeperDispatcher = InstanceKeeperDispatcher()

    var onBackEnabledChangedListener: ((Boolean) -> Unit)? = null

    val onBackPressedDispatcher: OnBackPressedDispatcher =
        OnBackPressedDispatcher(
            fallbackOnBackPressed = null,
            onHasEnabledCallbacksChanged = { onBackEnabledChangedListener?.invoke(it) },
        )

    val component: T =
        factory(
            DefaultAppComponentContext(
                scope = scope,
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                instanceKeeper = instanceKeeper,
                backHandler = BackHandler(onBackPressedDispatcher),
            )
        )

    override fun onDestroy() {
        instanceKeeper.destroy()
    }
}

private class DelegateOnBackPressedCallback(
    private val dispatcher: OnBackPressedDispatcher,
) : OnBackPressedCallback(enabled = dispatcher.hasEnabledCallbacks()) {
    override fun handleOnBackPressed() {
        dispatcher.onBackPressed()
    }

    @SuppressLint("VisibleForTests")
    override fun handleOnBackStarted(backEvent: BackEventCompat) {
        dispatcher.dispatchOnBackStarted(backEvent)
    }

    @SuppressLint("VisibleForTests")
    override fun handleOnBackProgressed(backEvent: BackEventCompat) {
        dispatcher.dispatchOnBackProgressed(backEvent)
    }

    @SuppressLint("VisibleForTests")
    override fun handleOnBackCancelled() {
        dispatcher.dispatchOnBackCancelled()
    }
}

private const val KEY_STATE_MARKER = "DefaultComponentContext_state_marker"