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
    @Query("SELECT * FROM call_log")
    fun getAll(): Flow<List<RtcCallLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCallLog(callLog: RtcCallLog)

    @Query("UPDATE call_log SET callStatus = :status where callLogId = :callLogId")
    suspend fun changeCallStatus(callLogId: Int, status: CallStatus)

    @Query("DELETE FROM call_log")
    suspend fun deleteAll()
}