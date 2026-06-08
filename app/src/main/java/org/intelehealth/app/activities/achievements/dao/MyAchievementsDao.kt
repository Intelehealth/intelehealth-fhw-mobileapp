package org.intelehealth.app.activities.achievements.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.database.InteleHealthDatabaseHelper
import org.intelehealth.app.utilities.UuidDictionary


class MyAchievementsDao(private val dbHelper: InteleHealthDatabaseHelper) {
    private  val TAG = "MyAchievementsDao"
   /* fun getTodaysDoctorVisitsCount(creatorUuid: String, visitAttributeType: String?, todaysDate: String): Int {

        val db = dbHelper.readDb
        var count = 0

        val queryBuilder = StringBuilder(""" SELECT COUNT(DISTINCT v.uuid) AS total FROM tbl_visit v LEFT JOIN tbl_visit_attribute attr ON v.uuid = attr.visit_uuid WHERE v.creator = ? AND substr(v.startdate, 1, 10) = ? AND v.sync IN (1, 'TRUE') COLLATE NOCASE """.trimIndent())
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
        return count
    }*/
   fun getTodaysDoctorVisitsCount(
       creatorUuid: String,
       visitAttributeType: String?,
       todaysDate: String
   ): Int {
       val db = dbHelper.readDb
       var count = 0

       val queryBuilder = StringBuilder(
           """
        SELECT COUNT(DISTINCT v.uuid) AS total 
        FROM tbl_visit v 
        LEFT JOIN tbl_visit_attribute attr 
               ON v.uuid = attr.visit_uuid
        LEFT JOIN tbl_visit_attribute speciality
               ON v.uuid = speciality.visit_uuid 
              AND speciality.visit_attribute_type_uuid = ?
        WHERE v.creator = ? 
          AND substr(v.startdate, 1, 10) = ? 
          AND v.sync IN (1, 'TRUE') COLLATE NOCASE
        """.trimIndent()
       )

       val args = mutableListOf(UuidDictionary.SPECIALITY, creatorUuid, todaysDate)

       if (!visitAttributeType.isNullOrEmpty()) {
           queryBuilder.append(" AND attr.visit_attribute_type_uuid != ?")
           args.add(visitAttributeType)
       }

       queryBuilder.append(" AND attr.value IS NOT NULL AND TRIM(attr.value) <> ''")

       queryBuilder.append(" AND (speciality.value IS NOT NULL OR speciality.value NOT LIKE '%doctor%' COLLATE NOCASE)")

       // 🔍 Log query for debugging
       var finalQuery = queryBuilder.toString()
       args.forEach { arg ->
           finalQuery = finalQuery.replaceFirst("?", "'$arg'")
       }
       Log.d("DB_QUERY", "Query todays doc: $finalQuery")
       val cursor = db.rawQuery(queryBuilder.toString(), args.toTypedArray())
       cursor?.use {
           if (it.moveToFirst()) {
               count = it.getInt(it.getColumnIndexOrThrow("total"))
           }
       }
       return count
   }


  /*  fun getTodaysNCDVisitsCount(creatorUuid: String, visitAttributeType: String?, todaysDate: String): Int {
        val db = dbHelper.readDb
        var count = 0

        val queryBuilder = StringBuilder("""SELECT COUNT(DISTINCT v.uuid) AS total FROM tbl_visit v LEFT JOIN tbl_visit_attribute attr ON v.uuid = attr.visit_uuid WHERE v.creator = ? AND substr(v.startdate, 1, 10) = ? AND v.sync IN (1, 'TRUE') COLLATE NOCASE """.trimIndent())
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
        return count
    }*/


    fun getPatientsRegisteredTodayByLoggedInHw(creatorUuidValue: String, todaysDate: String): Int {
        val db = dbHelper.readDb
        var count = 0
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

        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        return count
    }


    fun getHWTodaysActiveStatus(creatorUuid: String, todaysDate: String): Boolean {
        val db = dbHelper.readDb
        val query = """SELECT COUNT(DISTINCT v.uuid) AS total FROM tbl_visit v WHERE v.creator = ? AND v.sync IN (1, 'TRUE') COLLATE NOCASE AND substr(v.startdate, 1, 10) = ?"""
        db.rawQuery(query, arrayOf(creatorUuid, todaysDate)).use { cursor ->
            if (cursor.moveToFirst()) {
               val count = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
                return count > 0
            }
        }
        return false
    }

