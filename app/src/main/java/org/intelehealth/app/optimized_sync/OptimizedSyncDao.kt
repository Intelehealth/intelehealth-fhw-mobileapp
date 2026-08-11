package org.intelehealth.app.optimized_sync

import android.content.Context
import android.content.Intent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.ImagesDAO
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.database.dao.ProviderDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.models.pushRequestApiCall.PushRequestApiCall
import org.intelehealth.app.utilities.PatientsFrameJson
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UrlModifiers

/**
 * A full sync run as one blocking sequence.
 *
 * The existing sync fires its push on an RxJava subscription and starts the pull on the next line, so
 * the pull routinely asks the server for state the push has not finished writing. The server answers
 * honestly with the old values and the pull persists them over newer local data — most visibly an ABHA
 * number that reverts to "NA" until the following cycle. Every call here blocks, so each step observes
 * the result of the one before it.
 *
 * [periodicSync] is deliberately the only member visible outside this package; the individual steps are
 * meaningless on their own and ordering between them is the entire point.
 */
class OptimizedSyncDao {

    private val appContext: Context get() = IntelehealthApplication.getAppContext()

    fun periodicSync(): Boolean {
        val pushSuccess = pushDataApiPeriodicSync()
        val pullSuccess = pullDataBackgroundSync(appContext)
        val profileImagesPushed = patientProfileImagesPushSync()
        val userProfileImagePushed = loggedInUserProfileImagesPushSync()
        val obsImagesDeleted = deleteObsImageSync()
        val obsImagesPushed = obsImagesPushSync()

        return pushSuccess && pullSuccess && profileImagesPushed &&
            userProfileImagePushed && obsImagesDeleted && obsImagesPushed
    }

