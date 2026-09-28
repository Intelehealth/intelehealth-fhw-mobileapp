package org.intelehealth.app.optimized_sync

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.work.WorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.appointment.api.ApiClientAppointment
import org.intelehealth.app.appointment.dao.AppointmentDAO
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.ImagesDAO
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.database.dao.ProviderDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.database.dao.VisitsDAO
import org.intelehealth.app.models.pushRequestApiCall.PushRequestApiCall
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.NavigationUtils
import org.intelehealth.app.utilities.NotificationUtils
import org.intelehealth.app.utilities.PatientsFrameJson
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UrlModifiers
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    /**
     * Runs the whole sequence and reports whether the clinical record reached the server and came back.
     *
     * Only the push and the pull decide the outcome. The image steps and the appointment pull are
     * deliberately excluded: a rejected photograph or an unreachable appointment service says nothing
     * about whether patient data synced, and the callers that gate their UI on this boolean would read
     * one as a failed sync. They still run, still log their own failures, and are still retried on the
     * next cycle — they just do not veto the result.
     */
    fun periodicSync(): Boolean {
        val sessionManager = SessionManager(appContext)

        val pushSuccess = pushDataApiPeriodicSync()
        val pullSuccess = pullDataBackgroundSync(appContext)

        patientProfileImagesPushSync()
        loggedInUserProfileImagesPushSync()
        deleteObsImageSync()
        obsImagesPushSync()

        if (!sessionManager.isLogout) appointmentsPullSync()

        NotificationUtils().clearAllNotifications(appContext)
        enqueuePostSyncBroadcasts()

        return pushSuccess && pullSuccess
    }

    /**
     * Replaces the locally held appointment slots with the next thirty days from the server.
     *
     * The existing pull enqueues its call and returns immediately, so on the old path this ran
     * concurrently with the data pull it was supposed to follow. Executing it blocks instead, which also
     * means the delete-then-insert below cannot interleave with another sync's copy of itself.
     *
     * The 401 sign-out is preserved from the asynchronous original, but posted to the main thread: it
     * ends in a Toast and an Activity launch, and this now runs on a worker thread where the original
     * ran on Retrofit's callback thread.
     */
    private fun appointmentsPullSync(): Boolean {
        val sessionManager = SessionManager(appContext)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val startDate = dateFormat.format(Date())
        val endDate = dateFormat.format(Date(Date().time + THIRTY_DAYS_IN_MILLIS))
        val baseUrl = sessionManager.serverUrl + ":3004"

        return try {
            val response = ApiClientAppointment.getInstance(baseUrl).api
                .getSlotsAll(startDate, endDate, sessionManager.locationUuid)
                .execute()

            val slots = response.body()?.data ?: return false

            val appointmentDAO = AppointmentDAO()
            appointmentDAO.deleteAllAppointments()
            slots.forEach { slot ->
                runCatching { appointmentDAO.insert(slot) }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            appContext.sendBroadcast(
                Intent(AppConstants.SYNC_NOTIFY_INTENT_ACTION)
                    .setPackage(appContext.packageName)
                    .putExtra("JOB", AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE)
            )
            broadcastSyncStatus(AppConstants.SYNC_APPOINTMENT_PULL_DATA_DONE)
            true
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Handler(Looper.getMainLooper()).post {
                NavigationUtils().logoutOperation(appContext, e)
            }
            false
        }
    }

    /**
     * Fires the two post-sync notifications the screens listen for: a prescription-download refresh and
     * a bare last-sync broadcast. Both are carried by existing workers rather than sent from here so the
     * short delays they build in are preserved.
     */
    private fun enqueuePostSyncBroadcasts() {
        WorkManager.getInstance(appContext)
            .beginWith(AppConstants.VISIT_SUMMARY_WORK_REQUEST)
            .then(AppConstants.LAST_SYNC_WORK_REQUEST)
            .enqueue()
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

            val data = response?.data ?: run {
                // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
                CustomLog.e("NAS1752", "push failed - response body/data was null: $response")
                return false
            }

            val patientsDAO = PatientsDAO()
            data.patientlist?.forEach { patient ->
                runCatching {
                    patientsDAO.updateOpemmrsId(
                        patient.openmrsId,
                        patient.syncd.toString(),
                        patient.uuid
                    )
                    // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
                    CustomLog.d(
                        "NAS1752", "openmrsId assigned - patientUuid=${patient.uuid} " +
                                "openmrsId=${patient.openmrsId}"
                    )
                }.onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val visitsDAO = VisitsDAO()
            data.visitlist?.forEach { visit ->
                runCatching { visitsDAO.updateVisitSync(visit.uuid, visit.syncd.toString()) }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val encounterDAO = EncounterDAO()
            data.encounterlist?.forEach { encounter ->
                runCatching {
                    encounterDAO.updateEncounterSync(
                        encounter.syncd.toString(),
                        encounter.uuid
                    )
                }
                    .onFailure { FirebaseCrashlytics.getInstance().recordException(it) }
            }

            val appointmentDAO = AppointmentDAO()
            data.appointmentList?.forEach { appointment ->
                runCatching {
                    appointmentDAO.updateAppointmentSync(
                        appointment.visitUuid,
                        appointment.sync
                    )
                }
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
            // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
            val body = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()
            CustomLog.e(
                "NAS1752", "push failed - ${e.javaClass.simpleName}: ${e.message}" +
                    (body?.let { " | serverBody=$it" } ?: ""))
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
                CustomLog.e(
                    "OptimizedSyncDao" + "pullDataBackgroundSync",
                    "Before API call: pageNo=$pageNo, url=$url"
                )
                val response = executePageWithRetry {
                    AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic $encoded").execute()
                }
                CustomLog.e(
                    "OptimizedSyncDao" + "pullDataBackgroundSync",
                    "After API call: pageNo=$pageNo, url=$url"
                )

                if (!response.isSuccessful) {
                    // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
                    CustomLog.e(
                        "NAS1752", "pull failed - HTTP ${response.code()}: " +
                                "${response.errorBody()?.string()}"
                    )
                    broadcastSyncStatus(AppConstants.SYNC_FAILED)
                    return false
                }

                val body = response.body()
                val data = body?.data ?: run {
                    // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
                    CustomLog.e("NAS1752", "pull failed - response body/data was null")
                    broadcastSyncStatus(AppConstants.SYNC_FAILED)
                    return false
                }

                sessionManager.setPulled(data.pullexecutedtime)
                CustomLog.e(
                    "OptimizedSyncDao",
                    "pullDataBackgroundSync: pageNo=$pageNo, pullexecutedtime=${data.pullexecutedtime}"
                )
                if (!syncDAO.SyncData(body, true)) {
                    // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
                    CustomLog.e("NAS1752", "pull failed - syncDAO.SyncData returned false")
                    broadcastSyncStatus(AppConstants.SYNC_FAILED)
                    return false
                }
                CustomLog.e(
                    "OptimizedSyncDao",
                    "pullDataBackgroundSync completed for pageNo=$pageNo"
                )

                val nextPageNo = data.pageNo
                CustomLog.e(
                    "OptimizedSyncDao",
                    "pullDataBackgroundSync: pageNo=$pageNo, nextPageNo=$nextPageNo"
                )
                if (nextPageNo == -1) break
                pageNo = nextPageNo
            }

            sessionManager.setPullExcutedTime(sessionManager.isPulled)
            sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome())
            //sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome())
            sessionManager.setLastSyncDateTime(
                AppConstants.dateAndTimeUtils.getcurrentDateTime(
                    sessionManager.getAppLanguage()
                )
            )

            sessionManager.setPullSyncFinished(true)
            broadcastSyncStatus(AppConstants.SYNC_PULL_DATA_DONE)
            true
        } catch (e: Exception) {
            // TODO(NAS-1752): temporary QA logging, remove once consent testing is done.
            val body = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()

            CustomLog.e(
                "NAS1752", "pull failed - ${e.javaClass.simpleName}: ${e.message}" +
                    (body?.let { " | serverBody=$it" } ?: ""))
            FirebaseCrashlytics.getInstance().recordException(e)
            broadcastSyncStatus(AppConstants.SYNC_FAILED)
            false
        }
    }

    /**
     * Retries a single page fetch on transient network failures (timeouts, dropped connections)
     * instead of letting one bad page abort the whole multi-page pull. Rethrows the last IOException
     * once attempts are exhausted so the caller's existing catch block handles it unchanged.
     */
    private fun <T> executePageWithRetry(maxAttempts: Int = PULL_PAGE_MAX_ATTEMPTS, block: () -> Response<T>): Response<T> {
        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: IOException) {
                CustomLog.e(
                    "NAS1752",
                    "pull page attempt ${attempt + 1}/$maxAttempts failed - " +
                        "${e.javaClass.simpleName}: ${e.message}"
                )
                if (attempt == maxAttempts - 1) throw e
                Thread.sleep(PULL_PAGE_RETRY_BACKOFF_MS * (attempt + 1))
            }
        }
        error("unreachable")
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
                    .DELETE_OBS_IMAGE(
                        urlModifiers.obsImageDeleteUrl(voidedObsImage),
                        "Basic $encoded"
                    )
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

    private companion object {
        const val THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000
        const val PULL_PAGE_MAX_ATTEMPTS = 3
        const val PULL_PAGE_RETRY_BACKOFF_MS = 2000L
    }
}
