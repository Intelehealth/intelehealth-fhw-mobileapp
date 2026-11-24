package org.intelehealth.app.testutil

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.InteleHealthDatabaseHelper

/**
 * Wires the app's static database / context globals to a real SQLite database
 * in the Robolectric sandbox (fresh per test), so DAOs that read
 * `IntelehealthApplication.inteleHealthDatabaseHelper` work unchanged.
 * Call [install] in @Before and [uninstall] in @After.
 */
object TestAppDatabase {

    fun install(context: Context): SQLiteDatabase {
        setAppContext(context)
        InteleHealthDatabaseHelper.database = null
        val helper = InteleHealthDatabaseHelper(context)
        IntelehealthApplication.inteleHealthDatabaseHelper = helper
        return helper.writeDb
    }

    fun uninstall() {
        InteleHealthDatabaseHelper.database?.close()
        InteleHealthDatabaseHelper.database = null
        IntelehealthApplication.inteleHealthDatabaseHelper = null
        setAppContext(null)
    }

    /** Runs [block] on a worker thread and waits: DAO writes run inline only off the main thread. */
    fun onWorkerThread(block: () -> Unit) {
        var error: Throwable? = null
        val thread = Thread { try { block() } catch (t: Throwable) { error = t } }
        thread.start()
        thread.join()
        error?.let { throw it }
    }

    fun insertVisit(db: SQLiteDatabase, uuid: String, endDate: String? = null, submitted: Boolean = false) {
        db.insertOrThrow("tbl_visit", null, ContentValues().apply {
            put("uuid", uuid)
            put("patientuuid", "patient-$uuid")
            put("startdate", "2026-11-23T09:00:00Z")
            put("enddate", endDate)
            put("issubmitted", if (submitted) 1 else 0)
        })
    }

    fun insertPatient(
        db: SQLiteDatabase, uuid: String, openmrsId: String, first: String, last: String?,
        gender: String, dob: String?, photo: String? = null
    ) {
        db.insertOrThrow("tbl_patient", null, ContentValues().apply {
            put("uuid", uuid)
            put("openmrs_id", openmrsId)
            put("first_name", first)
            put("last_name", last)
            put("gender", gender)
            put("date_of_birth", dob)
            put("patient_photo", photo)
        })
    }

    /** Visit linked to [patientUuid] (unlike [insertVisit], which uses a placeholder patient). */
    fun insertVisitFor(db: SQLiteDatabase, uuid: String, patientUuid: String, sync: String = "false") {
        db.insertOrThrow("tbl_visit", null, ContentValues().apply {
            put("uuid", uuid)
            put("patientuuid", patientUuid)
            put("startdate", "2026-11-23T09:00:00Z")
            put("sync", sync)
        })
    }

    private const val ADULT_INITIAL_ENCOUNTER = "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f"
    private const val CHIEF_COMPLAINT_CONCEPT = "3edb0e09-9135-481e-b8f0-07a26fa9a5ce"

    /** Chief complaint obs on the visit's adult-initial encounter (how the app stores it). */
    fun insertChiefComplaint(db: SQLiteDatabase, visitUuid: String, value: String) {
        val encounter = "enc-$visitUuid"
        db.insertWithOnConflict("tbl_encounter", null, ContentValues().apply {
            put("uuid", encounter)
            put("visituuid", visitUuid)
            put("encounter_type_uuid", ADULT_INITIAL_ENCOUNTER)
        }, SQLiteDatabase.CONFLICT_REPLACE)
        db.insertOrThrow("tbl_obs", null, ContentValues().apply {
            put("uuid", "obs-$visitUuid-${System.nanoTime()}")
            put("encounteruuid", encounter)
            put("conceptuuid", CHIEF_COMPLAINT_CONCEPT)
            put("value", value)
        })
    }

    fun insertEncounter(db: SQLiteDatabase, uuid: String, visitUuid: String, sync: String) {
        db.insertOrThrow("tbl_encounter", null, ContentValues().apply {
            put("uuid", uuid)
            put("visituuid", visitUuid)
            put("encounter_type_uuid", "some-type")
            put("sync", sync)
        })
    }

    fun isSubmitted(db: SQLiteDatabase, visitUuid: String): Boolean =
        db.rawQuery("SELECT issubmitted FROM tbl_visit WHERE uuid = ?", arrayOf(visitUuid)).use {
            it.moveToFirst() && it.getInt(0) == 1
        }

    private fun setAppContext(context: Context?) = IntelehealthApplication.setAppContextForTesting(context)
}
