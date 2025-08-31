package org.intelehealth.app.activities.user.api

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import org.intelehealth.app.networkApiCalls.ApiClient
import org.intelehealth.app.networkApiCalls.ApiInterface
import org.intelehealth.app.user.UserSessionDao
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import java.time.LocalDate

class DailyApiWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val TAG = "DailyApiWorker"
    private val sharedPrefs = context.getSharedPreferences("daily_api_pref", Context.MODE_PRIVATE)

    override suspend fun doWork(): Result {
        val currentDate = LocalDate.now().toString()
        val lastApiDate = sharedPrefs.getString("lastApiCallDate", "")

        return try {
            syncUserSessions(context)
            // Save today's date
            sharedPrefs.edit().putString("lastApiCallDate", currentDate).apply()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    @SuppressLint("CheckResult")
    fun syncUserSessions(context: Context) {
        val unsyncedSessions = UserSessionDao(context).getUnsyncedSessions()
        if (unsyncedSessions.isEmpty()) return
        val sessionData = unsyncedSessions.map {
            UserSessionResponse(
                startTime = it.startTime,
                sessionDuration = it.sessionDuration
            )
        }

        val gson = Gson()
        val sessionJsonString = gson.toJson(sessionData)

        val sessionManager = SessionManager(context)
        ApiClient.changeApiBaseUrl(sessionManager.getServerUrl())

        val inputModel = UserSessionRequest(
            value = sessionJsonString,
            attributeType = UuidDictionary.ATTRIBUTE_TYPE_USER_SESSION_TIME
        )
        val api = ApiClient.createService(ApiInterface::class.java)
        api.pushUserSessionDetails2(sessionManager.getProviderID(), inputModel, "Basic ${sessionManager.getEncoded()}")
            .subscribeOn(io.reactivex.schedulers.Schedulers.io()) // run in background
            .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread()) // callback on main
            .subscribe({ response ->
                val sessionIds = unsyncedSessions.map { it.sessionId }
                UserSessionDao(context).markSessionsAsSynced(sessionIds) }, { error ->
                Log.e(TAG, "API error", error)
            })
    }
}