   /* fun getDoctorVisitsCountInGivenDateRange(
        creatorUuid: String,
        visitAttributeType: String?,
        startDate: String,
        endDate: String
    ): Int {
        if (startDate.isBlank() || endDate.isBlank())
            return 0
        val db = dbHelper.readDb
        var count = 0

        val queryBuilder = StringBuilder(""" SELECT COUNT(DISTINCT v.uuid) AS total FROM tbl_visit v LEFT JOIN tbl_visit_attribute attr ON v.uuid = attr.visit_uuid WHERE v.creator = ? AND substr(v.startdate, 1, 10) BETWEEN ? AND ? AND v.sync IN (1, 'TRUE') COLLATE NOCASE """.trimIndent())

        val args = mutableListOf(creatorUuid, startDate, endDate)

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
        return count
    }

*/
   /* fun getNCDVisitsCountInDateRange(
        creatorUuid: String,
        visitAttributeType: String,
        startDate: String,
        endDate: String
    ): Int {
        val db = dbHelper.readDb
        var count = 0
        val query = """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        LEFT JOIN tbl_visit_attribute va ON v.uuid = va.visit_uuid
        WHERE v.creator = ? 
        AND DATE(substr(v.startdate, 1, 10)) BETWEEN DATE(?) AND DATE(?)
        AND v.sync IN (1, 'TRUE') COLLATE NOCASE
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
*/
    fun getPatientsRegisteredByLoggedInHwInDateRange(
        creatorUuidValue: String,
        startDate: String,
        endDate: String
    ): Int {
        val db = dbHelper.readDb
        var count = 0
        val convertedAttrDateValue = buildSqlDateConversionExpr("attr_date.value")

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
          AND $convertedAttrDateValue BETWEEN ? AND ?
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

        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        return count
    }

    fun getHWActiveStatusInDateRange(creatorUuid: String, startDate: String, endDate: String): Int {

        val db = dbHelper.readDb
        var count = 0
        val query = """
        SELECT COUNT(DISTINCT substr(v.startdate, 1, 10)) AS total
        FROM tbl_visit v
        WHERE v.creator = ?
          AND (v.sync = 1 OR v.sync = 'TRUE')
          AND substr(v.startdate, 1, 10) BETWEEN ? AND ?
    """

        db.rawQuery(query, arrayOf(creatorUuid, startDate, endDate)).use { cursor ->
            if (cursor.moveToFirst()) {
                count = cursor.getInt(cursor.getColumnIndexOrThrow("total"))
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
        val db = dbHelper.readDb
        var count = 0
        val convertedAttrDateValue = buildSqlDateConversionExpr("attr1.value")

        val query = """
        SELECT COUNT(DISTINCT p.uuid) AS total
        FROM tbl_patient p
        JOIN tbl_patient_attribute attr 
            ON p.uuid = attr.patientuuid
        JOIN tbl_patient_attribute_master master 
            ON attr.person_attribute_type_uuid = master.uuid
        WHERE master.name = 'householdID'
          AND attr.value IS NOT NULL
          AND TRIM(attr.value) <> ''
          AND p.uuid IN (
              SELECT patientuuid
              FROM tbl_patient_attribute attr1
              JOIN tbl_patient_attribute_master master1 
                  ON attr1.person_attribute_type_uuid = master1.uuid
              WHERE master1.name = ?
                AND $convertedAttrDateValue BETWEEN ? AND ?
          )
          AND p.uuid IN (
              SELECT patientuuid
              FROM tbl_patient_attribute attr2
              JOIN tbl_patient_attribute_master master2 
                  ON attr2.person_attribute_type_uuid = master2.uuid
              WHERE master2.name = ?
                AND attr2.value = ?
          )
    """.trimIndent()

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
            }
        }

        return count
    }

