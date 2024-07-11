package org.intelehealth.app.ui.patient.data

import android.database.sqlite.SQLiteOpenHelper
import com.github.ajalt.timberkt.Timber
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.room.dao.PatientRegFieldDao

/**
 * Created by Vaghela Mithun R. on 02-07-2024 - 13:45.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientRepository(
    private val patientsDao: PatientsDAO,
    private val sqlHelper: SQLiteOpenHelper,
    regFieldDao: PatientRegFieldDao
) : RegFieldRepository(regFieldDao) {
    fun createNewPatient(patient: PatientDTO): Boolean =
        patientsDao.createPatients(patient, sqlHelper.writableDatabase)

    fun fetchPatient(uuid: String): PatientDTO {
        Timber.d { "uuid => $uuid" }
        PatientQueryBuilder().buildPatientDetailsQuery(uuid).apply {
            Timber.d { "Query => $this" }
            val cursor = sqlHelper.readableDatabase.rawQuery(this, null)
            return patientsDao.retrievePatientDetails(cursor)
        }
    }
}