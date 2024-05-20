package org.intelehealth.ncd.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.ncd.model.Patient
import org.intelehealth.ncd.model.PatientWithAttribute

@Dao
interface PatientDao {

    @Query("SELECT * FROM tbl_patient WHERE DATE('now') >= DATE(date_of_birth, :age || ' years')")
    suspend fun getPatientsBasedOnAge(age: Int): List<Patient>

    @Query("SELECT * FROM tbl_patient WHERE DATE('now') < DATE(date_of_birth, :age || ' years')")
    suspend fun getPatientsBelowAge(age: Int): List<Patient>

    @Query(
        "SELECT a.uuid, a.openmrs_id, a.first_name, a.middle_name, a.last_name, a.date_of_birth, b.value, b.person_attribute_type_uuid FROM tbl_patient AS a " +
                "INNER JOIN tbl_patient_attribute AS b ON a.uuid = b.patientuuid " +
                "WHERE b.person_attribute_type_uuid = :attribute " +
                "AND (((a.first_name LIKE '%' || :searchString || '%') OR (a.last_name LIKE '%' || :searchString || '%') OR (a.first_name|| ' ' || a.last_name LIKE '%' || :searchString || '%') OR (a.openmrs_id = :searchString)) " +
                "OR b.patientuuid IN (SELECT c.patientuuid FROM tbl_patient_attribute as c WHERE c.person_attribute_type_uuid = :phoneNumberAttribute AND c.value = :searchString)) " +
                "GROUP BY a.uuid"
    )
    suspend fun queryPatientsAndAttributesForSearchString(
        attribute: String,
        searchString: String,
        phoneNumberAttribute: String
    ): List<PatientWithAttribute>

}