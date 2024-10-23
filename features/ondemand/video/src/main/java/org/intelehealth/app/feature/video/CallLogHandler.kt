package org.intelehealth.app.feature.video

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.intelehealth.core.utils.helper.PreferenceHelper
import org.intelehealth.app.feature.video.data.CallLogRepository
import org.intelehealth.app.feature.video.model.VideoCallLog
import org.intelehealth.app.feature.video.utils.CallStatus
import javax.inject.Inject

/**
 * Created by Vaghela Mithun R. on 20-10-2023 - 16:55.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogHandler @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val preferenceHelper: PreferenceHelper
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun saveLog(callLog: VideoCallLog) {
        scope.launch {
            callLogRepository.saveLog(callLog)
        }
    }

    fun changCallStatus(status: CallStatus) {
        scope.launch {
            val lastRecordId = callLogRepository.getLastRecordId()
            callLogRepository.changeCallLogStatus(lastRecordId, status)
        }
    }
}