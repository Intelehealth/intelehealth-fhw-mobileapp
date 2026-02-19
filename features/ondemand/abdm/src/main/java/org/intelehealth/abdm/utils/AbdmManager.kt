package org.intelehealth.abdm.utils

object AbdmManager {

    @JvmStatic
    var baseUrl: String = ""

    @JvmStatic
    var encoded: String = ""

    @JvmStatic
    val checkExistingUserUrl: String
        get() = "$baseUrl/EMR-Middleware/webapi/check/id/"

    @JvmStatic
    val locationUuid: String = ""

    @JvmStatic
    var isCommunicationNumberUsed: Boolean = false

    @JvmStatic
    var isPreferredAddressSet: Boolean = false

}