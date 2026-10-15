package org.intelehealth.app.ui.home

import android.database.sqlite.SQLiteDatabase
import android.os.CancellationSignal
import android.util.Log
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.models.PrescriptionModel

class HomeScreenQueriesRepository {

    @JvmOverloads
    fun getRecentNotEndedVisits(db: SQLiteDatabase, signal: CancellationSignal? = null): List<PrescriptionModel> {
        val query = HomeScreenQueriesBuilder().getRecentNotEndedVisitsQuery()
        db.rawQuery(query, null, signal).use { cursor ->
            val resultList = mutableListOf<PrescriptionModel>()
            while (cursor.moveToNext()) {
                // Each row also runs a prescription lookup; stop those too once cancelled.
                signal?.throwIfCanceled()
                val model = PrescriptionModel().apply {
                    patientUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    patient_photo = cursor.getString(cursor.getColumnIndexOrThrow("patient_photo"))
                    visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visitUUID"))
                    first_name = cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
                    middle_name = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"))
                    phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"))
                    last_name = cursor.getString(cursor.getColumnIndexOrThrow("last_name"))
                    visit_start_date = cursor.getString(cursor.getColumnIndexOrThrow("startdate"))
                    dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"))
                    gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                    openmrs_id = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id"))
                    try {
                        isHasPrescription = EncounterDAO().isPrescriptionReceived(visitUuid)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                resultList.add(model)
            }

            return resultList
        }
    }

    @JvmOverloads
    fun getOlderNotEndedVisits(db: SQLiteDatabase, signal: CancellationSignal? = null): List<PrescriptionModel> {
        val query = HomeScreenQueriesBuilder().getOlderNotEndedVisits()
        db.rawQuery(query, null, signal).use { cursor ->
            val resultList = mutableListOf<PrescriptionModel>()

            while (cursor.moveToNext()) {
                signal?.throwIfCanceled()
                val model = PrescriptionModel().apply {
                    patientUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    patient_photo = cursor.getString(cursor.getColumnIndexOrThrow("patient_photo"))
                    visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visitUUID"))
                    first_name = cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
                    middle_name = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"))
                    phone_number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"))
                    last_name = cursor.getString(cursor.getColumnIndexOrThrow("last_name"))
                    visit_start_date = cursor.getString(cursor.getColumnIndexOrThrow("startdate"))
                    dob = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"))
                    gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                    openmrs_id = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id"))
                    try {
                        isHasPrescription = EncounterDAO().isPrescriptionReceived(visitUuid)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                resultList.add(model)
            }

            return resultList
        }
    }

    @JvmOverloads
    fun getPendingPrescriptionVisitsCount(db: SQLiteDatabase, signal: CancellationSignal? = null): Int {
        val query = HomeScreenQueriesBuilder().getPendingPrescriptionVisitsCount()
        db.rawQuery(query, null, signal).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    @JvmOverloads
    fun getReceivedPrescriptionVisitsCount(db: SQLiteDatabase, signal: CancellationSignal? = null): Int {
        val query = HomeScreenQueriesBuilder().getReceivedPrescriptionVisitsCount()
        db.rawQuery(query, null, signal).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun getUpcomingAppointmentCount(db: SQLiteDatabase): Int {
        val query = HomeScreenQueriesBuilder().getUpcomingAppointmentsCount()
        db.rawQuery(query, null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

}