package org.intelehealth.app.user

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.app.database.InteleHealthDatabaseHelper
import org.intelehealth.app.utilities.DeviceUtils
import org.intelehealth.app.utilities.UuidDictionary
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserSessionDao(private val context: Context) {
    private val BATCH_SIZE = DeviceUtils.getOptimalBatchSize();
    private val dbHelper = InteleHealthDatabaseHelper(context)

    fun add(userSession: UserSession) {

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("userId", userSession.userId)
            put("startTime", userSession.startTime) // string in "yyyy-MM-dd HH:mm:ss"
            put("endTime", userSession.endTime)     // string in "yyyy-MM-dd HH:mm:ss"
            put("sessionDuration", userSession.sessionDuration) // in milliseconds as string
            put("sync", userSession.sync)

        }
        db.insert("tbl_user_session", null, values)
        db.close()
    }

    fun getUnsyncedSessions(): List<UserSession> {
        val unsyncedSessions = mutableListOf<UserSession>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tbl_user_session WHERE sync = ? OR sync = ? COLLATE NOCASE", arrayOf("0", "false"))
        if (cursor.moveToFirst()) {
            do {
                val session = UserSession(
                    userId = cursor.getString(cursor.getColumnIndexOrThrow("userId")),
                    startTime = cursor.getString(cursor.getColumnIndexOrThrow("startTime")),
                    endTime = cursor.getString(cursor.getColumnIndexOrThrow("endTime")),
                    sessionDuration = cursor.getString(cursor.getColumnIndexOrThrow("sessionDuration")),
                    sync = cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                    sessionId = cursor.getInt(cursor.getColumnIndexOrThrow("sessionId"))
                )
                unsyncedSessions.add(session)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return unsyncedSessions
    }
    fun updateSessionsSynced(sessionIds: List<Int>) {
        if (sessionIds.isEmpty()) return

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val sql = "UPDATE tbl_user_session SET sync = 1 WHERE sessionId = ?"
            val stmt = db.compileStatement(sql)
            for (id in sessionIds) {
                stmt.bindLong(1, id.toLong())
                stmt.executeUpdateDelete()
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
    }
    fun markSessionsAsSynced(sessionIds: List<Int?>) {
        val db = InteleHealthDatabaseHelper(context).writableDatabase
        val idsString = sessionIds.joinToString(",")
        db.execSQL("UPDATE tbl_user_session SET sync = 1 WHERE sessionId IN ($idsString)")
        db.close()
    }

    fun calculateEndTime(startTime: String, sessionDurationMillis: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // Parse the start time string to Date
        val startDate = dateFormat.parse(startTime) ?: return ""

        // Convert duration string to Long safely
        val duration = sessionDurationMillis.toLongOrNull() ?: return ""

        // Add duration to start time
        val endDate = Date(startDate.time + duration)

        // Return formatted end time
        return dateFormat.format(endDate)
    }

    /**
     * Parses and inserts user sessions from the provided attribute type list using batched inserts.
     * @param providerAttributeTypeList List of attribute types containing session data.
     * @throws Exception if any parsing or database operation fails.
     *
     */
    fun parseAndInsertSessionsV2(providerAttributeTypeList: List<Any>) {
        try {
            val sessionsToInsert = mutableListOf<UserSession>()

            for (item in providerAttributeTypeList) {

                val json = item as? Map<*, *> ?: continue

                val attributeTypeUuid = json["attributetypeuuid"] as? String ?: continue

                if (attributeTypeUuid != UuidDictionary.ATTRIBUTE_TYPE_USER_SESSION_TIME) {
                    continue
                }

                val providerId = json["provideruuid"] as? String ?: continue
                val valueJson = json["value"] as? String ?: continue

                val type = object : TypeToken<List<Map<String, String>>>() {}.type
                val sessionList: List<Map<String, String>> =
                    Gson().fromJson(valueJson, type)

                for (sessionMap in sessionList) {
                    val startTime = sessionMap["startTime"] ?: continue
                    val duration = sessionMap["sessionDuration"] ?: continue

                    val endTime = calculateEndTime(startTime, duration)

                    sessionsToInsert.add(
                        UserSession(
                            userId = providerId,
                            startTime = startTime,
                            endTime = endTime,
                            sessionDuration = duration,
                            sync = "1"
                        )
                    )
                }
            }

            // 🔥 Single batched DB insert
            insertSessionsBatch(sessionsToInsert)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Inserts a list of UserSession objects into the database in batches.
     * @param sessions List of UserSession objects to insert.
     * @throws Exception if any database operation fails.
     */
    fun insertSessionsBatch(sessions: List<UserSession>) {

        if (sessions.isEmpty()) return

        val db = dbHelper.writableDatabase
        val values = ContentValues()

        try {
            for (i in sessions.indices) {

                if (i % BATCH_SIZE == 0) {
                    db.beginTransaction()
                }

                val s = sessions[i]
                values.clear()
                values.put("userId", s.userId)
                values.put("startTime", s.startTime)
                values.put("endTime", s.endTime)
                values.put("sessionDuration", s.sessionDuration)
                values.put("sync", s.sync)

                db.insertWithOnConflict(
                    "tbl_user_session",
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                if (i % BATCH_SIZE == BATCH_SIZE - 1 || i == sessions.size - 1) {
                    db.setTransactionSuccessful()
                    db.endTransaction()
                }
            }
        } finally {
            db.close()
        }
    }

   fun parseAndInsertSessions(providerAttributeTypeList: List<Any>) {
       try {
           val userSessionDao = UserSessionDao(context)

           for (item in providerAttributeTypeList) {
               // Convert each item to JSON object
               val jsonObject = JSONObject(Gson().toJson(item))

               val attributeTypeUuid = jsonObject.optString("attributetypeuuid")

               val providerId = jsonObject.optString("provideruuid") // adjust the key to match your API
               val userId = jsonObject.optString("userid") // adjust the key to match your API

               if (attributeTypeUuid == UuidDictionary.ATTRIBUTE_TYPE_USER_SESSION_TIME) {
                   val valueJson = jsonObject.optString("value")

                   val type = object : TypeToken<List<Map<String, String>>>() {}.type
                   val sessionList: List<Map<String, String>> = Gson().fromJson(valueJson, type)

                   for (sessionMap in sessionList) {
                       val startTime = sessionMap["startTime"] ?: ""
                       val duration = sessionMap["sessionDuration"] ?: ""

                       val exists = userSessionDao.existsByStartTimeAndDuration(startTime, duration)
                       if (exists) {
                           continue
                       }

                       val endTime = calculateEndTime(startTime, duration)

                       val userSession = UserSession(
                           userId = providerId, // or userId if they are same
                           startTime = startTime,
                           endTime = endTime,
                           sessionDuration = duration,
                           sync = "1"
                       )

                       add(userSession)
                   }
               }
           }
       } catch (e: Exception) {
           e.printStackTrace()
       }
   }

    fun existsByStartTimeAndDuration(startTime: String, duration: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM tbl_user_session WHERE startTime = ? AND sessionDuration = ?"
        val cursor = db.rawQuery(query, arrayOf(startTime, duration))
        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()
        return exists
    }

    fun getAverageSessionDurationByDate(userId: String, date: String): Long {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
        SELECT SUM(sessionDuration) 
        FROM tbl_user_session
        WHERE userId = ? 
          AND strftime('%Y-%m-%d', startTime) = ?
        """.trimIndent(), arrayOf(userId, date)
        )

        val totalMillis = if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        cursor.close()
        db.close()

        return totalMillis
    }
    fun getTotalSessionDurationByDateRange(
        userId: String,
        fromDate: String,
        toDate: String
    ): Long {
        val db = dbHelper.readableDatabase

        val sql = """
        SELECT
            SUM(sessionDuration) AS total_duration
        FROM tbl_user_session
        WHERE userId = ? 
          AND strftime('%Y-%m-%d', startTime) BETWEEN ? AND ?
    """.trimIndent()

        val cursor = db.rawQuery(sql, arrayOf(userId, fromDate, toDate))

        var totalDurationMillis = 0L
        if (cursor.moveToFirst()) {
            // getLong(0) will return NULL as 0 if no record is found
            totalDurationMillis = cursor.getLong(0)
        }

        cursor.close()
        db.close()

        return totalDurationMillis
    }

}
