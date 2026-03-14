package org.intelehealth.abdm.utils

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object AbdmManager {

    @JvmStatic
    var baseUrl: String = ""

    @JvmStatic
    var encoded: String = ""

    @JvmStatic
    val checkExistingUserUrl: String
        get() = "$baseUrl/EMR-Middleware/webapi/check/id/"

    @JvmStatic
    var locationUuid: String = ""

    @JvmStatic
    var isCommunicationNumberUsed: Boolean = false

    @JvmStatic
    var isPreferredAddressSet: Boolean = false

    @JvmStatic
    var hwFullName: String = ""

    @JvmStatic
    var abhaLoginType: String = ""

    @JvmStatic
    var tempScope: String = ""

    @JvmStatic
    var isCreateAbha: Boolean = false

    @JvmStatic
    var dbClient: SQLiteDatabase? = null

}