package org.intelehealth.app.activities.user.api

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class WorkScheduler {
    private val TAG = "WorkScheduler"

    fun scheduleDailyApiWorker(context: Context) {
        Log.d(TAG, "scheduleDailyApiWorker: api")
        val now = LocalDateTime.now()
        var nextRun = now.withHour(18).withMinute(0).withSecond(0).withNano(0)

        /*  val now = LocalDateTime.now()
          var nextRun = now.withHour(20).withMinute(0).withSecond(0).withNano(0) // 8 PM
  */
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1)
        }

        val initialDelay = Duration.between(now, nextRun).toMinutes()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequest.Builder(DailyApiWorker::class.java, 1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag("DailyApiTag")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyApiWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
