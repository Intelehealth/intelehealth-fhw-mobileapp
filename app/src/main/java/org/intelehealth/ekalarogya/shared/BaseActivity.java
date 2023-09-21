package org.intelehealth.ekalarogya.shared;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.intelehealth.ekalarogya.database.dao.ProviderDAO;
import org.intelehealth.ekalarogya.database.dao.RTCConnectionDAO;
import org.intelehealth.ekalarogya.models.dto.RTCConnectionDTO;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;
import org.intelehealth.ekalarogya.webrtc.activity.EkalChatActivity;
import org.intelehealth.ekalarogya.webrtc.notification.AppNotification;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 03-06-2023 - 19:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class BaseActivity extends AppCompatActivity implements SocketManager.NotificationListener {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketManager.getInstance().setNotificationListener(this);
    }

    @Override
    public void showNotification(@NonNull ChatMessage chatMessage) {
        RtcArgs args = new RtcArgs();
        args.setPatientName(chatMessage.getPatientName());
        args.setPatientId(chatMessage.getPatientId());
        args.setVisitId(chatMessage.getVisitId());
        args.setNurseId(chatMessage.getToUser());
        args.setDoctorUuid(chatMessage.getFromUser());
        Log.e(TAG, "showNotification: " + args.toJson());
        try {
            String title = new ProviderDAO().getProviderName(args.getDoctorUuid());
            new AppNotification.Builder(this)
                    .title(title)
                    .body(chatMessage.getMessage())
                    .pendingIntent(EkalChatActivity.getPendingIntent(this, args))
                    .send();

            saveChatInfoLog(args);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveChatInfoLog(RtcArgs args) throws DAOException {
        RTCConnectionDTO rtcDto = new RTCConnectionDTO();
        rtcDto.setUuid(UUID.randomUUID().toString());
        rtcDto.setVisitUUID(args.getVisitId());
        rtcDto.setConnectionInfo(args.getDoctorUuid());
        new RTCConnectionDAO().insert(rtcDto);
    }
}