    /**
     * Sends everything unsynced and applies the server's acknowledgements before returning, so the pull
     * that follows cannot observe a half-written state. Mirrors the five acknowledgement lists the
     * existing push handles: patients, visits, encounters, appointments and providers.
     */
    private fun pushDataApiPeriodicSync(): Boolean {
        val sessionManager = SessionManager(appContext)
        val pushRequest = PatientsFrameJson().frameJson()

        if (!isDataPresent(pushRequest)) return true

        val url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/push/pushdata"
        val encoded = sessionManager.encoded

        return try {
            val response = AppConstants.apiInterface
                .PUSH_RESPONSE_API_CALL_OBSERVABLE(url, "Basic $encoded", pushRequest)
                .blockingGet()

            val data = response?.data ?: return false

            val patientsDAO = PatientsDAO()
            data.patientlist?.forEach { patient ->
                runCatching {
                    patientsDAO.updateOpemmrsId(patient.openmrsId, patient.syncd.toString(), patient.uuid)
                }.onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val visitsDAO = VisitsDAO()
            data.visitlist?.forEach { visit ->
                runCatching { visitsDAO.updateVisitSync(visit.uuid, visit.syncd.toString()) }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val encounterDAO = EncounterDAO()
            data.encounterlist?.forEach { encounter ->
                runCatching { encounterDAO.updateEncounterSync(encounter.syncd.toString(), encounter.uuid) }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val appointmentDAO = AppointmentDAO()
            data.appointmentList?.forEach { appointment ->
                runCatching { appointmentDAO.updateAppointmentSync(appointment.visitUuid, appointment.sync) }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val providerDAO = ProviderDAO()
            data.providerlist?.forEach { provider ->
                runCatching { providerDAO.updateProviderProfileSync(provider.uuid, "true") }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            sessionManager.setSyncFinished(true)
            broadcastSyncStatus(AppConstants.SYNC_PUSH_DATA_DONE)
            true
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            broadcastSyncStatus(AppConstants.SYNC_FAILED)
            false
        }
    }

    /**
     * Pulls every page before returning. The endpoint is paginated — the response carries the next page
     * number and -1 when exhausted — and the existing implementation recurses through an asynchronous
     * callback, so a caller has no way to know when the data has actually landed. Persistence is handed
     * to [SyncDAO.SyncData] rather than reimplemented, which keeps the tables written here identical to
     * every other pull path.
     */
    private fun pullDataBackgroundSync(context: Context): Boolean {
        val sessionManager = SessionManager(context)
        val syncDAO = SyncDAO()
        val encoded = sessionManager.encoded
        var pageNo = 0

        return try {
            while (true) {
                val url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/" +
                    sessionManager.locationUuid + "/" + sessionManager.pullExcutedTime +
                    "/" + pageNo + "/" + AppConstants.PAGE_LIMIT

                val response = AppConstants.apiInterface
                    .RESPONSE_DTO_CALL(url, "Basic $encoded").execute()

                if (!response.isSuccessful) {
                    broadcastSyncStatus(AppConstants.SYNC_FAILED)
                    return false
                }

                val body = response.body()
                val data = body?.data ?: run {
                    broadcastSyncStatus(AppConstants.SYNC_FAILED)
                    return false
                }

                sessionManager.setPulled(data.pullexecutedtime)
                if (!syncDAO.SyncData(body, true)) {
                    broadcastSyncStatus(AppConstants.SYNC_FAILED)
                    return false
                }

                val nextPageNo = data.pageNo
                if (nextPageNo == -1) break
                pageNo = nextPageNo
            }

            sessionManager.setPullExcutedTime(sessionManager.isPulled)
            sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome())
            sessionManager.setPullSyncFinished(true)
            broadcastSyncStatus(AppConstants.SYNC_PULL_DATA_DONE)
            true
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            broadcastSyncStatus(AppConstants.SYNC_FAILED)
            false
        }
    }

    private fun patientProfileImagesPushSync(): Boolean {
        val sessionManager = SessionManager(appContext)
        val encoded = sessionManager.encoded
        val imagesDAO = ImagesDAO()
        val url = UrlModifiers().setPatientProfileImageUrl()

        val profiles = runCatching { imagesDAO.patientProfileUnsyncedImages }
            .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            .getOrNull() ?: return false

        var allSynced = true
        profiles.forEach { profile ->
            runCatching {
                AppConstants.apiInterface
                    .PERSON_PROFILE_PIC_UPLOAD(url, "Basic $encoded", profile).blockingGet()
                imagesDAO.updateUnsyncedPatientProfile(profile.person, "PP")
            }.onFailure {
                allSynced = false
                FirebaseCrashlytics.getInstance().recordException(it)
            }
        }
        return allSynced
    }

    /**
     * Uploads each unsynced obs image and marks it locally once the call completes.
     *
     * The endpoint returns an Observable and the asynchronous original marks the row synced in
     * onComplete rather than onNext, so success means "the call finished", not "a value arrived".
     * ignoreElements().blockingAwait() preserves exactly that: it returns on completion and throws on
     * error. blockingFirst() would not — it raises NoSuchElementException when a stream completes
     * without emitting.
     */
    private fun obsImagesPushSync(): Boolean {
        val sessionManager = SessionManager(appContext)
        val encoded = sessionManager.encoded
        val imagesDAO = ImagesDAO()
        val url = UrlModifiers().setObsImageUrl()

        val obsImages = runCatching { imagesDAO.obsUnsyncedImages }
            .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            .getOrNull() ?: return false

        var allSynced = true
        obsImages.forEach { obs ->
            runCatching {
                val file = File(AppConstants.IMAGE_PATH + obs.uuid + ".jpg")
                val requestFile = RequestBody.create("application/json".toMediaTypeOrNull(), file)
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                AppConstants.apiInterface
                    .OBS_JSON_RESPONSE_OBSERVABLE(url, "Basic $encoded", body, obs)
                    .ignoreElements()
                    .blockingAwait()

                imagesDAO.updateUnsyncedObsImages(obs.uuid)
            }.onFailure {
                allSynced = false
                FirebaseCrashlytics.getInstance().recordException(it)
            }
        }

        sessionManager.setPushSyncFinished(true)
        if (!sessionManager.isLogout) {
            broadcastSyncStatus(AppConstants.SYNC_OBS_IMAGE_PUSH_DONE)
        }
        return allSynced
    }

    /**
     * Deletes obs images voided locally.
     *
     * Faithful to the existing behaviour, which records nothing on success — so the same images are
     * offered again on every sync. Left as-is deliberately: the job here is to make the sequence
     * blocking, not to change what it does.
     */
    private fun deleteObsImageSync(): Boolean {
        val sessionManager = SessionManager(appContext)
        val encoded = sessionManager.encoded
        val imagesDAO = ImagesDAO()
        val urlModifiers = UrlModifiers()

        val voidedObsImages = runCatching { imagesDAO.voidedImageObs }
            .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            .getOrNull() ?: return false

        var allDeleted = true
        voidedObsImages.forEach { voidedObsImage ->
            runCatching {
                AppConstants.apiInterface
                    .DELETE_OBS_IMAGE(urlModifiers.obsImageDeleteUrl(voidedObsImage), "Basic $encoded")
                    .ignoreElements()
                    .blockingAwait()
            }.onFailure {
                allDeleted = false
                FirebaseCrashlytics.getInstance().recordException(it)
            }
        }
        return allDeleted
    }

    private fun loggedInUserProfileImagesPushSync(): Boolean {
        val sessionManager = SessionManager(appContext)
        val encoded = sessionManager.encoded
        val imagesDAO = ImagesDAO()
        val url = UrlModifiers().setProviderProfileImageUrl()

        val providerProfile = runCatching {
            imagesDAO.getUserProfileUnsyncedImages(sessionManager.providerID)
        }.onFailure { FirebaseCrashlytics.getInstance().recordException(it) }.getOrNull()

        if (providerProfile?.file.isNullOrEmpty()) return true

        return runCatching {
            AppConstants.apiInterface
                .PROVIDER_PROFILE_PIC_UPLOAD(url, providerProfile, "Basic $encoded")
                .blockingGet()
            imagesDAO.updateUnsyncedUserProfile(providerProfile!!.providerid)
            true
        }.onFailure { FirebaseCrashlytics.getInstance().recordException(it) }.getOrDefault(false)
    }

    private fun isDataPresent(request: PushRequestApiCall?): Boolean {
        if (request == null) return false
        return request.patients?.isNotEmpty() == true ||
            request.persons?.isNotEmpty() == true ||
            request.visits?.isNotEmpty() == true ||
            request.encounters?.isNotEmpty() == true ||
            request.providers?.isNotEmpty() == true ||
            request.appointments?.isNotEmpty() == true
    }

    private fun broadcastSyncStatus(status: Int) {
        appContext.sendBroadcast(
            Intent(AppConstants.SYNC_INTENT_ACTION)
                .setPackage(appContext.packageName)
                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, status)
        )
    }
}
