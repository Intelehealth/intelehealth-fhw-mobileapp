package org.intelehealth.ncd.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.ncd.model.Patient

@Dao
interface PatientDao {

    @Query("SELECT * FROM tbl_patient WHERE DATE('now') >= DATE(date_of_birth, :age || ' years')")
    suspend fun getPatientsBasedOnAge(age: Int): List<Patient>

    @Query("SELECT * FROM tbl_patient WHERE DATE('now') < DATE(date_of_birth, :age || ' years')")
    suspend fun getPatientsBelowAge(age: Int): List<Patient>

}