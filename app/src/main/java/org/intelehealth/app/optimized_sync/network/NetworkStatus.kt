package org.intelehealth.app.optimized_sync.network

/**
 * Snapshot of the current network connectivity state.
 *
 * @property isConnected  True when a network interface is available.
 * @property hasInternet  True when the connection has been validated against the internet
 *                        (i.e. NET_CAPABILITY_VALIDATED is present).
 * @property networkType  The transport/generation of the active network.
 * @property isMetered    True when the connection is metered (e.g. mobile data).
 * @author M N Surendra Reddy
 */
data class NetworkStatus(
    val isConnected: Boolean,
    val hasInternet: Boolean,
    val networkType: NetworkType,
    val isMetered: Boolean,
) {
    val displayName: String get() = networkType.displayName

    companion object {
        val DISCONNECTED = NetworkStatus(
            isConnected = false,
            hasInternet = false,
            networkType = NetworkType.NO_CONNECTION,
            isMetered = false,
        )
    }
}
