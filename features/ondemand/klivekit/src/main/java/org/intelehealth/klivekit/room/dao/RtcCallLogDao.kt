package org.intelehealth.klivekit.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.call.utils.CallStatus


/**
 * Created by Vaghela Mithun R. on 04-01-2023 - 15:58.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
@Dao
interface RtcCallLogDao {
    @Query("SELECT * FROM call_log ORDER BY callTime DESC")
    fun getAll(): Flow<List<RtcCallLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCallLog(callLog: RtcCallLog)

    @Query("UPDATE call_log SET callStatus = :status, callTime = :updatedOn where callLogId = :callLogId")
    suspend fun changeCallStatus(
        callLogId: Long,
        status: CallStatus,
        updatedOn: String = System.currentTimeMillis().toString()
    )

    @Query("DELETE FROM call_log")
    suspend fun deleteAll()

    @Query("SELECT callLogId FROM call_log ORDER BY callLogId  DESC LIMIT 1")
    suspend fun getLastRecordId(): Long
}