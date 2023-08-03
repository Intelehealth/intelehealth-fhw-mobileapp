package org.intelehealth.ezazi.ui.rtc.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.ui.rtc.call.CallInitializer;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity;
import org.intelehealth.klivekit.model.RtcArgs;

/**
 * Created by Vaghela Mithun R. on 24-05-2023 - 18:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class EzaziChatActivity extends ChatActivity {
    private static final String TAG = "EzaziChatActivity";

    public static void startChatActivity(Context context, RtcArgs args) {
        Intent chatIntent = new Intent(context, EzaziChatActivity.class);
        context.startActivity(buildExtra(chatIntent, args));
    }

    public static PendingIntent getPendingIntent(Context context, RtcArgs args) {
        Intent chatIntent = new Intent(context, EzaziChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, buildExtra(chatIntent, args),
                NotificationUtils.getPendingIntentFlag());
    }

    private static Intent buildExtra(Intent chatIntent, RtcArgs args) {
        chatIntent.putExtra("patientName", args.getPatientName());
        chatIntent.putExtra("visitUuid", args.getVisitId());
        chatIntent.putExtra("patientUuid", args.getPatientId());
        chatIntent.putExtra("fromUuid", args.getNurseId()); // provider uuid
        chatIntent.putExtra("isForVideo", false);
        chatIntent.putExtra("toUuid", args.getDoctorUuid());
        return chatIntent;
    }

    @Override
    protected int getContentResourceId() {
        return R.layout.activity_chat_ezazi;
    }

    @Override
    protected void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        super.setupActionBar();
        toolbar.setNavigationOnClickListener(v -> finishAfterTransition());
    }

    @Override
    protected Intent getVideoIntent() {
        return new Intent(this, EzaziVideoCallActivity.class);
    }

    @Override
    protected void initiateView() {
        mEmptyTextView = findViewById(R.id.empty_tv);
        mMessageEditText = findViewById(R.id.etMessageInput);
        mLoadingLinearLayout = findViewById(R.id.loading_layout);
        mEmptyLinearLayout = findViewById(R.id.empty_view);
        mRecyclerView = findViewById(R.id.rvConversation);
        mLayoutManager = new LinearLayoutManager(EzaziChatActivity.this, LinearLayoutManager.VERTICAL, true);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onVideoMenuClicked(RtcArgs args) {
        try {
            String patientOpenMrsId = new PatientsDAO().getOpenmrsId(args.getPatientId());
            args.setPatientOpenMrsId(patientOpenMrsId);
            args.setDoctorName(new ProviderDAO().getProviderName(args.getDoctorUuid()));
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
        args.setNurseName(new SessionManager(this).getChwname());
        new CallInitializer(args).initiateVideoCall(args1 -> EzaziVideoCallActivity.startVideoCallActivity(this, args1));
    }
}
