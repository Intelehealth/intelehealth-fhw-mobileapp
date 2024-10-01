package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.PatientLocation

interface PatientLocationDao : CoreDao<PatientLocation> {

    @Query("SELECT * FROM tbl_location WHERE locationUuid = :uuid")
    fun getAllPatientsByLocationUuid(uuid: String): LiveData<List<PatientLocation>>

}