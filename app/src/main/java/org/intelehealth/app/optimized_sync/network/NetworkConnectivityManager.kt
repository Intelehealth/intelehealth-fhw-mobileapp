package org.intelehealth.app.optimized_sync.network

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.util.concurrent.CopyOnWriteArraySet
import android.telephony.TelephonyCallback
import android.telephony.TelephonyCallback.DisplayInfoListener

/**
 * Monitors network connectivity and cellular generation (2G / 3G / 4G / 5G NSA / 5G SA).
 *
 * ## Usage
 * ```kotlin
 * val manager = NetworkConnectivityManager(context)
 * manager.addListener(myListener)
 * manager.startListening()          // typically in onStart / onResume
 * // …
 * manager.stopListening()           // typically in onStop / onPause
 * manager.removeListener(myListener)
 * ```
 *
 * ## Required permissions (declare in AndroidManifest.xml)
 * - `android.permission.ACCESS_NETWORK_STATE`   — mandatory
 * - `android.permission.ACCESS_WIFI_STATE`       — mandatory
 * - `android.permission.READ_PHONE_STATE`        — optional; enables precise cellular-generation
 *                                                   detection on API 30+. Falls back to UNKNOWN
 *                                                   if not granted.
 *
 * ## Thread safety
 * [NetworkConnectivityListener] callbacks are delivered on the thread that called
 * [startListening] (typically the main thread, because that is what
 * [ConnectivityManager.registerNetworkCallback] uses by default when no Handler is supplied).
 * @author M N Surendra Reddy
 */
class NetworkConnectivityManager(private val context: Context) {

    // -------------------------------------------------------------------------
    // System services
    // -------------------------------------------------------------------------

    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Thread-safe set of registered listeners. */
    private val listeners = CopyOnWriteArraySet<NetworkConnectivityListener>()

    private var isRegistered = false

    /** Last known status — starts as DISCONNECTED until [startListening] is called. */
    @Volatile
    private var currentStatus: NetworkStatus = NetworkStatus.DISCONNECTED

    // -------------------------------------------------------------------------
    // Debounce handling for network transitions
    // -------------------------------------------------------------------------

    private var lostRunnable: Runnable? = null

    // -------------------------------------------------------------------------
    // 5G NSA detection via TelephonyDisplayInfo (API 30+)
    // -------------------------------------------------------------------------

    /**
     * On API 30+ the radio may show LTE as the base network type while the device is
     * on a 5G Non-Standalone (NSA) beam.  [TelephonyDisplayInfo.overrideNetworkType]
     * exposes this override and is the only reliable way to detect NSA 5G.
     *
     * We track the last seen override here so that [buildNetworkStatus] can use it when
     * the transport is CELLULAR.
     */
    @Volatile
    private var nsa5GOverride: Boolean = false

    /**
     * Holds the API 31+ TelephonyCallback instance. Created lazily inside
     * [registerDisplayInfoListener] so that [TelephonyCallback] (API 31) is only
     * referenced after the version check — avoids NoClassDefFoundError on older devices.
     */
    private var displayInfoCallback: Any? = null

    @Suppress("DEPRECATION")
    private var legacyPhoneStateListener: PhoneStateListener? = null

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleDisplayInfo(info: TelephonyDisplayInfo) {
        val wasNsa5G = nsa5GOverride
        nsa5GOverride = when (info.overrideNetworkType) {
            TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> true
            else -> {
                // API 33+: OVERRIDE_NETWORK_TYPE_NR_ADVANCED covers mmWave / advanced 5G
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    info.overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
                } else {
                    false
                }
            }
        }

