package org.intelehealth.app.syncModule

import android.content.Context
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.appointment.sync.AppointmentSync
import org.intelehealth.app.database.dao.ImagesPushDAO
import org.intelehealth.app.database.dao.SyncDAO

class SyncWorkerForHomeScreen (
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val syncDAO = SyncDAO()
        val imagesPushDAO = ImagesPushDAO()
        val fromActivity = inputData.getString("fromActivity") ?: return Result.failure()

        // Push data to the server
        val isSynced = syncDAO.pushDataApi()

        // Wait for 4 seconds before pulling data
        Thread.sleep(4000)
        syncDAO.pullData(applicationContext, fromActivity, 0)
        AppointmentSync.getAppointments(applicationContext)

        // Push images
        imagesPushDAO.patientProfileImagesPush()
        imagesPushDAO.loggedInUserProfileImagesPush()

        // Wait for 3 seconds before pushing observation images
        Thread.sleep(3000)
        imagesPushDAO.obsImagesPush()

        // Delete local images after upload
        imagesPushDAO.deleteObsImage()

        return if (isSynced) Result.success() else Result.retry()
    }
}