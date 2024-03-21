package org.intelehealth.ncd.room.dao

import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.ncd.model.PatientAttributes

@Dao
interface PatientAttributeDao {

    @Query("SELECT * FROM tbl_patient_attribute WHERE patientuuid = :patientUuid AND person_attribute_type_uuid = :attributeUuid")
    suspend fun getPatientsBasedOnAttributeUuids(
        patientUuid: String,
        attributeUuid: String
    ): PatientAttributes

}