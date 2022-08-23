package org.intelehealth.app.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.utilities.exception.DAOException;

public class RTCMessageReceiver extends BroadcastReceiver {
    private static final String TAG = RTCMessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.logV(TAG, "onReceive");
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent != null) {
            RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
            RTCConnectionDTO rtcConnectionDTO = new RTCConnectionDTO();
            rtcConnectionDTO.setUuid(new UuidGenerator().UuidGenerator());
            rtcConnectionDTO.setVisitUUID(intent.getStringExtra("visit_uuid"));
            rtcConnectionDTO.setConnectionInfo(intent.getStringExtra("connection_info"));
            try {
                rtcConnectionDAO.insert(rtcConnectionDTO);
            } catch (DAOException e) {
                e.printStackTrace();
            }
        }

    }
}