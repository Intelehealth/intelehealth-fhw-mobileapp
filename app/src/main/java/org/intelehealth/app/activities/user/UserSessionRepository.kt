package org.intelehealth.app.user

import android.util.Log
import androidx.work.impl.utils.PreferenceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale


class UserSessionRepository private constructor(
    private val sessionManager: SessionManager,
    private val userSessionDao: UserSessionDao
) {

    private var userSession: UserSession? = null

    fun startSession() {
        userSession = UserSession(
            userId = sessionManager.providerID,
            startTime = DateAndTimeUtils.getTodaysDateInRequiredFormat("yyyy-MM-dd HH:mm:ss"),
            endTime = "",
            sessionDuration = "",
            sync = "0"
        )
    }

    fun endSession() {
        val session = userSession
        if (session == null) {
            return
        }

        session.endTime = DateAndTimeUtils.getTodaysDateInRequiredFormat("yyyy-MM-dd HH:mm:ss")
        session.userId = sessionManager.providerID
        session.sessionDuration = calculateDurationMillis(session.startTime, session.endTime)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                userSessionDao.add(session)
            } catch (e: Exception) {
            }
        }

        userSession = null // Clear session after saving
    }

    private fun calculateDurationMillis(startTime: String?, endTime: String?): String {
        if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) return ""

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val startDate = format.parse(startTime)
            val endDate = format.parse(endTime)

            if (startDate != null && endDate != null) {
                (endDate.time - startDate.time).toString() // duration in milliseconds as string
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e("UserSessionRepository", "Error parsing dates", e)
            ""
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserSessionRepository? = null

        @JvmStatic
        fun getInstance(sessionManager: SessionManager, userSessionDao: UserSessionDao): UserSessionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSessionRepository(sessionManager, userSessionDao).also { INSTANCE = it }
            }
        }
    }
}
