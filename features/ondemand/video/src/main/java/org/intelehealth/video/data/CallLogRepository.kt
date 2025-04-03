package org.intelehealth.video.data

import androidx.lifecycle.asLiveData
import org.intelehealth.klivekit.room.dao.RtcCallLogDao
import org.intelehealth.video.model.RtcCallLog
import org.intelehealth.video.utils.CallStatus


/**
 * Created by Vaghela Mithun R. on 20-10-2023 - 15:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogRepository(private val callLogDao: RtcCallLogDao) {
    suspend fun saveLog(callLog: RtcCallLog) = callLogDao.addCallLog(callLog)

    fun getCallLogs() = callLogDao.getAll().asLiveData()

    suspend fun changeCallLogStatus(callLogId: Long, status: CallStatus) =
        callLogDao.changeCallStatus(callLogId = callLogId, status = status)

    suspend fun clearLogs() = callLogDao.deleteAll()

    suspend fun getLastRecordId() = callLogDao.getLastRecordId()
}