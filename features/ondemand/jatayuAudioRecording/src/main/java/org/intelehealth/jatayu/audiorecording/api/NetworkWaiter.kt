package org.intelehealth.jatayu.audiorecording.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Suspends until the device has a working (validated) internet connection. */
internal class NetworkWaiter(context: Context) {

    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    fun isOnline(): Boolean = hasInternet(connectivity.getNetworkCapabilities(connectivity.activeNetwork))

    suspend fun awaitOnline() {
        if (isOnline()) return
        suspendCancellableCoroutine { cont ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    if (hasInternet(capabilities) && cont.isActive) {
                        connectivity.unregisterNetworkCallback(this)
                        cont.resume(Unit)
                    }
                }
            }
            connectivity.registerDefaultNetworkCallback(callback)
            cont.invokeOnCancellation { runCatching { connectivity.unregisterNetworkCallback(callback) } }
        }
    }

    private fun hasInternet(capabilities: NetworkCapabilities?): Boolean =
        capabilities != null &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
