package org.intelehealth.app.optimized_sync.network

/**
 * Listener interface for real-time network connectivity events.
 *
 * Register via [NetworkConnectivityManager.addListener] and unregister via
 * [NetworkConnectivityManager.removeListener] to avoid leaks.
 * @author M N Surendra Reddy
 */
interface NetworkConnectivityListener {

    /**
     * Called when a network becomes available (first connect or reconnect).
     * @param status The current network status at the time of availability.
     */
    fun onNetworkAvailable(status: NetworkStatus)

    /**
     * Called when the active network is lost (disconnected).
     */
    fun onNetworkLost()

    /**
     * Called when the network is still connected but its capabilities have changed
     * (e.g. WiFi → Cellular, 4G → 5G, metered state change).
     * @param status The updated network status.
     */
    fun onNetworkChanged(status: NetworkStatus)
}
