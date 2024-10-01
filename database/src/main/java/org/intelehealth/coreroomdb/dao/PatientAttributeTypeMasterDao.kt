package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.PatientAttributeTypeMaster

interface PatientAttributeTypeMasterDao : CoreDao<PatientAttributeTypeMaster> {

    @Query("SELECT name FROM tbl_patient_attribute_master WHERE uuid = :uuid")
    fun getAttributeNameByUuid(uuid: String): LiveData<List<PatientAttributeTypeMaster>>

    @Query("SELECT uuid FROM tbl_patient_attribute_master WHERE name = :name")
    fun getAttributeUuidByName(name: String): LiveData<List<PatientAttributeTypeMaster>>

}