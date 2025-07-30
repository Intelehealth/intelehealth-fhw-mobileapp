package org.intelehealth.app.activities.achievements.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.InteleHealthDatabaseHelper
import org.intelehealth.app.utilities.UuidDictionary


class MyAchievementsDao(private val dbHelper: InteleHealthDatabaseHelper) {
    private  val TAG = "MyAchievementsDao"
    fun getTodaysDoctorVisitsCount(creatorUuid: String, visitAttributeType: String?, todaysDate: String): Int {
        Log.d(TAG, "creatorUuid: $creatorUuid")
        Log.d(TAG, "visitAttributeType: $visitAttributeType")
        Log.d(TAG, "todaysDate: $todaysDate")

        val db = dbHelper.readableDatabase
        var count = 0

        val queryBuilder = StringBuilder(
            """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        LEFT JOIN tbl_visit_attribute attr ON v.uuid = attr.visit_uuid
        WHERE v.creator = ?
        AND substr(v.startdate, 1, 10) = ?
    """.trimIndent()
        )

        val args = mutableListOf(creatorUuid, todaysDate)

        if (!visitAttributeType.isNullOrEmpty()) {
            queryBuilder.append(" AND attr.visit_attribute_type_uuid != ?")
            args.add(visitAttributeType)
        }

        queryBuilder.append(" AND attr.value IS NOT NULL AND TRIM(attr.value) <> ''")

        val cursor = db.rawQuery(queryBuilder.toString(), args.toTypedArray())
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        Log.d(TAG, "getTodaysNCDVisitsCount: count: $count")
        return count
    }

    fun getTodaysNCDVisitsCount(creatorUuid: String, visitAttributeType: String?, todaysDate: String): Int {
        Log.d(TAG, "creatorUuid: $creatorUuid")
        Log.d(TAG, "visitAttributeType: $visitAttributeType")
        Log.d(TAG, "todaysDate: $todaysDate")

        val db = dbHelper.readableDatabase
        var count = 0

        val queryBuilder = StringBuilder(
            """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        LEFT JOIN tbl_visit_attribute attr ON v.uuid = attr.visit_uuid
        WHERE v.creator = ?
        AND substr(v.startdate, 1, 10) = ?
    """.trimIndent()
        )

        val args = mutableListOf<String>(creatorUuid, todaysDate)

        if (!visitAttributeType.isNullOrEmpty()) {
            queryBuilder.append(" AND attr.visit_attribute_type_uuid = ?")
            args.add(visitAttributeType)
        }

        queryBuilder.append(" AND attr.value IS NOT NULL AND TRIM(attr.value) <> ''")

        val cursor = db.rawQuery(queryBuilder.toString(), args.toTypedArray())
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        Log.d(TAG, "getTodaysNCDVisitsCount: count: $count")
        return count
    }

