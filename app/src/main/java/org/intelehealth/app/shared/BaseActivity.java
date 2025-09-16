package org.intelehealth.app.shared;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.ajalt.timberkt.Timber;
import com.google.gson.Gson;

import org.intelehealth.app.activities.visit.VisitActivity;
import org.intelehealth.app.ayu.visit.notification.MyNotificationManager;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.ui.language.activity.LanguageActivity;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.IDAChatActivity;
import org.intelehealth.app.webrtc.notification.AppNotification;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

import java.util.UUID;

/**
 * Created by Vaghela Mithun R. on 03-06-2023 - 19:29.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class BaseActivity extends LanguageActivity implements SocketManager.NotificationListener {
    private static final String TAG = "BaseActivity";
    private FeatureActiveStatus featureActiveStatus;
    private MyNotificationManager notificationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketManager.getInstance().setNotificationListener(this);
        notificationManager = new MyNotificationManager(this);

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("SHOW_DOCTOR_PRESCRIPTION_NOTIFICATION_BACKGROUND", false)) {
            String title = intent.getStringExtra("PRESCRIPTION_NOTIFICATION_TITLE");
            String subtitle = intent.getStringExtra("PRESCRIPTION_NOTIFICATION_SUBTITLE");

            if (title == null) title = "";
            if (subtitle == null) subtitle = "";

            showGlobalNotification(title, subtitle);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                prescriptionNotificationReceiver,
                new IntentFilter("SHOW_DOCTOR_PRESCRIPTION_NOTIFICATION_FOREGROUND")
        );
    }

    private final BroadcastReceiver prescriptionNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String title = intent.getStringExtra("PRESCRIPTION_NOTIFICATION_TITLE");
                String subtitle = intent.getStringExtra("PRESCRIPTION_NOTIFICATION_SUBTITLE");

                if (title == null) title = "";
                if (subtitle == null) subtitle = "";

                showGlobalNotification(title, subtitle);
            }
        }
    };

    /**
     * This method will load the active/deactivate status of overall application feature
     * like appointment = false, vital = false means as per this status we need to hide/show
     * the specific feature from the app
     */
    protected void loadFeatureActiveStatus() {
//        FeatureActiveStatusRepository repository = new FeatureActiveStatusRepository(ConfigDatabase.getInstance(this).featureActiveStatusDao());
//        FeatureActiveStatusViewModelFactory factory = new FeatureActiveStatusViewModelFactory(repository);
//        FeatureActiveStatusViewModel featureActiveStatusViewModel = new ViewModelProvider(this, factory).get(FeatureActiveStatusViewModel.class);
//        featureActiveStatusViewModel.fetchFeaturesActiveStatus().observe(this, featureActiveStatus -> {
//            if (featureActiveStatus != null) onFeatureActiveStatusLoaded(featureActiveStatus);
//        });
        onFeatureActiveStatusLoaded(FeatureActiveStatus.Companion.getDefaultFeatureStatus());
    }

    public void showGlobalNotification(String title, String subTitle) {
        if (notificationManager != null) {
            notificationManager.showNotification(
                    title,
                    subTitle,
                    () -> {
                        Intent intent = new Intent(this, VisitActivity.class);
                        startActivity(intent);
                        unregisterPrescriptionNotificationReceiver();
                        return null;
                    }
            );
        }
    }

    @Override
    public void showNotification(@NonNull ChatMessage chatMessage) {
        //commented this code beacuse getting featureActiveStatus null and not showing chat notification -AEAT-1907
        //if (featureActiveStatus != null && featureActiveStatus.getChatSection()) {
            RtcArgs args = new RtcArgs();
            args.setPatientName(chatMessage.getPatientName());
            args.setPatientId(chatMessage.getPatientId());
            args.setVisitId(chatMessage.getVisitId());
            args.setNurseId(chatMessage.getToUser());
            args.setDoctorUuid(chatMessage.getFromUser());
            try {
                String title = new ProviderDAO().getProviderName(args.getDoctorUuid(), ProviderDTO.Columns.USER_UUID.value);
                new AppNotification.Builder(this)
                        .title(title)
                        .body(chatMessage.getMessage())
                        .pendingIntent(IDAChatActivity.getPendingIntent(this, args))
                        .send();

                saveChatInfoLog(args.getVisitId(), args.getDoctorUuid());
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
       /// }
    }

    private void saveChatInfoLog(String visitId, String doctorId) throws DAOException {
        RTCConnectionDTO rtcDto = new RTCConnectionDTO();
        rtcDto.setUuid(UUID.randomUUID().toString());
        rtcDto.setVisitUUID(visitId);
        rtcDto.setConnectionInfo(doctorId);
        new RTCConnectionDAO().insert(rtcDto);
    }

    @Override
    public void saveTheDoctor(@NonNull ChatMessage chatMessage) {
        try {
            saveChatInfoLog(chatMessage.getVisitId(), chatMessage.getFromUser());
        } catch (DAOException e) {
            Timber.tag(TAG).e(e.getThwStack(), "saveTheDoctor: ");
        }
    }

    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        featureActiveStatus = activeStatus;
        Timber.tag(TAG).d("Active feature status=>%s", new Gson().toJson(activeStatus));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void unregisterPrescriptionNotificationReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(prescriptionNotificationReceiver);
    }

}