    fun buildSqlDateConversionExpr(dateColumn: String): String {
        return """
        strftime('%Y-%m-%d',
            substr($dateColumn, instr($dateColumn, ',') + 2, 4) || '-' ||
            CASE
                WHEN instr($dateColumn, 'January') > 0 THEN '01'
                WHEN instr($dateColumn, 'February') > 0 THEN '02'
                WHEN instr($dateColumn, 'March') > 0 THEN '03'
                WHEN instr($dateColumn, 'April') > 0 THEN '04'
                WHEN instr($dateColumn, 'May') > 0 THEN '05'
                WHEN instr($dateColumn, 'June') > 0 THEN '06'
                WHEN instr($dateColumn, 'July') > 0 THEN '07'
                WHEN instr($dateColumn, 'August') > 0 THEN '08'
                WHEN instr($dateColumn, 'September') > 0 THEN '09'
                WHEN instr($dateColumn, 'October') > 0 THEN '10'
                WHEN instr($dateColumn, 'November') > 0 THEN '11'
                WHEN instr($dateColumn, 'December') > 0 THEN '12'
            END || '-' ||
            substr($dateColumn, 1, instr($dateColumn, ' ') - 1)
        )
    """.trimIndent()
    }

    fun getBaselineSurveyRegisteredTodaysPatients(creatorUuid: String, todaysDate: String): Int {
        val db = dbHelper.readDb
        var count = 0

        val query = """
        SELECT COUNT(DISTINCT p.uuid) AS total
        FROM tbl_patient p
        JOIN tbl_patient_attribute attr_house
            ON attr_house.patientuuid = p.uuid
        JOIN tbl_patient_attribute_master master_house
            ON master_house.uuid = attr_house.person_attribute_type_uuid
        JOIN tbl_patient_attribute attr_date
            ON attr_date.patientuuid = p.uuid
        JOIN tbl_patient_attribute_master master_date
            ON master_date.uuid = attr_date.person_attribute_type_uuid
        JOIN tbl_patient_attribute attr_provider
            ON attr_provider.patientuuid = p.uuid
        JOIN tbl_patient_attribute_master master_provider
            ON master_provider.uuid = attr_provider.person_attribute_type_uuid
        WHERE LOWER(TRIM(master_house.name)) = LOWER('householdID')
          AND attr_house.value IS NOT NULL
          AND TRIM(attr_house.value) <> ''
          AND LOWER(TRIM(master_date.name)) = LOWER(?)
          AND attr_date.value = ?
          AND LOWER(TRIM(master_provider.name)) = LOWER(?)
          AND attr_provider.value = ?
    """.trimIndent()

        val cursor = db.rawQuery(
            query,
            arrayOf(
                UuidDictionary.ATTRIBUTE_TYPE_DATE_CREATED_NAME, // createdDate
                todaysDate,
                UuidDictionary.ATTRIBUTE_TYPE_PROVIDER_ID_NAME,  // providerUUID
                creatorUuid
            )
        )
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }

