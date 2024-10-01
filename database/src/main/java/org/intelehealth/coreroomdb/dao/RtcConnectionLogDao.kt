package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.RtcConnectionLog

interface RtcConnectionLogDao : CoreDao<RtcConnectionLog> {

    @Query("SELECT * FROM tbl_rtc_connection_log WHERE visitUuid = :visitUuid")
    fun getConnectionLogByVisitUuid(visitUuid: String): LiveData<List<RtcConnectionLog>>

}