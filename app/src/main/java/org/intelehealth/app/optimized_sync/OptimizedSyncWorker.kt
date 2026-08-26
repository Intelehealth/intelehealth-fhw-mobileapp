package org.intelehealth.app.optimized_sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.appointment.model.AppointmentInfo
import org.intelehealth.app.optimized_sync.network.NetworkConnectivityManager
import org.intelehealth.app.utilities.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    /**
     * Only the periodic run promotes itself to the foreground.
     *
     * The scheduled run has no user watching it and needs the notification to survive being snoozed.
     * The one-off runs fire from a save or the refresh button while the app is on screen, where the
     * process is already alive and the notification adds nothing — and there are dozens of those a day,
     * each spending from the same daily foreground-service budget the platform now enforces.
     */
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
            if (inputData.getBoolean(KEY_RUN_IN_FOREGROUND, false)) {
                setForegroundAsync(getForegroundInfo()).get()
            }
            val isSyncSuccessful = OptimizedSyncDao().periodicSync()
            reviewUpcomingAppointments()
            if (isSyncSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /**
     * Warns about appointments starting within the next three quarters of an hour and drops the ones
     * already past. Carried over from the worker this replaces, where it ran on a fifteen second delayed
     * main-thread handler; a Worker is already off the main thread, so the delay serves no purpose and
     * the sweep runs directly after the appointment list has been refreshed.
     *
     * The channel mismatch is carried over as-is rather than corrected: the channel created here is
     * "Intelehealth" while the notification is built against "unicef", which the platform has never
     * registered, so these notifications are dropped and have been for as long as the minimum supported
     * version has been Oreo. Repairing it would start delivering reminders that have never been seen,
     * which is a product decision and not part of moving the sync off its callbacks.
     */
    private fun reviewUpcomingAppointments() {
        runCatching {
            val appointmentDAO = AppointmentDAO()
            val appointments = appointmentDAO.appointments ?: return
            val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            val now = dateFormat.parse(dateFormat.format(Date())) ?: return

            appointments.forEach { appointment ->
                val slot = dateFormat.parse("${appointment.slotDate} ${appointment.slotTime}")
                    ?: return@forEach
                val minutesAway = (slot.time - now.time) / 1000 / 60

                if (minutesAway in 1..MINUTES_BEFORE_APPOINTMENT_REMINDER) {
                    displayAppointmentNotification(appointment)
                }
                if (minutesAway <= 0) {
                    appointmentDAO.deleteAppointmentByVisitId(appointment.visitUuid)
                }
            }
        }.onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
    }

    private fun displayAppointmentNotification(appointment: AppointmentInfo) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                LEGACY_APPOINTMENT_CHANNEL_NAME,
                LEGACY_APPOINTMENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val message = context.getString(R.string.today_at) + appointment.slotTime + " " +
            appointment.patientName + " " +
            context.getString(R.string.has_an_appointment_with) + " " + appointment.drName

        val notification = NotificationCompat.Builder(context, LEGACY_APPOINTMENT_CHANNEL_ID)
            .setContentTitle(context.getString(R.string._appointment))
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        notificationManager.notify(APPOINTMENT_NOTIFICATION_ID, notification)
    }

    override fun getForegroundInfo(): ForegroundInfo = getOptimizedSyncForeground(context)

    companion object {
        private const val LEGACY_APPOINTMENT_CHANNEL_NAME = "Intelehealth"
        private const val LEGACY_APPOINTMENT_CHANNEL_ID = "unicef"
        private const val APPOINTMENT_NOTIFICATION_ID = 1
        private const val MINUTES_BEFORE_APPOINTMENT_REMINDER = 45L

        private const val KEY_RUN_IN_FOREGROUND = "run_in_foreground"

        /**
         * Both schedules carry the connectivity constraint the requests they replace were built with,
         * so WorkManager holds the run back until the device is online instead of starting it and
         * having the worker's own check turn it away.
         */
        private val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        private fun buildPeriodicSyncRequest() = PeriodicWorkRequestBuilder<OptimizedSyncWorker>(
            OptimizedSyncConstants.PERIODIC_WORK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        ).setInitialDelay(
            OptimizedSyncConstants.PERIODIC_WORK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        ).setConstraints(syncConstraints)
            .setInputData(workDataOf(KEY_RUN_IN_FOREGROUND to true))
            .build()

        /**
         * Installs the periodic schedule, retiring the one it replaces first.
         *
         * The schedule this supersedes was registered under its own unique name and WorkManager keeps
         * enqueued periodic work across launches and reboots, so on an upgraded install it would carry
         * on running its own sync alongside this one. Cancelling happens before the setup gate because
         * a device that has not completed setup still needs the old schedule gone.
         */
        @JvmStatic
        fun enqueuePeriodicWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(AppConstants.UNIQUE_WORK_NAME)

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
         *
         * No input data is set, so this run stays in the background and posts no notification.
         */
        @JvmStatic
        fun enqueueOneTimeWork(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                OptimizedSyncConstants.UNIQUE_ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<OptimizedSyncWorker>()
                    .setConstraints(syncConstraints)
                    .build()
            )
        }
    }
}
