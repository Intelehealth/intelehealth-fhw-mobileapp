package org.intelehealth.app.feature.video.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.intelehealth.app.feature.video.model.VideoCallLog
import org.intelehealth.app.feature.video.utils.CallStatus


/**
 * Created by Vaghela Mithun R. on 04-01-2023 - 15:58.
 * Email : vaghela@codeglo.com
 * Mob   : +919727206702
 **/
@Dao
interface VideoCallLogDao {
    @Query("SELECT * FROM call_log ORDER BY callTime DESC")
    fun getAll(): Flow<List<VideoCallLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCallLog(callLog: VideoCallLog)

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