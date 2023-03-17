package org.intelehealth.apprtc;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static org.webrtc.SessionDescription.Type.OFFER;
import static io.socket.client.Socket.EVENT_CONNECT;
import static io.socket.client.Socket.EVENT_DISCONNECT;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.apprtc.data.Constants;
import org.intelehealth.apprtc.databinding.ActivitySamplePeerConnectionBinding;
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    private Socket socket;
    private boolean isInitiator;
    private boolean isChannelReady;
    private boolean isStarted;


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
    private String mNurseId = "Doctor";
    private boolean mIsInComingRequest = false;
    private Ringtone mRingtone;

    BroadcastReceiver broadcastReceiver;
    boolean mMicrophonePluggedIn = false;

    private BroadcastReceiver mCallEndBroadcastReceiver;
    public static final String CALL_END_FROM_WEB_INTENT_ACTION = "org.intelehealth.apprtc.CALL_END_FROM_WEB_INTENT_ACTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample_peer_connection);
        mRoomId = getIntent().getStringExtra("roomId");
        mIsInComingRequest = getIntent().getBooleanExtra("isInComingRequest", false);
        if (getIntent().hasExtra("doctorname"))
            mDoctorName = getIntent().getStringExtra("doctorname");
        if (getIntent().hasExtra("nurseId"))
            mNurseId = getIntent().getStringExtra("nurseId");

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


                    socket.emit("create or join", mRoomId);
                    startStreamingVideo();
                }
                stopRinging();
            }
        });
        binding.inCallRejectImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socket != null) {
                    socket.emit("create or join", mRoomId);
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
                    setAudioStatus(!localAudioTrack.enabled());
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
                    } else {
                        binding.videoImv.setImageResource(R.drawable.vc_new_v_camera_icon);
                        Toast.makeText(CompleteActivity.this, getString(R.string.video_off_lbl), Toast.LENGTH_SHORT).show();
                        binding.videoImv.setAlpha(0.2f);
                    }
                }
            }
        });
        binding.flipImv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean lastStatusOfAudioEnabled = localAudioTrack.enabled();
                mIsReverseCamera = !mIsReverseCamera;
                // recreate the video track
                createVideoTrackFromCameraAndShowIt();
                // start again the video streaming
                startStreamingVideo();
                setAudioStatus(lastStatusOfAudioEnabled);
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

        start();

        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(mPhoneStateBroadcastReceiver, filter);


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
    }

    private void setAudioStatus(boolean targetAudioStatus) {
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(targetAudioStatus);
            if (localAudioTrack.enabled()) {
                binding.audioImv.setImageResource(R.drawable.vc_new_call_mic_icon);
                Toast.makeText(CompleteActivity.this, getString(R.string.audio_on_lbl), Toast.LENGTH_SHORT).show();
                binding.audioImv.setAlpha(1.0f);
            } else {
                binding.audioImv.setImageResource(R.drawable.vc_new_call_mic_icon);
                Toast.makeText(CompleteActivity.this, getString(R.string.audio_off_lbl), Toast.LENGTH_SHORT).show();
                binding.audioImv.setAlpha(0.2f);
            }
        }
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
                binding.inCallRejectImv.performClick();
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

        }
    }

    @Override
    protected void onDestroy() {
        disconnectAll();
        try {
            unregisterReceiver(mPhoneStateBroadcastReceiver);
            unregisterReceiver(mCallEndBroadcastReceiver);
            unregisterReceiver(broadcastReceiver);
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
        } finally {
            finish();
        }


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
            Log.v(TAG, "connectToSignallingServer - " + url);
            socket = IO.socket(url);

            //socket emitter "call", you can listen on it after connection;
            // if you get any request on it; show incoming call screen;
            // if user accept it,
            // do same what you are doing on start call button(emitting - 'create or join' etc)
            // otherwise after 15 secs emit on "no answer"
            //

            socket.on(EVENT_CONNECT, args -> {
                Log.d(TAG, "connectToSignallingServer: connect");
                //socket.emit("create or join", "foo");
                if (!mIsInComingRequest) {

                    socket.emit("create or join", mRoomId);

                }
            }).on("ipaddr", args -> {
                Log.d(TAG, "connectToSignallingServer: ipaddr");
            }).on("bye", args -> {
                Log.d(TAG, "connectToSignallingServer: bye");
                //socket.emit("bye");
                disconnectAll();

            }).on("call", args -> {
                Log.d(TAG, "connectToSignallingServer: call");
                socket.emit("create or join", mRoomId);
            }).on("no_answer", args -> {
                Log.d(TAG, "connectToSignallingServer: no answer");
                // socket.emit("bye");
                disconnectAll();
            }).on("created", args -> {
                Log.d(TAG, "connectToSignallingServer: created");
                isInitiator = true;
            }).on("full", args -> {
                Log.d(TAG, "connectToSignallingServer: full");
            }).on("join", args -> {
                Log.d(TAG, "connectToSignallingServer: join");
                Log.d(TAG, "connectToSignallingServer: Another peer made a request to join room");
                Log.d(TAG, "connectToSignallingServer: This peer is the initiator of room");
                isChannelReady = true;
            }).on("joined", args -> {
                Log.d(TAG, "connectToSignallingServer: joined");
                isChannelReady = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.statusTv.setVisibility(View.GONE);
                    }
                });

            }).on("log", args -> {
                for (Object arg : args) {
                    Log.d(TAG, "connectToSignallingServer: log" + String.valueOf(arg));
                }
            }).on("message", args -> {
                Log.d(TAG, "connectToSignallingServer: got a message");
            }).on("message", args -> {
                try {
                    if (args[0] instanceof String) {
                        String message = (String) args[0];
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
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, message.getString("sdp")));
                        } else if (message.getString("type").equals("candidate") && isStarted) {
                            Log.d(TAG, "connectToSignallingServer: receiving candidates");
                            IceCandidate candidate = new IceCandidate(message.getString("id"), message.getInt("label"), message.getString("candidate"));
                            peerConnection.addIceCandidate(candidate);
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
            Log.d(TAG, "isInitiator: " + isInitiator);
//            if (isInitiator) {
            doCall();
//            }
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
                Log.d(TAG, "onCreateSuccess: ");
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
}
