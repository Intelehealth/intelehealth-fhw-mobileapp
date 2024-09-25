package org.intelehealth.video

import com.codeglo.coyamore.data.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.intelehealth.video.data.CallLogRepository
import org.intelehealth.video.model.RtcCallLog
import org.intelehealth.video.utils.CallStatus
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

    fun saveLog(callLog: RtcCallLog) {
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