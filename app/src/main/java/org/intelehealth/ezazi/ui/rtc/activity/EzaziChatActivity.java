package org.intelehealth.ezazi.ui.rtc.activity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.ui.rtc.call.CallInitializer;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

/**
 * Created by Vaghela Mithun R. on 24-05-2023 - 18:34.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class EzaziChatActivity extends ChatActivity {
    @Override
    protected int getContentResourceId() {
        return R.layout.activity_chat_ezazi;
    }

    @Override
    protected void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        super.setupActionBar();
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