        return count
    }
    fun getTodaysNCDVisitsCount(
        creatorUuid: String,
        visitAttributeType: String?,
        todaysDate: String
    ): Int {
        val db = dbHelper.readDb
        var count = 0

        val queryBuilder = StringBuilder(
            """
        SELECT COUNT(DISTINCT v.uuid) AS total 
        FROM tbl_visit v 
        LEFT JOIN tbl_visit_attribute attr 
               ON v.uuid = attr.visit_uuid
        LEFT JOIN tbl_visit_attribute speciality 
               ON v.uuid = speciality.visit_uuid 
              AND speciality.visit_attribute_type_uuid = ?
        WHERE v.creator = ? 
          AND substr(v.startdate, 1, 10) = ? 
          AND v.sync IN (1, 'TRUE') COLLATE NOCASE
        """.trimIndent()
        )

        val args = mutableListOf(UuidDictionary.SPECIALITY, creatorUuid, todaysDate)

        if (!visitAttributeType.isNullOrEmpty()) {
            queryBuilder.append(
                """
            AND (
                (
                  attr.visit_attribute_type_uuid = ?
                  AND attr.value IS NOT NULL 
                  AND TRIM(attr.value) <> ''
                )
                OR (
                  speciality.value IS NOT NULL 
                  AND speciality.value LIKE '%doctor%' COLLATE NOCASE
                )
            )
            """
            )
            args.add(visitAttributeType)
        } else {
            // If no attribute type is passed, only check for doctor in speciality
            queryBuilder.append(
                """
            AND (
                speciality.value IS NOT NULL 
                AND speciality.value LIKE '%doctor%' COLLATE NOCASE
            )
            """
            )
        }

        // 🔍 Log query for debugging
        var finalQuery = queryBuilder.toString()
        args.forEach { arg ->
            finalQuery = finalQuery.replaceFirst("?", "'$arg'")
        }
        Log.d("DB_QUERY", "Query today ncd count : $finalQuery")

        val cursor = db.rawQuery(queryBuilder.toString(), args.toTypedArray())
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
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

        val db = dbHelper.readDb
        var count = 0

        val queryBuilder = StringBuilder(
            """
        SELECT COUNT(DISTINCT v.uuid) AS total 
        FROM tbl_visit v 
        LEFT JOIN tbl_visit_attribute attr 
               ON v.uuid = attr.visit_uuid
        LEFT JOIN tbl_visit_attribute speciality 
               ON v.uuid = speciality.visit_uuid 
              AND speciality.visit_attribute_type_uuid = '3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d'
        WHERE v.creator = ? 
          AND substr(v.startdate, 1, 10) BETWEEN ? AND ? 
          AND v.sync IN (1, 'TRUE') COLLATE NOCASE
        """.trimIndent()
        )

        val args = mutableListOf(creatorUuid, startDate, endDate)

        if (!visitAttributeType.isNullOrEmpty()) {
            queryBuilder.append(" AND attr.visit_attribute_type_uuid != ?")
            args.add(visitAttributeType)
        }

        queryBuilder.append(" AND attr.value IS NOT NULL AND TRIM(attr.value) <> ''")

        queryBuilder.append(" AND (speciality.value IS NOT NULL OR speciality.value NOT LIKE '%doctor%' COLLATE NOCASE)")

        // 🔍 Log query for debugging
        var finalQuery = queryBuilder.toString()
        args.forEach { arg ->
            finalQuery = finalQuery.replaceFirst("?", "'$arg'")
        }
        Log.d("DB_QUERY", "Query doctor date range: $finalQuery")

        val cursor = db.rawQuery(queryBuilder.toString(), args.toTypedArray())
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }
        return count
    }
    fun getNCDVisitsCountInDateRange(
        creatorUuid: String,
        visitAttributeType: String,
        startDate: String,
        endDate: String
    ): Int {
        val db = dbHelper.readDb
        var count = 0
        val query = """
        SELECT COUNT(DISTINCT v.uuid) AS total
        FROM tbl_visit v
        LEFT JOIN tbl_visit_attribute va ON v.uuid = va.visit_uuid
        LEFT JOIN tbl_visit_attribute speciality 
               ON v.uuid = speciality.visit_uuid 
              AND speciality.visit_attribute_type_uuid = ?
        WHERE v.creator = ? 
          AND DATE(substr(v.startdate, 1, 10)) BETWEEN DATE(?) AND DATE(?)
          AND v.sync IN (1, 'TRUE') COLLATE NOCASE
          AND (
              (va.visit_attribute_type_uuid = ? OR va.visit_attribute_type_uuid IS NULL)
              OR (speciality.value IS NOT NULL AND speciality.value LIKE '%doctor%' COLLATE NOCASE)
          )
    """.trimIndent()

        Log.d("DB_QUERY", "Query ncd date range: $query")

        val cursor = db.rawQuery(
            query,
            arrayOf(
                UuidDictionary.SPECIALITY, // binds to the speciality.visit_attribute_type_uuid in the LEFT JOIN
                creatorUuid,
                startDate,
                endDate,
                visitAttributeType      // binds to va.visit_attribute_type_uuid in the WHERE clause
            )
        )
        cursor?.use {
            if (it.moveToFirst()) {
                count = it.getInt(it.getColumnIndexOrThrow("total"))
            }
        }
        return count
    }

}