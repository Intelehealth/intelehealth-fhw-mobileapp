package org.intelehealth.app.optimized_sync

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.intelehealth.app.optimized_sync.network.NetworkConnectivityManager
import org.intelehealth.app.utilities.SessionManager
import java.util.concurrent.TimeUnit

/**
 * Runs a full sync as one blocking sequence.
 *
 * The same class serves the periodic schedule and the one-off syncs fired after a save, so both take
 * the identical code path — the distinction is only how the work is enqueued, and the two unique names
 * keep them separately addressable in WorkManager.
 *
 * Both gates fail the run rather than proceeding on a doomed connection: validated internet, and either
 * adequate battery or mains power.
 */
class OptimizedSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val networkManager = NetworkConnectivityManager(context = context)
        val powerState = PowerStateProvider(context = context)

        if (!networkManager.getCurrentStatus().hasInternet) {
            return Result.failure()
        }

        if (!powerState.isPowerRequirementMet()) {
            return Result.failure()
        }

        return try {
            setForegroundAsync(foregroundInfo).get()
            val isSyncSuccessful = OptimizedSyncDao().periodicSync()
            if (isSyncSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    override fun getForegroundInfo(): ForegroundInfo = getOptimizedSyncForeground(context)

    companion object {
        private fun buildPeriodicSyncRequest() = PeriodicWorkRequestBuilder<OptimizedSyncWorker>(
            OptimizedSyncConstants.PERIODIC_WORK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        ).setInitialDelay(
            OptimizedSyncConstants.PERIODIC_WORK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        ).build()

        @JvmStatic
        fun enqueuePeriodicWork(context: Context) {
            if (SessionManager(context).isSetupComplete) {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    OptimizedSyncConstants.UNIQUE_PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    buildPeriodicSyncRequest()
                )
            }
        }

        @JvmStatic
        fun cancelPeriodicWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(
                OptimizedSyncConstants.UNIQUE_PERIODIC_WORK_NAME
            )
        }

        /**
         * Fires a single sync now, used after a save rather than waiting for the next period.
         *
         * APPEND keeps saves made in quick succession from running concurrently — each waits for the
         * one before it, which is the whole point of making the sequence blocking. KEEP would instead
         * discard the later request, and a save enqueued while an earlier sync was mid-flight would
         * never be pushed.
         */
        @JvmStatic
        fun enqueueOneTimeWork(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                OptimizedSyncConstants.UNIQUE_ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<OptimizedSyncWorker>().build()
            )
        }
    }
}
