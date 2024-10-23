package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.VisitAttribute

interface VisitAttributeDao : CoreDao<VisitAttribute> {

    @Query("SELECT * FROM tbl_visit_attribute WHERE visitUuid = :visitUuid")
    fun getVisitAttributesByVisitUuid(visitUuid: String): LiveData<List<VisitAttribute>>

}