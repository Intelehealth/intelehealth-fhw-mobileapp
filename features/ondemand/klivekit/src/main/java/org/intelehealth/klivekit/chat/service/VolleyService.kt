package org.intelehealth.klivekit.chat.service

import android.util.Log
import com.android.volley.Request.Method
import com.android.volley.toolbox.JsonObjectRequest
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity
import org.intelehealth.klivekit.utils.Constants

/**
 * Created by Vaghela Mithun R. on 19-07-2023 - 00:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class VolleyService {
    fun getAllMessages(fromUuid: String, toUuid: String, patientUuid: String) {
        val url = Constants.GET_ALL_MESSAGE_URL + fromUuid + "/" + toUuid + "/" + patientUuid
        val jsonObjectRequest = JsonObjectRequest(Method.GET, url, null,
            { response ->

            }) { error ->
        }
    }
}