    fun getPatientsRegisteredTodayByLoggedInHw(creatorUuidValue: String, todaysDate: String): Int {
        val db = dbHelper.readableDatabase
        var count = 0

        Log.d(TAG, "getPatientsRegisteredTodayByLoggedInHw: creatorUuid = $creatorUuidValue")
        Log.d(TAG, "getPatientsRegisteredTodayByLoggedInHw: createdDate = $todaysDate")

        val query = """
    SELECT COUNT(DISTINCT p.uuid) AS total
    FROM tbl_patient p
    JOIN tbl_patient_attribute attr_date
        ON p.uuid = attr_date.patientuuid
    JOIN tbl_patient_attribute_master master_date
        ON attr_date.person_attribute_type_uuid = master_date.uuid
    JOIN tbl_patient_attribute attr_creator
        ON p.uuid = attr_creator.patientuuid
    JOIN tbl_patient_attribute_master master_creator
        ON attr_creator.person_attribute_type_uuid = master_creator.uuid
          WHERE master_date.name = ?
    AND attr_date.value = ?
    AND master_creator.name =?
    AND attr_creator.value =?
""".trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME, // "Date Created"
                todaysDate,
                UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME,  // "Provider ID"
                creatorUuidValue
            )
        )
        Log.d(TAG, "kkcckxecuting getPatientsRegisteredTodayByLoggedInHw query:\n$query")
        Log.d(TAG, "kkcckWith getPatientsRegisteredTodayByLoggedInHw params: ${UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME}, $todaysDate, ${UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME}, $creatorUuidValue")

        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        return count
    }


    fun getHWTodaysActiveStatus(creatorUuid: String, todaysDate: String): Boolean {
        Log.d(TAG, "getHWTodaysActiveStatus: creatorUuid : "+creatorUuid)
        val db = dbHelper.readableDatabase
        val query = """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        WHERE v.creator = ? 
        AND v.sync IN (1, 'TRUE') COLLATE NOCASE
        AND substr(v.startdate, 1, 10) = ?
    """
        db.rawQuery(query, arrayOf(creatorUuid, todaysDate)).use { cursor ->
            if (cursor.moveToFirst()) {
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
                Log.d(TAG, "getHWTodaysActiveStatus: count : "+count)

                return count > 0
            }
        }
        return false
    }

    fun getBaselineSurveyRegisteredTodaysPatients(creatorUuid: String, todaysDate: String): Int {
        Log.d(TAG, "kkachi getBaselineSurveyRegisteredTodaysPatients: creatorUuid11111 : "+creatorUuid)
        Log.d(TAG, "kkachi getBaselineSurveyRegisteredTodaysPatients: todaysDate111 : "+todaysDate)
        val db = dbHelper.readableDatabase
        var count = 0
        val query = """
    SELECT COUNT(DISTINCT p.uuid) AS total
    FROM tbl_patient p
    JOIN tbl_patient_attribute attr ON p.uuid = attr.patientuuid
    JOIN tbl_patient_attribute_master master ON attr.person_attribute_type_uuid = master.uuid
    WHERE master.name IN (
        'OCCUPATION', 'Caste', 'Education Level', 'ayushmanCardStatus',
        'mgnregaCardStatus', 'Bank Account', 'Mobile Phone Type',
        'Use WhatsApp', 'martialStatus'
    )
    AND attr.value IS NOT NULL
    AND TRIM(attr.value) != ''
    AND p.uuid IN (
        SELECT patientuuid
        FROM tbl_patient_attribute attr1
        JOIN tbl_patient_attribute_master master1 ON attr1.person_attribute_type_uuid = master1.uuid
        WHERE master1.name = ?
        AND attr1.value = ?
    )
    AND p.uuid IN (
        SELECT patientuuid
        FROM tbl_patient_attribute attr2
        JOIN tbl_patient_attribute_master master2 ON attr2.person_attribute_type_uuid = master2.uuid
        WHERE master2.name = ?
        AND attr2.value = ?
    )
    """.trimIndent()
        Log.d(TAG, "kkcckxecuting query:\n$query")
        Log.d(TAG, "kkcckWith params: ${UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME}, $todaysDate, ${UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME}, $creatorUuid")

        val cursor = db.rawQuery(
            query,
            arrayOf(
                UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME, // "Date Created"
                todaysDate,
                UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME,  // "Provider ID"
                creatorUuid
            )
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
                Log.d(TAG, "getBaselineSurveyRegisteredTodaysPatients: count : "+count)
            }
            cursor.close()
        }
        return count

    }

    fun getDoctorVisitsCountInGivenDateRange(
        creatorUuid: String,
        visitAttributeType: String?,
        startDate: String,
        endDate: String
    ): Int {
        if (startDate.isBlank() || endDate.isBlank())
            return 0

        Log.d(TAG, "creatorUuid: $creatorUuid")
        Log.d(TAG, "visitAttributeType: $visitAttributeType")
        Log.d(TAG, "startDate: $startDate")
        Log.d(TAG, "endDate: $endDate")

        val db = dbHelper.readableDatabase
        var count = 0

        val queryBuilder = StringBuilder(
            """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        LEFT JOIN tbl_visit_attribute attr ON v.uuid = attr.visit_uuid
        WHERE v.creator = ?
          AND substr(v.startdate, 1, 10) BETWEEN ? AND ?
    """.trimIndent()
        )

        val args = mutableListOf(creatorUuid, startDate, endDate)

        if (!visitAttributeType.isNullOrEmpty()) {
            queryBuilder.append(" AND attr.visit_attribute_type_uuid != ?")
            args.add(visitAttributeType)
        }

        queryBuilder.append(" AND attr.value IS NOT NULL AND TRIM(attr.value) <> ''")

        Log.d(TAG, "kkkkkcExecuting getDoctorVisitsCountInGivenDateRange query:\n$queryBuilder.toString()")
        Log.d(TAG, "kkkkkcWith params: getDoctorVisitsCountInGivenDateRange ${UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME}, $startDate, $endDate, ${UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME}, $creatorUuid")

        val cursor = db.rawQuery(queryBuilder.toString(), args.toTypedArray())
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        Log.d(TAG, "getDoctorVisitsCountBetweenDates: count: $count")
        return count
    }


    fun getNCDVisitsCountInDateRange(
        creatorUuid: String,
        visitAttributeType: String,
        startDate: String,
        endDate: String
    ): Int {
        Log.d(TAG, "getNCDVisitsCountInDateRange: creatorUuid : $creatorUuid")
        Log.d(TAG, "getNCDVisitsCountInDateRange: visitAttributeType : $visitAttributeType")
        Log.d(TAG, "getNCDVisitsCountInDateRange: startDate : $startDate")
        Log.d(TAG, "getNCDVisitsCountInDateRange: endDate : $endDate")

        val db = dbHelper.readableDatabase
        var count = 0
        val query = """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        LEFT JOIN tbl_visit_attribute va ON v.uuid = va.visit_uuid
        WHERE v.creator = ? 
        AND DATE(substr(v.startdate, 1, 10)) BETWEEN DATE(?) AND DATE(?)
        AND (va.visit_attribute_type_uuid = ? OR va.visit_attribute_type_uuid IS NULL)
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(creatorUuid, startDate, endDate, visitAttributeType))
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }
        return count
    }

    fun getPatientsRegisteredByLoggedInHwInDateRange(
        creatorUuidValue: String,
        startDate: String,
        endDate: String
    ): Int {
        val db = dbHelper.readableDatabase
        var count = 0

        Log.d(TAG, "getPatientsRegisteredBetweenDatesByLoggedInHw: creatorUuid = $creatorUuidValue")
        Log.d(TAG, "Start Date = $startDate, End Date = $endDate")

        val query = """
        SELECT COUNT(DISTINCT p.uuid) AS total
        FROM tbl_patient p
        JOIN tbl_patient_attribute attr_date
            ON p.uuid = attr_date.patientuuid
        JOIN tbl_patient_attribute_master master_date
            ON attr_date.person_attribute_type_uuid = master_date.uuid
        JOIN tbl_patient_attribute attr_creator
            ON p.uuid = attr_creator.patientuuid
        JOIN tbl_patient_attribute_master master_creator
            ON attr_creator.person_attribute_type_uuid = master_creator.uuid
        WHERE master_date.name = ?
          AND attr_date.value BETWEEN ? AND ?
          AND master_creator.name = ?
          AND attr_creator.value = ?
        """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME,  // "Date Created"
                startDate,
                endDate,
                UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME,   // "Provider ID"
                creatorUuidValue
            )
        )

        Log.d(TAG, "kkkkkcExecuting getPatientsRegisteredBetweenDatesByLoggedInHw query:\n$query")
        Log.d(TAG, "kkkkkcWith params: ${UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME}, $startDate, $endDate, ${UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME}, $creatorUuidValue")

        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        return count
    }

    fun getHWActiveStatusInDateRange(creatorUuid: String, startDate: String, endDate: String): Int {
        Log.d(TAG, "getHWTodaysActiveStatus: creatorUuid : $creatorUuid, startDate: $startDate, endDate: $endDate")

        val db = dbHelper.readableDatabase
        var count = 0
       /* val query = """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        WHERE v.creator = ? 
          AND v.sync IN (1, 'TRUE') COLLATE NOCASE
          AND substr(v.startdate, 1, 10) BETWEEN ? AND ?
    """*/
        val query = """
        SELECT COUNT(DISTINCT substr(v.startdate, 1, 10)) AS total
        FROM tbl_visit v
        WHERE v.creator = ?
          AND (v.sync = 1 OR v.sync = 'TRUE')
          AND substr(v.startdate, 1, 10) BETWEEN ? AND ?
    """
        Log.d(TAG, "kkcckxecuting getPatientsRegisteredTodayByLoggedInHw query:\n$query")

        db.rawQuery(query, arrayOf(creatorUuid, startDate, endDate)).use { cursor ->
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
                Log.d(TAG, "getHWTodaysActiveStatus: count : $count")
               // return count > 0
            }
        }

        return count
    }
    fun getBaselineSurveyRegisteredPatientsInDateRange(
        creatorUuid: String,
        startDate: String,
        endDate: String
    ): Int {
        Log.d(TAG, "getBaselineSurveyRegisteredPatientsInDateRange: creatorUuid : $creatorUuid")
        Log.d(TAG, "getBaselineSurveyRegisteredPatientsInDateRange: startDate : $startDate")
        Log.d(TAG, "getBaselineSurveyRegisteredPatientsInDateRange: endDate : $endDate")

        val db = dbHelper.readableDatabase
        var count = 0

        val query = """
        SELECT COUNT(DISTINCT p.uuid) AS total
        FROM tbl_patient p
        JOIN tbl_patient_attribute attr ON p.uuid = attr.patientuuid
        JOIN tbl_patient_attribute_master master ON attr.person_attribute_type_uuid = master.uuid
        WHERE master.name IN (
            'OCCUPATION', 'Caste', 'Education Level', 'ayushmanCardStatus',
            'mgnregaCardStatus', 'Bank Account', 'Mobile Phone Type',
            'Use WhatsApp', 'martialStatus'
        )
        AND attr.value IS NOT NULL
        AND TRIM(attr.value) != ''
        AND p.uuid IN (
            SELECT patientuuid
            FROM tbl_patient_attribute attr1
            JOIN tbl_patient_attribute_master master1 ON attr1.person_attribute_type_uuid = master1.uuid
            WHERE master1.name = ?
            AND attr1.value BETWEEN ? AND ?
        )
        AND p.uuid IN (
            SELECT patientuuid
            FROM tbl_patient_attribute attr2
            JOIN tbl_patient_attribute_master master2 ON attr2.person_attribute_type_uuid = master2.uuid
            WHERE master2.name = ?
            AND attr2.value = ?
        )
    """.trimIndent()

        Log.d(TAG, "Executing query:\n$query")
        Log.d(TAG, "With params: ${UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME}, $startDate, $endDate, ${UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME}, $creatorUuid")

        val cursor = db.rawQuery(
            query,
            arrayOf(
                UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME, // "Date Created"
                startDate,
                endDate,
                UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME,  // "Provider ID"
                creatorUuid
            )
        )

        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
                Log.d(TAG, "getBaselineSurveyRegisteredPatientsInDateRange: count : $count")
            }
        }

        return count
    }


}