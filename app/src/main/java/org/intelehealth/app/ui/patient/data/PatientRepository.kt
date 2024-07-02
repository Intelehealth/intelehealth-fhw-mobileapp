package org.intelehealth.app.ui.patient.data

import org.intelehealth.app.database.dao.PatientsDAO

/**
 * Created by Vaghela Mithun R. on 02-07-2024 - 13:45.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientRepository(private val patientsDao: PatientsDAO) {
    suspend fun createNewPatient() {
//        patientsDao.createPatients()
    }
}