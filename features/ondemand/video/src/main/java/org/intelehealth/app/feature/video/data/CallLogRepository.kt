package org.intelehealth.app.feature.video.data

import androidx.lifecycle.asLiveData
import org.intelehealth.app.feature.video.room.dao.VideoCallLogDao
import org.intelehealth.app.feature.video.model.VideoCallLog
import org.intelehealth.app.feature.video.utils.CallStatus


/**
 * Created by Vaghela Mithun R. on 20-10-2023 - 15:24.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogRepository(private val callLogDao: VideoCallLogDao) {
    suspend fun saveLog(callLog: VideoCallLog) = callLogDao.addCallLog(callLog)

    fun getCallLogs() = callLogDao.getAll().asLiveData()

    suspend fun changeCallLogStatus(callLogId: Long, status: CallStatus) =
        callLogDao.changeCallStatus(callLogId = callLogId, status = status)

    suspend fun clearLogs() = callLogDao.deleteAll()

    suspend fun getLastRecordId() = callLogDao.getLastRecordId()
}