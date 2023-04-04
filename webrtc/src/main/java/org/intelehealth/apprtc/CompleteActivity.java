package org.intelehealth.apprtc;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;
import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.apprtc.adapter.ChatListingAdapter;
import org.intelehealth.apprtc.data.Constants;
import org.intelehealth.apprtc.databinding.ActivitySamplePeerConnectionBinding;
import org.intelehealth.apprtc.utils.AwsS3Utils;
import org.intelehealth.apprtc.utils.BitmapUtils;
import org.intelehealth.apprtc.utils.RealPathUtil;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;

public class CompleteActivity extends AppCompatActivity {
    private static final String TAG = "CompleteActivity";
    private static final int RC_CALL = 111;

    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    public static final String VIDEO_TRACK_TYPE = "video";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL = "WebRTC-FlexFEC-03/Enabled/";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int BPS_IN_KBPS = 1000;

    public static final int VIDEO_RESOLUTION_WIDTH = 1280 / 3;
    public static final int VIDEO_RESOLUTION_HEIGHT = 720 / 3;
    public static final int FPS = 24;
    public static final String CALL_END_FROM_WEB_INTENT_ACTION = "org.intelehealth.app.CALL_END_FROM_WEB_INTENT_ACTION";

    private Socket socket;
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;
    private boolean mIsStartNewCall = false;


    MediaConstraints audioConstraints;
    MediaConstraints videoConstraints;
    MediaConstraints sdpConstraints;
    VideoSource videoSource;
    VideoTrack localVideoTrack;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    SurfaceTextureHelper surfaceTextureHelper;

    private ActivitySamplePeerConnectionBinding binding;
    private PeerConnection peerConnection;
    private EglBase rootEglBase;
    private PeerConnectionFactory factory;
    private VideoTrack videoTrackFromCamera;

    VideoCapturer videoCapturer;
    //VideoSource videoSource;

    // incoming/partner video track
    private VideoTrack remoteVideoTrack;
    // incoming/partner audio track
    private AudioTrack remoteAudioTrack;

    // my camera mediaStream
    MediaStream mediaStream;

    //Firestore
    //FirebaseFirestore db = FirebaseFirestore.getInstance();
    // used for fliping the camera
    private boolean mIsReverseCamera = false;

    // my camera surface view VideoRenderer
    private VideoRenderer selfSurfaceViewVideoRenderer;
    // partener camera surface view VideoRenderer
    private VideoRenderer incomingSurfaceViewVideoRenderer;

    private String mRoomId = "foo";
    private String mDoctorName = "Doctor";
    private String mDoctorUUID = "Doctor";
    private String mNurseId = "Doctor";
    private boolean mIsInComingRequest = false;
    private Ringtone mRingtone;

    BroadcastReceiver broadcastReceiver;

    boolean mMicrophonePluggedIn = false;
    private JSONObject mRoomJsonObject = new JSONObject();
    private static final int WAIT_TIMER = 6 * 60 * 60 * 1000; // expecting max 6 hour call
    private TextView mTimerTextView;

    private boolean mIsChatWindowOpened = false;
    private BroadcastReceiver mCallEndBroadcastReceiver;
    private BroadcastReceiver mImageUrlFormatBroadcastReceiver;

    private CountDownTimer mCountDownTimer = new CountDownTimer(WAIT_TIMER, 1000) {

        public void onTick(long millisUntilFinished) {
            long timerMilli = WAIT_TIMER - millisUntilFinished;
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;

            long elapsedHours = timerMilli / hoursInMilli;
            timerMilli = timerMilli % hoursInMilli;

            long elapsedMinutes = timerMilli / minutesInMilli;
            timerMilli = timerMilli % minutesInMilli;

            long elapsedSeconds = timerMilli / secondsInMilli;

            String displayTimeString = String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
            binding.tvTimer.setText(displayTimeString);
        }

        public void onFinish() {

        }
    };

