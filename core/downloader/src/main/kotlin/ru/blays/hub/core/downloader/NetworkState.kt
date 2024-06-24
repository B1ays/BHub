package ru.blays.hub.core.downloader

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

val Context.networkState: StateFlow<NetworkState> by NetworkStateDelegate

@OptIn(DelicateCoroutinesApi::class)
private data object NetworkStateDelegate {
    private lateinit var _state: StateFlow<NetworkState>

    operator fun getValue(context: Context, property: KProperty<*>): StateFlow<NetworkState> {
        if(::_state.isInitialized) {
            return _state
        }

        val connectivityManager =
            context.getSystemService(ConnectivityManager::class.java)!!

        _state = callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    launch {
                        getForCapabilities(networkCapabilities).also { state ->
                            send(state)
                        }
                    }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = GlobalScope,
            started = SharingStarted.Eagerly,
            initialValue = getCurrentState(connectivityManager)
        )

        return _state
    }

    private fun getCurrentState(connectivityManager: ConnectivityManager): NetworkState {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkState.DISCONNECTED
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkState.DISCONNECTED
        return getForCapabilities(networkCapabilities)
    }

    private fun getForCapabilities(capabilities: NetworkCapabilities): NetworkState = when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkState.CONNECTED
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkState.CONNECTED
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkState.CONNECTED
        else -> NetworkState.DISCONNECTED
    }
}

enum class NetworkState {
    CONNECTED,
    DISCONNECTED
}