        // Re-broadcast a change if the 5G NSA flag flipped while we are connected
        if (wasNsa5G != nsa5GOverride && currentStatus.isConnected) {
            val updated = currentStatus.copy(networkType = resolveCellularType())
            currentStatus = updated
            notifyNetworkChanged(updated)
        }
    }

    // -------------------------------------------------------------------------
    // ConnectivityManager.NetworkCallback
    // -------------------------------------------------------------------------

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            // We wait for onCapabilitiesChanged which fires immediately after onAvailable
            // with a fully-populated NetworkCapabilities object.
        }

        override fun onLost(network: Network) {
            // DO NOT immediately mark disconnected.
            // Let capabilities + debounce decide the real state.
            triggerDelayedLostCheck()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            val previous = currentStatus
            val updated = buildNetworkStatus(networkCapabilities)

            if (updated.isConnected) {
                lostRunnable?.let {
                    mainHandlerDispatch.removeCallbacks(it)
                    lostRunnable = null
                }
            }

            currentStatus = updated

            when {
                // Offline → Online (real internet restored)
                !previous.hasInternet && updated.hasInternet -> {
                    notifyNetworkAvailable(updated)
                }

                // Online → Online but type changed (WiFi ↔ Data)
                previous.hasInternet && updated.hasInternet && previous != updated -> {
                    notifyNetworkChanged(updated)
                }

                // Online → No Internet (start debounce)
                previous.hasInternet && !updated.hasInternet -> {
                    triggerDelayedLostCheck()
                }

                // Already offline → still offline → ignore noise
            }
        }
    }

    private fun triggerDelayedLostCheck() {
        lostRunnable?.let { mainHandlerDispatch.removeCallbacks(it) }

        lostRunnable = Runnable {
            val latest = fetchCurrentStatus()

            if (!latest.hasInternet && currentStatus.hasInternet) {
                currentStatus = NetworkStatus.DISCONNECTED
                notifyNetworkLost()
            } else {
                // Network recovered or was never truly lost
                currentStatus = latest
            }

            lostRunnable = null
        }

        mainHandlerDispatch.postDelayed(lostRunnable!!, 800)
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Register a [NetworkConnectivityListener].  Safe to call before [startListening].
     */
    fun addListener(listener: NetworkConnectivityListener) {
        listeners.add(listener)
    }

    /** Unregister a previously registered [NetworkConnectivityListener]. */
    fun removeListener(listener: NetworkConnectivityListener) {
        listeners.remove(listener)
    }

    /**
     * Begin monitoring.  Idempotent — safe to call multiple times.
     *
     * Requires [Manifest.permission.ACCESS_NETWORK_STATE].
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun startListening() {
        if (isRegistered) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
        isRegistered = true

        // Seed the current status so callers can query it synchronously.
        currentStatus = fetchCurrentStatus()

        // Register for 5G NSA display-info overrides when available.
        registerDisplayInfoListener()
    }

    /**
     * Stop monitoring.  Idempotent — safe to call multiple times.
     */
    fun stopListening() {
        if (!isRegistered) return
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: IllegalArgumentException) {
            // Callback was already unregistered — nothing to do.
        }
        unregisterDisplayInfoListener()
        isRegistered = false
    }

    /**
     * Returns the current [NetworkStatus] by querying [ConnectivityManager] directly.
     * Does not depend on [startListening] having been called.
     *
     * Requires [Manifest.permission.ACCESS_NETWORK_STATE].
     */
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun getCurrentStatus(): NetworkStatus = fetchCurrentStatus()

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun fetchCurrentStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork
            ?: return NetworkStatus.DISCONNECTED
        val caps = connectivityManager.getNetworkCapabilities(network)
            ?: return NetworkStatus.DISCONNECTED
        return buildNetworkStatus(caps)
    }

    private fun buildNetworkStatus(caps: NetworkCapabilities): NetworkStatus {
        val isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val hasInternet = isConnected &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        val networkType = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> resolveCellularType()
            else -> NetworkType.UNKNOWN
        }

        return NetworkStatus(
            isConnected = isConnected,
            hasInternet = hasInternet,
            networkType = networkType,
            isMetered = isMetered,
        )
    }

    /**
     * Determines the cellular generation.
     *
     * Priority:
     * 1. 5G NSA override from [TelephonyDisplayInfo] (API 30+)
     * 2. [TelephonyManager.dataNetworkType] mapped to a generation
     * 3. [NetworkType.UNKNOWN] if [Manifest.permission.READ_PHONE_STATE] is not granted
     */
    private fun resolveCellularType(): NetworkType {
        // 5G NSA is flagged by the display-info override, not dataNetworkType.
        if (nsa5GOverride) return NetworkType.CELLULAR_5G_NSA

        return try {
            val type = getRawNetworkType()
            mapToGeneration(type)
        } catch (_: SecurityException) {
            // READ_PHONE_STATE not granted — cannot determine cellular generation.
            NetworkType.UNKNOWN
        }
    }

    @SuppressLint("MissingPermission")
    private fun getRawNetworkType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            telephonyManager.dataNetworkType
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.networkType
        }

    private fun mapToGeneration(networkType: Int): NetworkType = when (networkType) {

        // ── 2G ───────────────────────────────────────────────────────────────
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN,
        TelephonyManager.NETWORK_TYPE_GSM,
            -> NetworkType.CELLULAR_2G

        // ── 3G ───────────────────────────────────────────────────────────────
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA,
            -> NetworkType.CELLULAR_3G

        // ── 4G ───────────────────────────────────────────────────────────────
        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN,
            -> NetworkType.CELLULAR_4G

        // ── 5G SA (Standalone NR) — API 29+ ──────────────────────────────────
        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            networkType == TelephonyManager.NETWORK_TYPE_NR
        ) {
            NetworkType.CELLULAR_5G_SA
        } else {
            NetworkType.UNKNOWN
        }
    }

    // -------------------------------------------------------------------------
    // 5G NSA: TelephonyDisplayInfo registration (API 30+)
    // -------------------------------------------------------------------------

    @SuppressLint("MissingPermission")
    private fun registerDisplayInfoListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (displayInfoCallback == null) {
                    displayInfoCallback = object : TelephonyCallback(), DisplayInfoListener {
                        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                            handleDisplayInfo(telephonyDisplayInfo)
                        }
                    }
                }
                telephonyManager.registerTelephonyCallback(
                    context.mainExecutor,
                    displayInfoCallback as TelephonyCallback,
                )
            } catch (_: SecurityException) {
                // READ_PHONE_STATE not granted — 5G NSA detection unavailable.
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                if (legacyPhoneStateListener == null) {
                    legacyPhoneStateListener = object : PhoneStateListener() {
                        @RequiresApi(Build.VERSION_CODES.R)
                        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                            handleDisplayInfo(telephonyDisplayInfo)
                        }
                    }
                }
                @Suppress("DEPRECATION")
                telephonyManager.listen(
                    legacyPhoneStateListener,
                    PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED,
                )
            } catch (_: SecurityException) {
                // READ_PHONE_STATE not granted — 5G NSA detection unavailable.
            }
        }
    }

    private fun unregisterDisplayInfoListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                (displayInfoCallback as? TelephonyCallback)?.let {
                    telephonyManager.unregisterTelephonyCallback(it)
                }
            } catch (_: Exception) { /* ignore */
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                legacyPhoneStateListener?.let {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
                }
            } catch (_: Exception) { /* ignore */
            }
        }
        nsa5GOverride = false
    }

    // -------------------------------------------------------------------------
    // Listener dispatch
    // -------------------------------------------------------------------------

    private val mainHandlerDispatch = Handler(Looper.getMainLooper())

    private fun notifyNetworkAvailable(status: NetworkStatus) {
        mainHandlerDispatch.post { listeners.forEach { it.onNetworkAvailable(status) } }
    }

    private fun notifyNetworkLost() {
        mainHandlerDispatch.post { listeners.forEach { it.onNetworkLost() } }
    }

    private fun notifyNetworkChanged(status: NetworkStatus) {
        mainHandlerDispatch.post { listeners.forEach { it.onNetworkChanged(status) } }
    }

}