    private String mLastS3FormattedUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample_peer_connection);
        mImagePathRoot = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;

        mRoomId = getIntent().getStringExtra("roomId");
        mIsInComingRequest = getIntent().getBooleanExtra("isInComingRequest", false);
        if (getIntent().hasExtra("doctorname"))
            mDoctorName = getIntent().getStringExtra("doctorname");
        if (getIntent().hasExtra("nurseId"))
            mNurseId = getIntent().getStringExtra("nurseId");
        if (getIntent().hasExtra("doctorUUID"))
            mDoctorUUID = getIntent().getStringExtra("doctorUUID");

        if (getIntent().hasExtra("visitId"))
            mVisitUUID = getIntent().getStringExtra("visitId");

        if (getIntent().hasExtra("doctorId"))
            mDoctorUUID = getIntent().getStringExtra("doctorId");

        binding.tvDoctorName.setText(mDoctorName.startsWith("Dr.") ? mDoctorName : ("Dr. " + mDoctorName));
        mFromUUId = mNurseId;
        mToUUId = mDoctorUUID;
        mPatientUUid = mRoomId;
        try {
            mRoomJsonObject.put("room", mRoomId);
            mRoomJsonObject.put("connectToDrId", mDoctorUUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mCallEndBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.callEndImv.performClick();
                    }
                });
            }
        };
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(CALL_END_FROM_WEB_INTENT_ACTION);
        registerReceiver(mCallEndBroadcastReceiver, filterSend);

        mImageUrlFormatBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String url = intent.getStringExtra("fileUrl");
                if (url.equals(mLastS3FormattedUrl)) {
                    return;
                }
                mLastS3FormattedUrl = url;
                postMessages(mFromUUId, mToUUId, mPatientUUid, url, "attachment");
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AwsS3Utils.ACTION_FILE_UPLOAD_DONE);
        registerReceiver(mImageUrlFormatBroadcastReceiver, intentFilter);


        mRequestQueue = Volley.newRequestQueue(this);
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP app.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        mLayoutManager = new LinearLayoutManager(CompleteActivity.this, LinearLayoutManager.VERTICAL, true);
        binding.chatsRcv.setLayoutManager(mLayoutManager);

        binding.callerNameTv.setText(mDoctorName);
        binding.inCallAcceptImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.callingLayout.setVisibility(View.GONE);
                binding.rippleBackgroundContent.stopRippleAnimation();
                if (socket != null) {

                    initializeSurfaceViews();

                    initializePeerConnectionFactory();

                    createVideoTrackFromCameraAndShowIt();

                    initializePeerConnections();

                    startStreamingVideo();

                    socket.emit("create or join", mRoomId); // incoming
                    //socket.emit("create_or_join_hw", mRoomJsonObject); // outgoing
                }
                stopRinging();
            }
        });
        binding.inCallRejectImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socket != null) {
                    socket.emit("create or join", mRoomId);
                    //socket.emit("create_or_join_hw", mRoomId);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            socket.emit("no_answer");
                        }
                    }, 100);

                }
                binding.rippleBackgroundContent.stopRippleAnimation();
                stopRinging();

            }
        });

        binding.audioImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localAudioTrack != null) {
                    localAudioTrack.setEnabled(!localAudioTrack.enabled());
                    if (localAudioTrack.enabled()) {
                        binding.audioImv.setImageResource(R.drawable.vc_new_call_mic_icon);
                        Toast.makeText(CompleteActivity.this, getString(R.string.audio_on_lbl), Toast.LENGTH_SHORT).show();
                        binding.audioImv.setAlpha(1.0f);
                        binding.selfVoiceStatusIv.setImageResource(R.drawable.call_status_11);

                    } else {
                        binding.audioImv.setImageResource(R.drawable.vc_new_call_mic_icon);
                        Toast.makeText(CompleteActivity.this, getString(R.string.audio_off_lbl), Toast.LENGTH_SHORT).show();
                        binding.audioImv.setAlpha(0.2f);
                        binding.selfVoiceStatusIv.setImageResource(R.drawable.audio_stream_off);

                    }
                }

                if (socket != null) {
                    try {
                        socket.emit(localAudioTrack.enabled() ? "audioOn" : "audioOff", new JSONObject().put("fromWebapp", false));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        binding.callEndImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socket != null)
                    socket.emit("bye", "app");
                disconnectAll();

            }
        });
        binding.videoImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoTrackFromCamera != null) {
                    videoTrackFromCamera.setEnabled(!videoTrackFromCamera.enabled());
                    if (videoTrackFromCamera.enabled()) {
                        binding.videoImv.setImageResource(R.drawable.vc_new_v_camera_icon);
                        Toast.makeText(CompleteActivity.this, getString(R.string.video_on_lbl), Toast.LENGTH_SHORT).show();
                        binding.videoImv.setAlpha(1.0f);
                        binding.selfVideoOffLl.setVisibility(View.GONE);
                    } else {
                        binding.videoImv.setImageResource(R.drawable.vc_new_v_camera_icon);
                        Toast.makeText(CompleteActivity.this, getString(R.string.video_off_lbl), Toast.LENGTH_SHORT).show();
                        binding.videoImv.setAlpha(0.2f);
                        binding.selfVideoOffLl.setVisibility(View.VISIBLE);
                    }

                    if (socket != null) {
                        try {
                            socket.emit(videoTrackFromCamera.enabled() ? "videoOn" : "videoOff", new JSONObject().put("fromWebapp", false));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        binding.chatImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                        binding.videoContainerLl.getLayoutParams();
                RelativeLayout.LayoutParams incomingLayoutParams = (RelativeLayout.LayoutParams) binding.incomingSurfaceViewFrame.getLayoutParams();
                RelativeLayout.LayoutParams selfLayoutParams = (RelativeLayout.LayoutParams) binding.selfSurfaceViewFrame.getLayoutParams();

                if (mIsChatWindowOpened) {
                    binding.chatImv.setImageResource(R.drawable.vc_new_chat_icon);
                    binding.chatImv.setAlpha(1.0f);
                    mIsChatWindowOpened = false;
                    params.weight = 2.0f;
                    binding.centerLl.setVisibility(View.VISIBLE);
                    binding.centerVerticalLl.setVisibility(View.GONE);
                    binding.chatContainerLl.setVisibility(View.GONE);
                    incomingLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    selfLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                } else {
                    binding.chatImv.setImageResource(R.drawable.vc_new_chat_icon);
                    binding.chatImv.setAlpha(0.2f);
                    mIsChatWindowOpened = true;
                    params.weight = 1.0f;
                    binding.centerLl.setVisibility(View.GONE);
                    binding.centerVerticalLl.setVisibility(View.VISIBLE);
                    binding.chatContainerLl.setVisibility(View.VISIBLE);

                    incomingLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
                    selfLayoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;

                    incomingLayoutParams.addRule(RelativeLayout.START_OF, R.id.center_vertical_ll);
                    selfLayoutParams.addRule(RelativeLayout.END_OF, R.id.center_vertical_ll);

                    selfLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                }
                binding.videoContainerLl.setLayoutParams(params);
                binding.incomingSurfaceViewFrame.setLayoutParams(incomingLayoutParams);
                binding.selfSurfaceViewFrame.setLayoutParams(selfLayoutParams);
            }
        });
        binding.flipImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsReverseCamera = !mIsReverseCamera;
                // recreate the video track
                createVideoTrackFromCameraAndShowIt();
                // start again the video streaming
                startStreamingVideo();

            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                int status;
                if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    status = intent.getIntExtra("state", -1);
                    if (status == 0) {
                        mMicrophonePluggedIn = false;
                        setSpeakerphone();
                    }
                    if (status == 1) {
                        mMicrophonePluggedIn = true;
                        setSpeakerphone();
                    }
                }
            }
        };
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(broadcastReceiver, receiverFilter);


        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(mPhoneStateBroadcastReceiver, filter);
        start();
        if (mIsInComingRequest) {
            binding.callingLayout.setVisibility(View.VISIBLE);
            binding.rippleBackgroundContent.startRippleAnimation();
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            //mRingtone.setLooping(true);
            mRingtone.play();

        } else {
            binding.callingLayout.setVisibility(View.GONE);

        }

        mCountDownTimer.start();
        getAllMessages(false);
    }

    private void stopRinging() {
        if (mRingtone != null && mRingtone.isPlaying())
            mRingtone.stop();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage(R.string.call_end_aler_txt);
        alertdialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (socket != null)
                    socket.emit("bye", "app");
                else
                    finish();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.no, null);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        //IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

    }


    @Override
    protected void onDestroy() {
        disconnectAll();
        try {
            unregisterReceiver(mPhoneStateBroadcastReceiver);
            unregisterReceiver(mCallEndBroadcastReceiver);
            unregisterReceiver(mImageUrlFormatBroadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * Release all resources & close the scoket
     */
    private void disconnectAll() {
        try {
            if (socket != null) {
                socket.disconnect();
                socket = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CompleteActivity.this, getString(R.string.call_end_lbl), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (peerConnection != null) {
                peerConnection.dispose();
                peerConnection = null;
            }
            if (videoSource != null) {
                videoSource.dispose();
                videoSource = null;
            }
            if (localVideoTrack != null) {
                localVideoTrack.dispose();
                localVideoTrack = null;
            }
            if (surfaceTextureHelper != null) {
                surfaceTextureHelper.dispose();
                surfaceTextureHelper = null;
            }

            stopRinging();

            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            e.printStackTrace();
        }
        finish();

    }


    private void start() {
        if (checkAndRequestPermissions()) {
            connectToSignallingServer();

            if (!mIsInComingRequest) {

                initializeSurfaceViews();

                initializePeerConnectionFactory();

                createVideoTrackFromCameraAndShowIt();

                initializePeerConnections();

                startStreamingVideo();
            }
        }
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int recordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), RC_CALL);
            return false;
        }
        return true;
    }

    private void connectToSignallingServer() {
        try {
            String url = Constants.BASE_URL + "?userId=" + mNurseId + "&name=" + mNurseId;
            Log.v("url", url);
            socket = IO.socket(url);

            //socket emitter "call", you can listen on it after connection;
            // if you get any request on it; show incoming call screen;
            // if user accept it,
            // do same what you are doing on start call button(emitting - 'create or join' etc)
            // otherwise after 15 secs emit on "no answer"
            //
            // Video & Audio ON/OF emit listener
            socket.on("videoOn", args -> {
                Log.d(TAG, "videoOn event emit from web: ");
                for (Object arg : args) {
                    Log.d(TAG, "updateMessage: videoOn" + String.valueOf(arg));
                }
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    if (jsonObject.getBoolean("fromWebapp"))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.remoteVideoOffLl.setVisibility(View.GONE);
                                binding.tvDoctorName.setTextColor(getResources().getColor(R.color.white));
                            }
                        });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            socket.on("videoOff", args -> {
                Log.d(TAG, "videoOff event emit from web: ");
                for (Object arg : args) {
                    Log.d(TAG, "updateMessage: videoOff" + String.valueOf(arg));
                }
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    if (jsonObject.getBoolean("fromWebapp"))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.remoteVideoOffLl.setVisibility(View.VISIBLE);
                                binding.tvDoctorName.setTextColor(getResources().getColor(R.color.gray_4));
                            }
                        });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            socket.on("audioOn", args -> {
                Log.d(TAG, "audioOn event emit from web: ");
                for (Object arg : args) {
                    Log.d(TAG, "updateMessage: audioOn" + String.valueOf(arg));
                }
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    if (jsonObject.getBoolean("fromWebapp"))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.remoteVoiceStatusIv.setImageResource(R.drawable.call_status_11);
                            }
                        });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            socket.on("audioOff", args -> {
                Log.d(TAG, "audioOff event emit from web: ");
                for (Object arg : args) {
                    Log.d(TAG, "updateMessage: audioOff" + String.valueOf(arg));
                }
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    if (jsonObject.getBoolean("fromWebapp"))
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.remoteVoiceStatusIv.setImageResource(R.drawable.audio_stream_off);
                            }
                        });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
            // CHAT *************************************
            socket.on("isread", args -> {
                Log.d(TAG, "isread event emit from web: ");
                getAllMessages(false);
            });
            socket.on("updateMessage", args -> {
                try {
                    for (Object arg : args) {
                        Log.d(TAG, "updateMessage: " + String.valueOf(arg));
                    }

                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));

                    mFromUUId = jsonObject.getString("toUser");
                    mToUUId = jsonObject.getString("fromUser");
                    mPatientUUid = jsonObject.getString("patientId");
                    mVisitUUID = jsonObject.getString("visitId");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getAllMessages(false);


                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
            //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

            socket.on(EVENT_CONNECT, args -> {
                Log.d(TAG, "connectToSignallingServer: connect");
                //socket.emit("create or join", "foo");
                Log.v("RoomJsonObject", mRoomJsonObject.toString());
                if (!mIsInComingRequest)
                    socket.emit("create_or_join_hw", mRoomJsonObject);


            }).on("ipaddr", args -> {
                Log.d(TAG, "connectToSignallingServer: ipaddr");
            }).on("bye", args -> {
                Log.d(TAG, "connectToSignallingServer: bye");
                socket.emit("bye");
                disconnectAll();

            }).on("call", args -> {
                Log.d(TAG, "connectToSignallingServer: call");
                //socket.emit("create or join", mRoomId);
                socket.emit("create_or_join_hw", mRoomJsonObject);
            }).on("no_answer", args -> {
                Log.d(TAG, "connectToSignallingServer: no answer");
                socket.emit("bye");
                disconnectAll();
            }).on("created", args -> {
                Log.d(TAG, "connectToSignallingServer: created");
                isInitiator = true;
            }).on("full", args -> {
                Log.d(TAG, "connectToSignallingServer: full");
            }).on("join", args -> {
                for (int i = 0; i < args.length; i++) {
                    Log.d(TAG, "join - " + args[i]);
                }
                Log.d(TAG, "connectToSignallingServer: join");
                Log.d(TAG, "connectToSignallingServer: Another peer made a request to join room");
                Log.d(TAG, "connectToSignallingServer: This peer is the initiator of room");
                isChannelReady = true;
            }).on("joined", args -> {
                Log.d(TAG, "connectToSignallingServer: joined");
                for (Object arg : args) {
                    Log.d(TAG, "joined - " + arg);
                }
                isChannelReady = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.statusTv.setVisibility(View.GONE);
                    }
                });


            }).on("ready", args -> {

                for (Object arg : args) {
                    Log.d(TAG, "ready - " + arg);
                }
                Log.d(TAG, "connectToSignallingServer: ready");
                if (mIsInComingRequest) {
                    //socket.emit("ready");
                } else {
                    isChannelReady = true;
                    maybeStart();
                    //
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CompleteActivity.this, "Doctor Joined!", Toast.LENGTH_SHORT).show();
                            binding.statusTv.setVisibility(View.GONE);
                        }
                    });
                }

            }).on("log", args -> {
                for (Object arg : args) {
                    Log.d(TAG, "connectToSignallingServer: log" + String.valueOf(arg));
                }
            }).on("message", args -> {
                Log.d(TAG, "connectToSignallingServer: got a message");
                try {
                    if (args[0] instanceof String) {
                        String message = (String) args[0];
                        Log.d(TAG, "connectToSignallingServer: got String message " + message);
                        if (message.equals("got user media")) {
                            maybeStart();
                        }
                    } else {
                        JSONObject message = (JSONObject) args[0];
                        Log.d(TAG, "connectToSignallingServer: got message " + message);
                        if (message.getString("type").equals("offer")) {
                            Log.d(TAG, "connectToSignallingServer: received an offer " + isInitiator + " " + isStarted);
                            if (!isInitiator && !isStarted) {
                                maybeStart();
                            }
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                            doAnswer();
                        } else if (message.getString("type").equals("answer") && isStarted) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 1000);
                        } else if (message.getString("type").equals("candidate") && isStarted) {
                            Log.d(TAG, "connectToSignallingServer: receiving candidates");
                            //{"type":"candidate","candidate":{"candidate":"candidate:11 1 UDP 91953663 172.31.34.2 50457 typ relay raddr 172.31.34.2 rport 50457",
                            // "sdpMid":"audio","sdpMLineIndex":0,"usernameFragment":"2353e29e"}}
                            //IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        IceCandidate candidate = new IceCandidate(message.getJSONObject("candidate").getString("sdpMid"),
                                                message.getJSONObject("candidate").getInt("sdpMLineIndex"),
                                                message.getJSONObject("candidate").getString("candidate"));
                                        peerConnection.addIceCandidate(candidate);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 1000);

                        }
                        /*else if (message === 'bye' && isStarted) {
                        handleRemoteHangup();
                    }*/
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).on(EVENT_DISCONNECT, args -> {
                Log.d(TAG, "connectToSignallingServer: disconnect");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CompleteActivity.this, "Disconnected!", Toast.LENGTH_SHORT).show();

                    }
                });
                finish();
            });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private void doAnswer() {
        Log.v(TAG, "doAnswer()");
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new MediaConstraints());
    }

    private void maybeStart() {
        Log.d(TAG, "maybeStart: " + isStarted + " " + isChannelReady);
        if (!isStarted && isChannelReady) {
            isStarted = true;
            // if (isInitiator) {
            doCall();
            //}
        }
    }

    private void doCall() {
        Log.v(TAG, "doCall()");
        MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.d(TAG, "createOffer onCreateSuccess:()");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
        startStreamingVideo();
    }

    private void sendMessage(Object message) {
        socket.emit("message", message);
    }

    private void initializeSurfaceViews() {
        rootEglBase = EglBase.create();
        binding.selfSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        binding.selfSurfaceView.setEnableHardwareScaler(true);
        binding.selfSurfaceView.setMirror(true);

        binding.incomingSurfaceView.init(rootEglBase.getEglBaseContext(), null);
        binding.incomingSurfaceView.setEnableHardwareScaler(true);
        binding.incomingSurfaceView.setMirror(true);

        //add one more
    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }


    private void createVideoTrackFromCameraAndShowIt() {
        if (videoSource != null) videoSource.dispose();
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            videoCapturer.dispose();
        }
        audioConstraints = new MediaConstraints();
        videoCapturer = createVideoCapturer();
        videoSource = factory.createVideoSource(videoCapturer);
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        if (videoTrackFromCamera != null) videoTrackFromCamera.dispose();
        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        selfSurfaceViewVideoRenderer = new VideoRenderer(binding.selfSurfaceView);

        videoTrackFromCamera.addRenderer(selfSurfaceViewVideoRenderer);

        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("101", audioSource);
        localAudioTrack.setEnabled(true);


    }

    private void initializePeerConnections() {
        peerConnection = createPeerConnection(factory);
    }


    private void startStreamingVideo() {
        if (mediaStream != null) {
            mediaStream.removeTrack(videoTrackFromCamera);
            mediaStream.removeTrack(localAudioTrack);
            peerConnection.removeStream(mediaStream);
            //mediaStream.dispose();
        }
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);

        sendMessage("got user media");
    }

    private PeerConnection createPeerConnection(PeerConnectionFactory factory) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_1_URL));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_2_URL));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_3_URL, Constants.ICE_SERVER_3_USER, Constants.ICE_SERVER_3_PASSWORD));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_4_URL, Constants.ICE_SERVER_4_USER, Constants.ICE_SERVER_4_PASSWORD));


        /*Testing*/
        /*iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_1_URL));
        //iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_2_URL));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_3_URL, Constants.ICE_SERVER_3_USER, Constants.ICE_SERVER_3_PASSWORD));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_4_URL, Constants.ICE_SERVER_4_USER, Constants.ICE_SERVER_4_PASSWORD));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_5_URL, Constants.ICE_SERVER_5_USER, Constants.ICE_SERVER_5_PASSWORD));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_6_URL, Constants.ICE_SERVER_6_USER, Constants.ICE_SERVER_6_PASSWORD));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_7_URL, Constants.ICE_SERVER_7_USER, Constants.ICE_SERVER_7_PASSWORD));
        iceServers.add(new PeerConnection.IceServer(Constants.ICE_SERVER_8_URL, Constants.ICE_SERVER_8_USER, Constants.ICE_SERVER_8_PASSWORD));*/


        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: ");
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ");
            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.d(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ");
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "onIceCandidate: ");
                if (mIsInComingRequest) {
                    JSONObject message = new JSONObject();

                    try {
                        message.put("type", "candidate");
                        message.put("label", iceCandidate.sdpMLineIndex);
                        message.put("id", iceCandidate.sdpMid);
                        message.put("candidate", iceCandidate.sdp);

                        Log.d(TAG, "onIceCandidate: sending candidate " + message);
                        sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    JSONObject message = new JSONObject();
                    JSONObject candidate = new JSONObject();

                    try {
                        candidate.put("type", "candidate");
                        candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                        candidate.put("sdpMid", iceCandidate.sdpMid);
                        candidate.put("candidate", iceCandidate.sdp);
                        //candidate.put("usernameFragment", "123");

                        message.put("type", "candidate");
                        message.put("candidate", candidate);

                        Log.d(TAG, "onIceCandidate: sending candidate " + message);
                        sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.d(TAG, "onIceCandidatesRemoved: ");
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                remoteVideoTrack = mediaStream.videoTracks.get(0);
                remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);
                remoteVideoTrack.setEnabled(true);
                incomingSurfaceViewVideoRenderer = new VideoRenderer(binding.incomingSurfaceView);
                remoteVideoTrack.addRenderer(incomingSurfaceViewVideoRenderer);
                setSpeakerphone();

            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.d(TAG, "onRemoveStream: ");
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                Log.d(TAG, "onDataChannel: ");
            }

            @Override
            public void onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ");
            }
        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    /**
     * Sets the speaker phone mode.
     */
    private void setSpeakerphone() {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn && !mMicrophonePluggedIn) {
            return;
        }
        audioManager.setSpeakerphoneOn(!mMicrophonePluggedIn);
    }


    private VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(true));
        }

        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {

        final String[] deviceNames = enumerator.getDeviceNames();

        if (!mIsReverseCamera) {
            for (String deviceName : deviceNames) {
                Log.v("deviceNames", deviceName);
                if (enumerator.isFrontFacing(deviceName)) {
                    VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                    if (videoCapturer != null) {
                        return videoCapturer;
                    }
                    Log.v("deviceNames", "Front Selected");
                }
            }
        } else {
            for (String deviceName : deviceNames) {
                if (!enumerator.isFrontFacing(deviceName)) {
                    VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                    if (videoCapturer != null) {
                        return videoCapturer;
                    }
                    Log.v("deviceNames", "Back Selected");
                }
            }
        }

        return null;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this);
    }

    private BroadcastReceiver mPhoneStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
            if (callState == TelephonyManager.CALL_STATE_OFFHOOK) {
                if (socket != null) {
                    socket.emit("bye");
                }
            }
        }
    };

    /*CHAT*/

    public void sendMessageNow(View view) {
        hideSoftKeyboard();
        if (mToUUId.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_wait_for_doctor), Toast.LENGTH_SHORT).show();
            return;
        }
        String message = binding.textEtv.getText().toString().trim();
        if (!message.isEmpty()) {
            postMessages(mFromUUId, mToUUId, mPatientUUid, message, "text");
        } else {
            Toast.makeText(this, getString(R.string.empty_message_txt), Toast.LENGTH_SHORT).show();
        }
    }

    public void loadAttachment(View view) {
        validatePermissionAndIntent();
    }

    private String mLastSelectedImageName = "";


    private void cameraStart() {
        File file = new File(mImagePathRoot);
        final String imagePath = file.getAbsolutePath();
        final String imageName = UUID.randomUUID().toString();
        mLastSelectedImageName = imageName;
        Intent cameraIntent = new Intent(CompleteActivity.this, CameraActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
        }
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        //mContext.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
        mStartForCameraResult.launch(cameraIntent);
       /* Intent broadcast = new Intent();
        broadcast.setAction(Constants.IMAGE_CAPTURE_REQUEST_INTENT_ACTION);
        sendBroadcast(broadcast);*/
    }

    ActivityResultLauncher<Intent> mStartForCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the Intent
                        String currentPhotoPath = data.getStringExtra("RESULT");

                        Log.v(TAG, "currentPhotoPath : " + currentPhotoPath);
                        if (!RealPathUtil.isFileLessThan512Kb(new File(currentPhotoPath))) {
                            Toast.makeText(CompleteActivity.this, "Max doc size is 512 KB", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject inputJsonObject = new JSONObject();
                            inputJsonObject.put("fromUser", mFromUUId);
                            inputJsonObject.put("toUser", mToUUId);
                            inputJsonObject.put("patientId", mPatientUUid);
                            inputJsonObject.put("message", ".jpg");
                            inputJsonObject.put("type", "attachment");
                            inputJsonObject.put("isLoading", true);

                            addNewMessage(inputJsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        AwsS3Utils.saveFileToS3Cloud(CompleteActivity.this, mVisitUUID, currentPhotoPath);
                    }
                }
            });


    private void galleryStart() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mStartForGalleryResult.launch(intent);
    }

    private void browseStartForPdf() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("application/pdf");
        mStartForPDFResult.launch(chooseFile);
    }

    private static final int MY_CAMERA_REQUEST_CODE = 1001;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;

    private void selectImage() {
        final CharSequence[] options = {/*getString(R.string.take_photo_lbl),*/ getString(R.string.choose_from_gallery_lbl), "Choose Documents", getString(R.string.cancel_lbl)};
        AlertDialog.Builder builder = new AlertDialog.Builder(CompleteActivity.this);
        builder.setTitle("Select");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
               /* if (item == 0) {
                    if (mImageCount >= 5) {
                        Toast.makeText(CompleteActivity.this, "Maximum 5 Images you can send per one case", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cameraStart();

                } else*/
                if (item == 0) {
                    if (mImageCount >= 5) {
                        Toast.makeText(CompleteActivity.this, "Maximum 5 Images you can send per one case", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    galleryStart();

                } else if (item == 1) {
                    if (mPDFCount >= 2) {
                        Toast.makeText(CompleteActivity.this, "Maximum 2 documents you can send per one case", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    browseStartForPdf();

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void validatePermissionAndIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            //cameraStart();
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CALL) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                start();
            } else {
                Toast.makeText(CompleteActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                finish();
            }

        } else if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                cameraStart();
                selectImage();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    ActivityResultLauncher<Intent> mStartForGalleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String currentPhotoPath = "";
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            String[] filePath = {MediaStore.Images.Media.DATA};
                            Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                            c.moveToFirst();
                            int columnIndex = c.getColumnIndex(filePath[0]);
                            String picturePath = c.getString(columnIndex);
                            c.close();
                            //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                            Log.v("path", picturePath + "");

                            // copy & rename the file
                            String finalImageName = UUID.randomUUID().toString();
                            currentPhotoPath = mImagePathRoot + finalImageName + ".jpg";
                            BitmapUtils.copyFile(picturePath, currentPhotoPath);

                            // Handle the Intent


                            Log.i(TAG, currentPhotoPath);
                            if (!RealPathUtil.isFileLessThan512Kb(new File(currentPhotoPath))) {
                                Toast.makeText(CompleteActivity.this, "Max doc size is 512 KB", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                JSONObject inputJsonObject = new JSONObject();
                                inputJsonObject.put("fromUser", mFromUUId);
                                inputJsonObject.put("toUser", mToUUId);
                                inputJsonObject.put("patientId", mPatientUUid);
                                inputJsonObject.put("message", ".jpg");
                                inputJsonObject.put("type", "attachment");
                                inputJsonObject.put("isLoading", true);

                                addNewMessage(inputJsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            AwsS3Utils.saveFileToS3Cloud(CompleteActivity.this, mVisitUUID, currentPhotoPath);
                        } else {
                            Toast.makeText(CompleteActivity.this, "Unable to pick the gallery data!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
    ActivityResultLauncher<Intent> mStartForPDFResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String currentPDFPath = "";
                        try {
                            Uri uri = data.getData();
                            FileInputStream fileInputStream = (FileInputStream) getContentResolver().openInputStream(uri);
                            if (fileInputStream != null) {
                                byte[] fileBytesArray = new byte[fileInputStream.available()];
                                fileInputStream.read(fileBytesArray);
                                fileInputStream.close();

                                //TODO: Remove below code
                                //Below code is to test the above fileBytesArray is correct or not.
                                //Below we are creating a MainTest file in your internal storage (For that You need write Storage permission) and checking the content same as original file
                                //In-case of file type change .pdf to .txt or whatever type of file you are choosing
                                File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/TEMP/" +
                                        UUID.randomUUID().toString() + ".pdf");
                                OutputStream fileOutputStream = new FileOutputStream(mFile);
                                fileOutputStream.write(fileBytesArray);
                                fileOutputStream.close();
                                //End
                                currentPDFPath = mFile.getPath();
                                if (!RealPathUtil.isFileLessThan1MB(mFile)) {
                                    Toast.makeText(CompleteActivity.this, "Max doc size is 1MB", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.v(TAG, "currentPDFPath" + currentPDFPath);
                                try {
                                    JSONObject inputJsonObject = new JSONObject();
                                    inputJsonObject.put("fromUser", mFromUUId);
                                    inputJsonObject.put("toUser", mToUUId);
                                    inputJsonObject.put("patientId", mPatientUUid);
                                    inputJsonObject.put("message", ".pdf");
                                    inputJsonObject.put("LayoutType", "attachment");
                                    inputJsonObject.put("isLoading", true);

                                    addNewMessage(inputJsonObject);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                AwsS3Utils.saveFileToS3Cloud(CompleteActivity.this, mVisitUUID, currentPDFPath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                }
            });
    public String mImagePathRoot = "";

    public void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllMessages(boolean isAlreadySetReadStatus) {
        Log.v(TAG, "getAllMessages -mFromUUId - " + mFromUUId);
        Log.v(TAG, "getAllMessages -mToUUId - " + mToUUId);
        Log.v(TAG, "getAllMessages -mPatientUUid - " + mPatientUUid);
        if (mFromUUId.isEmpty() || mToUUId.isEmpty() || mPatientUUid.isEmpty()) {
            return;
        }
        binding.emptyTv.setText(getString(R.string.loading));
        String url = Constants.GET_ALL_MESSAGE_URL + mFromUUId + "/" + mToUUId + "/" + mPatientUUid;
        Log.v(TAG, url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "getAllMessages -response - " + response.toString());
                binding.emptyTv.setText(getString(R.string.you_have_no_messages_start_sending_messages_now));
                showChat(response, isAlreadySetReadStatus);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "getAllMessages - onErrorResponse - " + error.getMessage());
                binding.emptyTv.setText(getString(R.string.you_have_no_messages_start_sending_messages_now));
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private int mPDFCount = 0;
    private int mImageCount = 0;

    private void showChat(JSONObject response, boolean isAlreadySetReadStatus) {
        try {
            mChatList.clear();
            mPDFCount = 0;
            mImageCount = 0;
            if (response.getBoolean("success")) {
                JSONArray jsonArray = response.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject chatJsonObject = jsonArray.getJSONObject(i);
                    //Log.v(TAG, "showChat - " + chatJsonObject);
                    if (chatJsonObject.getString("fromUser").equals(mFromUUId)) {
                        chatJsonObject.put("LayoutType", Constants.RIGHT_ITEM_HW); // HW
                        if (chatJsonObject.getString("type").equalsIgnoreCase("attachment")) {
                            if (chatJsonObject.getString("message").endsWith(".pdf")) {
                                mPDFCount += 1;
                            } else {
                                mImageCount += 1;
                            }
                        }
                    } else {
                        chatJsonObject.put("LayoutType", Constants.LEFT_ITEM_DOCT); // DOCTOR
                    }
                    mChatList.add(chatJsonObject);
                }
                if (mChatList.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                }

                sortList();

                mChatListingAdapter = new ChatListingAdapter(this, mChatList, new ChatListingAdapter.AttachmentClickListener() {
                    @Override
                    public void onClick(String url) {
                        showImageOrPdf(url);
                    }
                });
                binding.chatsRcv.setAdapter(mChatListingAdapter);

                if (!mChatList.isEmpty()) {
                    JSONObject jsonObject = mChatList.get(0);
                    mVisitUUID = jsonObject.getString("visitId");
                    mPatientName = jsonObject.getString("patientName");
                    mPatientUUid = jsonObject.getString("patientId");


                    // save in db
                    JSONObject connectionInfoObject = new JSONObject();
                    connectionInfoObject.put("fromUUID", mFromUUId);
                    connectionInfoObject.put("toUUID", mToUUId);
                    connectionInfoObject.put("patientUUID", mPatientUUid);

                    Intent intent = new Intent(ACTION_NAME);
                    intent.putExtra("visit_uuid", mVisitUUID);
                    intent.putExtra("connection_info", connectionInfoObject.toString());
                    intent.setComponent(new ComponentName("org.intelehealth.app", "org.intelehealth.app.utilities.RTCMessageReceiver"));

                    getApplicationContext().sendBroadcast(intent);


                }
                if (!isAlreadySetReadStatus)
                    for (int i = 0; i < mChatList.size(); i++) {
                        //Log.v(TAG, "ID=" + mChatList.get(i).getString("id"));
                        if (mChatList.get(i).getInt("LayoutType") == Constants.LEFT_ITEM_DOCT && mChatList.get(i).getInt("isRead") == 0) {
                            setReadStatus(mChatList.get(i).getString("id"));
                            break;
                        }
                    }

            } /*else {
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                finish();
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sortList() {
        Collections.sort(mChatList, new Comparator<JSONObject>() {
            public int compare(JSONObject o1, JSONObject o2) {
                try {
                    Date a = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(o1.getString("createdAt"));
                    Date b = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(o2.getString("createdAt"));
                    return b.compareTo(a);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

    }

    private void postMessages(String fromUUId, String toUUId, String patientUUId, String message, String type) {
        try {

            JSONObject inputJsonObject = new JSONObject();
            inputJsonObject.put("fromUser", fromUUId);
            inputJsonObject.put("toUser", toUUId);
            inputJsonObject.put("patientId", patientUUId);
            inputJsonObject.put("message", message);
            inputJsonObject.put("patientName", mPatientName);
            inputJsonObject.put("hwName", "");
            inputJsonObject.put("patientPic", "");
            inputJsonObject.put("hwPic", "");
            inputJsonObject.put("visitId", mVisitUUID);
            inputJsonObject.put("isRead", false);
            inputJsonObject.put("type", type);
            binding.loadingLayout.setVisibility(View.VISIBLE);
            Log.v(TAG, "postMessages - inputJsonObject - " + inputJsonObject.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.SEND_MESSAGE_URL, inputJsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "postMessages - response - " + response.toString());
                    binding.textEtv.setText("");
                    getAllMessages(false);
                    binding.loadingLayout.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "postMessages - onErrorResponse - " + error.getMessage());
                    binding.loadingLayout.setVisibility(View.GONE);
                }
            });
            mRequestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setReadStatus(String messageID) {
        String url = Constants.SET_READ_STATUS_OF_MESSAGE_URL + messageID;
        Log.v(TAG, "setReadStatus - url - " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "setReadStatus - response - " + response.toString());
                getAllMessages(true);
                if (socket != null) socket.emit("isread");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "setReadStatus - onErrorResponse - " + error.getMessage());

            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void addNewMessage(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("fromUser").equals(mFromUUId)) {
                jsonObject.put("LayoutType", Constants.RIGHT_ITEM_HW);
            } else {
                jsonObject.put("LayoutType", Constants.LEFT_ITEM_DOCT);
            }
            if (!jsonObject.has("createdAt")) {
                SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                rawSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                jsonObject.put("createdAt", rawSimpleDateFormat.format(new Date()));
            }
            mChatList.add(jsonObject);

            binding.emptyView.setVisibility(View.GONE);
            sortList();

            if (mChatListingAdapter == null) {
                mChatListingAdapter = new ChatListingAdapter(this, mChatList, new ChatListingAdapter.AttachmentClickListener() {
                    @Override
                    public void onClick(String url) {
                        showImageOrPdf(url);
                    }
                });
                binding.chatsRcv.setAdapter(mChatListingAdapter);
            } else {
                mChatListingAdapter.refresh(mChatList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";
    private List<JSONObject> mChatList = new ArrayList<JSONObject>();
    private LinearLayoutManager mLayoutManager;
    private ChatListingAdapter mChatListingAdapter;


    private String mFromUUId = "";
    private String mToUUId = "";
    private String mPatientUUid = "";
    private String mVisitUUID = "";
    private String mPatientName = "";
    private RequestQueue mRequestQueue;

    /*88888888888888888888888888888888888888888888888888888888888*/

    private void showImageOrPdf(String url) {
        if (url.endsWith(".pdf")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            Glide.with(this)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.1f)
                    .into((ImageView) findViewById(R.id.preview_img));
            findViewById(R.id.image_preview_ll).setVisibility(View.VISIBLE);
        }
    }

    public void closePreview(View view) {
        findViewById(R.id.image_preview_ll).setVisibility(View.GONE);
    }
}
