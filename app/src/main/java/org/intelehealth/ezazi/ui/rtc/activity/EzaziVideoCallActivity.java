package org.intelehealth.ezazi.ui.rtc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.ajalt.timberkt.Timber;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.databinding.ActivityVideoCallEzaziBinding;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.ui.activity.CoreVideoCallActivity;
import org.intelehealth.klivekit.utils.CallType;
import org.intelehealth.klivekit.utils.RemoteActionType;
import org.intelehealth.klivekit.utils.RtcUtilsKt;

import io.livekit.android.renderer.SurfaceViewRenderer;
import io.livekit.android.renderer.TextureViewRenderer;
import io.livekit.android.room.participant.ConnectionQuality;
import io.livekit.android.room.track.CameraPosition;
import io.livekit.android.room.track.VideoTrack;

/**
 * Created by Vaghela Mithun R. on 29-06-2023 - 10:22.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

public class EzaziVideoCallActivity extends CoreVideoCallActivity {

    public static void startVideoCallActivity(Context context, RtcArgs args) {

        Log.e(TAG, "startVideoCallActivity: " + new Gson().toJson(args));
        args.setUrl(BuildConfig.LIVE_KIT_URL);
        args.setActionType(RemoteActionType.VIDEO_CALL.name());
        args.setSocketUrl(BuildConfig.SOCKET_URL + "?userId=" + args.getNurseId() + "&name=" + args.getNurseName());

        Intent intent = new Intent(context, EzaziVideoCallActivity.class);
        intent.putExtra(RtcUtilsKt.RTC_ARGS, args);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        int callState = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
        if (callState == TelephonyManager.CALL_STATE_IDLE) {
            context.startActivity(intent);
        }
    }

    public static final String TAG = "LiveVideoCallActivity";

    private ActivityVideoCallEzaziBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityVideoCallEzaziBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        binding.videoCallView.selfSurfaceView.setZOrderMediaOverlay(true);
//        binding.videoCallView.selfSurfaceView.setZOrderOnTop(true);
//        binding.videoCallView.selfSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
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
            CallType callType = CallType.OUTGOING;
            if (args.isIncomingCall()) callType = CallType.INCOMING;
            binding.incomingCallView.callingHintsTv.setText(callType.getValue());
            binding.incomingCallView.callerNameTv.setText(doctorName);
            binding.incomingCallView.tvCallerIdentity.setText(String.valueOf(args.getDoctorName().toCharArray()[0]));
            binding.videoCallView.tvRemoteUsername.setText(doctorName);
            binding.videoCallView.remoteUserTextIcon.setText(String.valueOf(args.getDoctorName().toUpperCase().toCharArray()[0]));
            binding.videoCallView.localUserTextIcon.setText(String.valueOf(args.getNurseName().toUpperCase().toCharArray()[0]));
        }
    }

    @Override
    public void attachLocalVideo(@NonNull VideoTrack videoTrack) {
        Timber.tag(TAG).e("attachLocalVideo: " + videoTrack.getName());
        binding.videoCallView.selfSurfaceView.setVisibility(View.VISIBLE);
        videoTrack.addRenderer(binding.videoCallView.selfSurfaceView);
    }

    @Override
    public void attachRemoteVideo(@NonNull VideoTrack videoTrack) {
        Timber.tag(TAG).e("attachRemoteVideo: %s", videoTrack.getEnabled());
        videoTrack.addRenderer(binding.videoCallView.incomingSurfaceView);
    }

    @Override
    public void onParticipantConnected(@Nullable Boolean it) {
        super.onParticipantConnected(it);
        if (Boolean.TRUE.equals(it)) {
            binding.videoCallView.incomingSurfaceView.setVisibility(View.VISIBLE);
            if (!args.isIncomingCall()) {
                onCallAccept();
                stopRingtone();
            }
        }
    }

    @NonNull
    @Override
    public TextureViewRenderer getLocalVideoRender() {
        return binding.videoCallView.selfSurfaceView;
    }

    @NonNull
    @Override
    public SurfaceViewRenderer getRemoteVideoRender() {
        binding.videoCallView.selfSurfaceView.setVisibility(View.VISIBLE);
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
    public void onGoingCall() {
        super.onGoingCall();
        binding.incomingCallView.getRoot().setVisibility(View.VISIBLE);
        binding.incomingCallView.rippleBackgroundContent.startRippleAnimation();
        binding.incomingCallView.fabAcceptCall.setVisibility(View.GONE);
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
        binding.videoCallView.selfSurfaceView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        binding.videoCallView.frameLocalVideoOverlay.setVisibility(!enabled ? View.VISIBLE : View.GONE);
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
        binding.videoCallView.incomingSurfaceView.setVisibility(isHide ? View.GONE : View.VISIBLE);
        binding.videoCallView.frameRemoteVideoOverlay.setVisibility(isHide ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRemoteParticipantMicChange(boolean isMuted) {
        super.onRemoteParticipantMicChange(isMuted);
        Drawable drawable = getCurrentMicStatusIcon(isMuted);
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
