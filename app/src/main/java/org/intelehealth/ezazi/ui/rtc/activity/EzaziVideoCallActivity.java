package org.intelehealth.ezazi.ui.rtc.activity;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.databinding.ActivityVideoCallEzaziBinding;
import org.intelehealth.klivekit.ui.activity.CoreVideoCallActivity;

import io.livekit.android.renderer.SurfaceViewRenderer;
import io.livekit.android.room.participant.ConnectionQuality;
import io.livekit.android.room.track.CameraPosition;
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
            initClickListener();
        }

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void setupActionBar() {
        setSupportActionBar(binding.actionBarView.toolbar);
        binding.actionBarView.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );
    }

    private void initClickListener() {
        binding.videoCallView.callActionView.btnCallEnd.setOnClickListener(view -> endCall());
        binding.videoCallView.callActionView.btnMicOnOff.setOnClickListener(view -> getVideoCallViewModel().toggleMicrophone());
        binding.videoCallView.callActionView.btnVideoOnOff.setOnClickListener(view -> getVideoCallViewModel().toggleCamera());
        binding.videoCallView.callActionView.btnFlipCamera.setOnClickListener(view -> getVideoCallViewModel().flipCamera());
        binding.incomingCallView.fabDeclineCall.setOnClickListener(view -> declineCall());
        binding.incomingCallView.fabAcceptCall.setOnClickListener(view -> acceptCall());
    }

    private void initView() {
        if (args != null && args.getDoctorName() != null) {
            String doctorName = args.getDoctorName();
            if (!args.getDoctorName().startsWith("Dr")) {
                doctorName = "Dr." + doctorName;
            }
            String callType = "Ongoing Call";
            if (args.isIncomingCall()) callType = "Incoming Call";
            binding.incomingCallView.callingHintsTv.setText(callType);
            binding.incomingCallView.callerNameTv.setText(doctorName);
            binding.incomingCallView.tvCallerIdentity.setText(String.valueOf(args.getDoctorName().toCharArray()[0]));
            binding.videoCallView.tvRemoteUsername.setText(doctorName);
        }
    }

    @Override
    public void attachLocalVideo(@NonNull VideoTrack videoTrack) {
        videoTrack.addRenderer(binding.videoCallView.selfSurfaceView);
    }

    @Override
    public void attachRemoteVideo(@NonNull VideoTrack videoTrack) {
        binding.videoCallView.incomingSurfaceView.setVisibility(View.VISIBLE);
        videoTrack.addRenderer(binding.videoCallView.incomingSurfaceView);
    }

    @NonNull
    @Override
    public SurfaceViewRenderer getLocalVideoRender() {
        return binding.videoCallView.selfSurfaceView;
    }

    @NonNull
    @Override
    public SurfaceViewRenderer getRemoteVideoRender() {
        return binding.videoCallView.incomingSurfaceView;
    }

    @Override
    public void onIncomingCall() {
        super.onIncomingCall();
        binding.incomingCallView.getRoot().setVisibility(View.VISIBLE);
        binding.incomingCallView.rippleBackgroundContent.startRippleAnimation();
        playRingtone();
    }

    @Override
    public void onCallAccept() {
        super.onCallAccept();
        binding.incomingCallView.getRoot().setVisibility(View.GONE);
        binding.incomingCallView.rippleBackgroundContent.stopRippleAnimation();
    }

    @Override
    public void onCallEnd() {
        super.onCallEnd();
        binding.videoCallView.callActionView.btnCallEnd.performClick();
    }

    @Override
    public void onCallDecline() {
        super.onCallDecline();
        binding.incomingCallView.rippleBackgroundContent.stopRippleAnimation();
    }

    @Override
    public void onCallCountDownStart(@NonNull String duration) {
        super.onCallCountDownStart(duration);
    }

    @Override
    public void onCameraStatusChanged(boolean enabled) {
        super.onCameraStatusChanged(enabled);
        binding.videoCallView.callActionView.btnVideoOnOff.setActivated(!enabled);
        binding.videoCallView.selfSurfaceView.setVisibility(!enabled ? View.VISIBLE : View.GONE);
        binding.videoCallView.ivSelfProfile.setVisibility(!enabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCameraPositionChanged(@NonNull CameraPosition cameraPosition) {
        super.onCameraPositionChanged(cameraPosition);
        binding.videoCallView.callActionView.btnFlipCamera.setActivated(
                cameraPosition == CameraPosition.BACK);
    }

    @Override
    public void onLocalParticipantSpeaking(boolean isSpeaking) {
        super.onLocalParticipantSpeaking(isSpeaking);
        binding.videoCallView.ivLocalSpeakerStatus.setActivated(isSpeaking);
    }

    @Override
    public void onMicrophoneStatusChanged(boolean status) {
        super.onMicrophoneStatusChanged(status);
        binding.videoCallView.callActionView.btnMicOnOff.setActivated(!status);
        Drawable drawable = getCurrentMicStatusIcon(!status);
        binding.videoCallView.ivLocalSpeakerStatus.setImageDrawable(drawable);
    }

    @Override
    public void onConnectivityChanged(@Nullable ConnectionQuality it) {
        super.onConnectivityChanged(it);
        binding.videoCallView.statusTv.setVisibility(it == ConnectionQuality.POOR ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRemoteParticipantCameraChange(boolean isHide) {
        super.onRemoteParticipantCameraChange(isHide);
        binding.videoCallView.incomingSurfaceView.setVisibility(isHide ? View.VISIBLE : View.GONE);
        binding.videoCallView.ivSelfProfile.setVisibility(!isHide ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRemoteParticipantMicChange(boolean isMuted) {
        super.onRemoteParticipantMicChange(isMuted);
        Drawable drawable = getCurrentMicStatusIcon(!isMuted);
        binding.videoCallView.ivRemoteSpeakerStatus.setImageDrawable(drawable);
    }

    @Override
    public void onRemoteParticipantSpeaking(boolean isSpeaking) {
        super.onRemoteParticipantSpeaking(isSpeaking);
        binding.videoCallView.ivRemoteSpeakerStatus.setActivated(isSpeaking);
    }

    private Drawable getCurrentMicStatusIcon(boolean isMuted) {
        if (isMuted) {
            return ContextCompat.getDrawable(this, R.drawable.ic_mic_off);
        } else {
            return ContextCompat.getDrawable(this, R.drawable.selector_active_speaker);
        }
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
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
