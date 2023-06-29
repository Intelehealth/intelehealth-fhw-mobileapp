package org.intelehealth.ezazi.ui.rtc.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.ActivityVideoCallEzaziBinding;
import org.intelehealth.klivekit.ui.activity.CoreVideoCallActivity;

import io.livekit.android.renderer.SurfaceViewRenderer;
import io.livekit.android.room.track.VideoTrack;

/**
 * Created by Vaghela Mithun R. on 29-06-2023 - 10:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class EzaziVideoCallActivity extends CoreVideoCallActivity {
    public static final String TAG = "LiveVideoCallActivity";

    private ActivityVideoCallEzaziBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityVideoCallEzaziBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        super.onCreate(savedInstanceState);

        if (isArgsInitiate()) {
            setupActionBar();
            initView();
//            initClickListener();
        }

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void setupActionBar() {
        setSupportActionBar(binding.actionBarView.toolbar);
        binding.actionBarView.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );
    }

//    private void initClickListener() {
//        binding.videoCallView.callActionView.btnCallEnd.setOnClickListener(view->endCall());
//        binding.videoCallView.callActionView.btnMicOnOff.setOnClickListener(view->videoCallViewModel.toggleMicrophone());
//        binding.videoCallView.callActionView.btnVideoOnOff.setOnClickListener { videoCallViewModel.toggleCamera() }
//        binding.videoCallView.callActionView.btnFlipCamera.setOnClickListener { videoCallViewModel.flipCamera() }
//        binding.incomingCallView.inCallRejectImv.setOnClickListener { declineCall() }
//        binding.incomingCallView.inCallRejectImv.setOnClickListener { acceptCall() }
//    }

    private void initView() {
        if (args != null && args.getDoctorName() != null) {
            String doctorName = args.getDoctorName();
            if (!args.getDoctorName().startsWith("Dr")) {
                doctorName = "Dr." + doctorName;
            }
            String callType = "Ongoing Call";
            if (args.isIncomingCall()) callType = "Incoming Call";
            binding.incomingCallView.callerNameTv.setText(callType);
            binding.videoCallView.tvRemoteUsername.setText(doctorName);
        }
    }

    @Override
    public void attachLocalVideo(@NonNull VideoTrack videoTrack) {

    }

    @Override
    public void attachRemoteVideo(@NonNull VideoTrack videoTrack) {

    }

    @NonNull
    @Override
    public SurfaceViewRenderer getLocalVideoRender() {
        return null;
    }

    @NonNull
    @Override
    public SurfaceViewRenderer getRemoteVideoRender() {
        return null;
    }

    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            showAppClosingDialog();
        }
    };

    private void showAppClosingDialog() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.call_end_aler_txt)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sayBye("app");
                    }
                }).setNegativeButton(R.string.no, null).create().show();
    }
}
