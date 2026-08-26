package org.intelehealth.app.optimized_sync.network

/**
 * Represents the type of network currently in use.
 *
 * Cellular generations are classified as:
 *  - 2G: GPRS, EDGE, CDMA, 1xRTT, IDEN, GSM
 *  - 3G: UMTS, EVDO, HSDPA, HSUPA, HSPA, HSPAP, TD-SCDMA, eHRPD
 *  - 4G: LTE, IWLAN
 *  - 5G NSA: 5G Non-Standalone (LTE anchor + NR secondary, API 30+)
 *  - 5G SA:  5G Standalone NR (NETWORK_TYPE_NR, API 29+)
 *  @author M N Surendra Reddy
 */
enum class NetworkType(val displayName: String) {
    WIFI("WiFi"),
    CELLULAR_2G("2G"),
    CELLULAR_3G("3G"),
    CELLULAR_4G("4G (LTE)"),
    CELLULAR_5G_NSA("5G NSA"),
    CELLULAR_5G_SA("5G"),
    ETHERNET("Ethernet"),
    NO_CONNECTION("No Connection"),
    UNKNOWN("Unknown");

    val isCellular: Boolean
        get() = this in setOf(CELLULAR_2G, CELLULAR_3G, CELLULAR_4G, CELLULAR_5G_NSA, CELLULAR_5G_SA)

    val is5G: Boolean
        get() = this == CELLULAR_5G_NSA || this == CELLULAR_5G_SA
}
