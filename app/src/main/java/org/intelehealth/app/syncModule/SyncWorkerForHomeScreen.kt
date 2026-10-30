package org.intelehealth.app.syncModule

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
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

        // Push data to the server
        val isSynced = syncDAO.pushDataApi()

        // Wait for 4 seconds before pulling data
        Thread.sleep(4000)
        // Same paged pull as the background sync, so two pulls never run side by side and
        // the home screen can follow it through PullSyncWorker's work state.
        PullSyncWorker.enqueue(applicationContext)
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

    companion object {
        const val UNIQUE_WORK_NAME = "home_screen_sync"
    }
}
