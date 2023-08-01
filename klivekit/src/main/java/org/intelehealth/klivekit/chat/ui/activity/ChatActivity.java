package org.intelehealth.klivekit.chat.ui.activity;

import static org.intelehealth.klivekit.utils.RtcUtilsKt.RTC_ARGS;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.github.ajalt.timberkt.Timber;
import com.google.gson.Gson;

import org.intelehealth.klivekit.R;
import org.intelehealth.klivekit.chat.ui.adapter.ChatListingAdapter;
import org.intelehealth.klivekit.model.ChatMessage;
import org.intelehealth.klivekit.model.ChatResponse;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;
import org.intelehealth.klivekit.ui.activity.VideoCallActivity;
import org.intelehealth.klivekit.ui.viewmodel.SocketViewModel;
import org.intelehealth.klivekit.utils.AwsS3Utils;
import org.intelehealth.klivekit.utils.BitmapUtils;
import org.intelehealth.klivekit.utils.Constants;
import org.intelehealth.klivekit.utils.RealPathUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import kotlin.jvm.functions.Function1;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getName();
    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";
    //    private List<JSONObject> mChatList = new ArrayList<JSONObject>();
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    private ChatListingAdapter mChatListingAdapter;

    //    private Socket mSocket;
    private RequestQueue mRequestQueue;

    private String mFromUUId = "";
    private String mToUUId = "";
    private String mPatientUUid = "";
    private String mVisitUUID = "";
    private String mPatientName = "";
    protected LinearLayout mEmptyLinearLayout, mLoadingLinearLayout;
    protected EditText mMessageEditText;
    protected TextView mEmptyTextView;

    protected void setupActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mPatientName);
    }

    protected void initiateView() {
        mEmptyTextView = findViewById(R.id.empty_tv);
        mMessageEditText = findViewById(R.id.text_etv);
        mLoadingLinearLayout = findViewById(R.id.loading_layout);
        mEmptyLinearLayout = findViewById(R.id.empty_view);
        mRecyclerView = findViewById(R.id.chats_rcv);
        mLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, true);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    protected Intent getVideoIntent() {
        return new Intent(this, VideoCallActivity.class);
    }

    protected @LayoutRes int getContentResourceId() {
        return R.layout.activity_chat;
    }

    private final Function1<String, Emitter.Listener> emitter = s -> args -> emitEvent(s, args);

    private void emitEvent(String event, Object... args) {
        switch (event) {
            case SocketManager.EVENT_IS_READ:
                getAllMessages(false);
                break;
            case SocketManager.EVENT_UPDATE_MESSAGE:
                onUpdateMessageEvent(args);
                break;
            case SocketManager.EVENT_CALL:
                onCallEvent(args);
                break;
            default:
                Timber.tag(TAG).d("Event=>" + event);
                break;
        }
    }

    private void onUpdateMessageEvent(Object... args) {
        try {
            if (args.length == 0) return;
            Log.e(TAG, "onUpdateMessageEvent: " + new Gson().toJson(args[0]));
            for (Object arg : args) {
                Log.d(TAG, "updateMessage: " + String.valueOf(arg));
            }

            JSONObject jsonObject = new JSONArray(new Gson().toJson(args[0]))
                    .getJSONObject(0)
                    .getJSONObject("nameValuePairs");
            runOnUiThread(() -> {
                if (mToUUId.isEmpty()) {
                    try {
                        mToUUId = jsonObject.getString("fromUser");
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
                        getAllMessages(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    getAllMessages(false);
                }


            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onCallEvent(Object... args) {
        Log.d(TAG, "calling...: ");
        for (Object arg : args) {
            Log.d(TAG, "call: " + String.valueOf(arg));
        }
        try {
            if (args[0] instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) args[0];
                //{"nurseId":"28cea4ab-3188-434a-82f0-055133090a38","doctorName":"Mr Doctor","roomId":"f0f3d654-a7cd-4c7e-904c-f702c1825e0c"}
                Intent in = new Intent(this, VideoCallActivity.class);

                in.putExtra("roomId", jsonObject.getString("roomId"));
                in.putExtra("isInComingRequest", true);
                in.putExtra("doctorname", jsonObject.getString("doctorName"));
                in.putExtra("nurseId", jsonObject.getString("nurseId"));
                int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                if (callState == TelephonyManager.CALL_STATE_IDLE) {
                    startActivity(in);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentResourceId());
        mImagePathRoot = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        SocketManager.getInstance().setEmitterListener(emitter);
        if (getIntent().hasExtra("patientUuid")) {
            mPatientUUid = getIntent().getStringExtra("patientUuid");
        }
        if (getIntent().hasExtra("fromUuid")) {
            mFromUUId = getIntent().getStringExtra("fromUuid");
        }
        if (getIntent().hasExtra("toUuid")) {
            mToUUId = getIntent().getStringExtra("toUuid");
        }
        if (getIntent().hasExtra("visitUuid")) {
            mVisitUUID = getIntent().getStringExtra("visitUuid");
        }
        if (getIntent().hasExtra("patientName")) {
            mPatientName = getIntent().getStringExtra("patientName");
        }
        Log.v("mPatientUUid", String.valueOf(mPatientUUid));
        Log.v("mFromUUId", String.valueOf(mFromUUId));
        Log.v("mToUUId", String.valueOf(mToUUId));
        Log.v("mVisitUUID", String.valueOf(mVisitUUID));
        Log.v("mPatientName", String.valueOf(mPatientName));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setTitle(mPatientName);
//        ((TextView) findViewById(R.id.title_incoming_tv)).setText(mPatientName);
        //getSupportActionBar().setSubtitle(mVisitUUID);
        mRequestQueue = Volley.newRequestQueue(this);

        initiateView();
        initListAdapter();
        setupActionBar();
//        mEmptyTextView = findViewById(R.id.empty_tv);
//        mMessageEditText = findViewById(R.id.text_etv);
//        mLoadingLinearLayout = findViewById(R.id.loading_layout);
//        mEmptyLinearLayout = findViewById(R.id.empty_view);
//        mRecyclerView = findViewById(R.id.chats_rcv);
//        mLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, true);
//        mRecyclerView.setLayoutManager(mLayoutManager);


        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP app.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        // test nurseid / from userid - 28cea4ab-3188-434a-82f0-055133090a38
        //patientId  - a286e0de-eba0-4ad5-b698-900657d8ac75
        //Doctor id - a4ac4fee-538f-11e6-9cfe-86f436325720
        connectTOSocket();

        getAllMessages(false);
        //postMessages(FROM_UUID, TO_UUID, PATIENT_UUID, "hell.. mobile test - " + System.currentTimeMillis());
        mMessageEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        mMessageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Toast.makeText(ChatActivity.this, "Send", Toast.LENGTH_SHORT).show();
                sendMessageNow(null);
                return true;
            }
            return false;
        });

        if (getIntent().getBooleanExtra("isForVideo", false)) {

        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String fileUrl = intent.getStringExtra("fileUrl");
                postMessages(mFromUUId, mToUUId, mPatientUUid, fileUrl, "attachment");
            }
        };
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(AwsS3Utils.ACTION_FILE_UPLOAD_DONE);
        registerReceiver(mBroadcastReceiver, filterSend);
    }

    private BroadcastReceiver mBroadcastReceiver;


    public void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllMessages(boolean isAlreadySetReadStatus) {
        if (mFromUUId.isEmpty() || mToUUId.isEmpty() || mPatientUUid.isEmpty()) {
            return;
        }
        mEmptyTextView.setText(getString(R.string.loading));
        String url = Constants.GET_ALL_MESSAGE_URL + mFromUUId + "/" + mToUUId + "/" + mPatientUUid;
        Log.v(TAG, "getAllMessages - " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "getAllMessages -response - " + response.toString());
                mEmptyTextView.setText(getString(R.string.you_have_no_messages_start_sending_messages_now));
                ChatResponse chatResponse = new Gson().fromJson(response.toString(), ChatResponse.class);
                showChat(chatResponse, isAlreadySetReadStatus);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "getAllMessages - onErrorResponse - " + error.getMessage());
                mEmptyTextView.setText(getString(R.string.you_have_no_messages_start_sending_messages_now));
            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private int mPDFCount = 0;
    private int mImageCount = 0;

    private void showChat(ChatResponse response, boolean isAlreadySetReadStatus) {
        mPDFCount = 0;
        mImageCount = 0;
        if (response.isSuccess()) {
            ArrayList<ChatMessage> messages = response.getData();
            if (messages.isEmpty()) {
                mEmptyLinearLayout.setVisibility(View.VISIBLE);
            } else {
                mEmptyLinearLayout.setVisibility(View.GONE);
                updateListAdapter(response, isAlreadySetReadStatus);
            }
        }
    }

    private void initListAdapter() {
        mChatListingAdapter = new ChatListingAdapter(this, new ArrayList<>(), url -> showImageOrPdf(url));
        mRecyclerView.setAdapter(mChatListingAdapter);
    }

    private void updateListAdapter(ChatResponse response, boolean isAlreadySetReadStatus) {
        for (int i = 0; i < response.getData().size(); i++) {
            ChatMessage message = response.getData().get(i);
            if (message.getFromUser().equals(mFromUUId)) {
                message.setLayoutType(Constants.RIGHT_ITEM_HW);
                if (message.isAttachment()) {
                    if (message.getMessage().endsWith(".pdf")) {
                        mPDFCount += 1;
                    } else {
                        mImageCount += 1;
                    }
                }
            } else {
                message.setLayoutType(Constants.LEFT_ITEM_DOCT);
            }
        }

        sortList(response.getData());
        mChatListingAdapter.refresh(response.getData());

        if (!isAlreadySetReadStatus)
            for (int i = 0; i < response.getData().size(); i++) {
                //Log.v(TAG, "ID=" + mChatList.get(i).getString("id"));
                if (response.getData().get(i).getLayoutType() == Constants.LEFT_ITEM_DOCT
                        && response.getData().get(i).getIsRead() == 0) {
                    setReadStatus(response.getData().get(i).getId());
                    break;
                }
            }
    }

    private void sortList(List<ChatMessage> messages) {
        Collections.sort(messages, (Comparator<ChatMessage>) (o1, o2) -> {
            try {
                Date a = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(o1.getCreatedAt());
                Date b = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(o2.getCreatedAt());
                return b.compareTo(a);
            } catch (ParseException e) {
                return -1;
            }
        });
    }

    private void postMessages(String fromUUId, String toUUId, String patientUUId, String message, String type) {

//            JSONObject inputJsonObject = new JSONObject();
//            inputJsonObject.put("fromUser", fromUUId);
//            inputJsonObject.put("toUser", toUUId);
//            inputJsonObject.put("patientId", patientUUId);
//            inputJsonObject.put("message", message);
//            inputJsonObject.put("patientName", mPatientName);
//            inputJsonObject.put("hwName", "");
//            inputJsonObject.put("patientPic", "");
//            inputJsonObject.put("hwPic", "");
//            inputJsonObject.put("visitId", mVisitUUID);
//            inputJsonObject.put("isRead", false);
//            inputJsonObject.put("type", type);
        mLoadingLinearLayout.setVisibility(View.VISIBLE);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setFromUser(fromUUId);
        chatMessage.setIsRead(0);
        chatMessage.setPatientId(patientUUId);
        chatMessage.setToUser(toUUId);
        chatMessage.setVisitId(mVisitUUID);
        chatMessage.setPatientName(mPatientName);
        chatMessage.setType(type);
        Log.v(TAG, "postMessages - inputJsonObject - " + chatMessage.toJson());
        try {
            JsonObjectRequest objectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    Constants.SEND_MESSAGE_URL,
                    new JSONObject(chatMessage.toJson()), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "postMessages - response - " + response.toString());
                    mMessageEditText.setText("");
                    getAllMessages(false);
                    mLoadingLinearLayout.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v(TAG, "postMessages - onErrorResponse - " + error.getMessage());
                    mLoadingLinearLayout.setVisibility(View.GONE);
                }
            });
            mRequestQueue.add(objectRequest);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void setReadStatus(int messageId) {
        String url = Constants.SET_READ_STATUS_OF_MESSAGE_URL + messageId;
        Log.v(TAG, "setReadStatus - url - " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, null, response -> {
            Log.v(TAG, "setReadStatus - response - " + response.toString());
            getAllMessages(true);
            SocketManager.getInstance().emit(SocketManager.EVENT_IS_READ, null);
//                if (mSocket != null) mSocket.emit("isread");
        }, error -> Log.v(TAG, "setReadStatus - onErrorResponse - " + error.getMessage()));
        mRequestQueue.add(jsonObjectRequest);
    }

    private void connectTOSocket() {
        String url = Constants.BASE_URL + "?userId=" + mFromUUId + "&name=" + mFromUUId;
        if (!SocketManager.getInstance().isConnected()) {
            SocketManager.getInstance().connect(url);
        }
//        try {


//            Log.v(TAG, "connectTOSocket - " + url);
//            mSocket = IO.socket(url);
//            mSocket.on("connect", args -> {
//                for (Object arg : args) {
//                    Log.d(TAG, "connect: " + String.valueOf(arg));
//                }
//            });
//            mSocket.on("disconnect", args -> {
//                for (Object arg : args) {
//                    Log.d(TAG, "disconnect: " + String.valueOf(arg));
//                }
//            });
//            mSocket.on("isread", args -> {
//                Log.d(TAG, "isread event emit from web: ");
//                getAllMessages(false);
//            });
//            mSocket.on("call", args -> {
//                Log.d(TAG, "calling...: ");
//                for (Object arg : args) {
//                    Log.d(TAG, "call: " + String.valueOf(arg));
//                }
//                try {
//                    if (args[0] instanceof JSONObject) {
//                        JSONObject jsonObject = (JSONObject) args[0];
//                        //{"nurseId":"28cea4ab-3188-434a-82f0-055133090a38","doctorName":"Mr Doctor","roomId":"f0f3d654-a7cd-4c7e-904c-f702c1825e0c"}
//                        Intent in = new Intent(this, VideoCallActivity.class);
//
//                        in.putExtra("roomId", jsonObject.getString("roomId"));
//                        in.putExtra("isInComingRequest", true);
//                        in.putExtra("doctorname", jsonObject.getString("doctorName"));
//                        in.putExtra("nurseId", jsonObject.getString("nurseId"));
//                        int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
//                        if (callState == TelephonyManager.CALL_STATE_IDLE) {
//                            startActivity(in);
//                        }
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            });
//            mSocket.on("allUsers", args -> {
//                // try {
//                for (Object arg : args) {
//                    Log.d(TAG, "allUsers: " + String.valueOf(arg));
//                }
//                    /*if (mToUUId.isEmpty()) {
//                        JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                parseForToUUID(jsonObject);
//                            }
//                        });
//                    }*/
//               /* } catch (JSONException e) {
//                    e.printStackTrace();
//                }*/
//            });
//            // will trigger when got the new message
//            mSocket.on("updateMessage", args -> {
//                try {
//                    for (Object arg : args) {
//                        Log.d(TAG, "updateMessage: " + String.valueOf(arg));
//                    }
//
//                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mToUUId.isEmpty()) {
//                                try {
//                                    mToUUId = jsonObject.getString("fromUser");
//                                    // save in db
//                                    JSONObject connectionInfoObject = new JSONObject();
//                                    connectionInfoObject.put("fromUUID", mFromUUId);
//                                    connectionInfoObject.put("toUUID", mToUUId);
//                                    connectionInfoObject.put("patientUUID", mPatientUUid);
//
//                                    Intent intent = new Intent(ACTION_NAME);
//                                    intent.putExtra("visit_uuid", mVisitUUID);
//                                    intent.putExtra("connection_info", connectionInfoObject.toString());
//                                    intent.setComponent(new ComponentName("org.intelehealth.app", "org.intelehealth.app.utilities.RTCMessageReceiver"));
//
//                                    getApplicationContext().sendBroadcast(intent);
//                                    getAllMessages(false);
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//
//                            } else {
//                                getAllMessages(false);
//                                /*if (jsonObject.has("dataValues")) {
//                                    try {
//                                        addNewMessage(jsonObject.getJSONObject("dataValues"));
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                } else
//                                    addNewMessage(jsonObject);*/
//                            }
//
//
//                        }
//                    });
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            });
//            mSocket.connect();

//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

    private void parseForToUUID(JSONObject jsonObject) {
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (jsonObject.get(key) instanceof JSONObject) {
                    JSONObject innerJsonObject = (JSONObject) jsonObject.get(key);
                    String uuid = innerJsonObject.getString("uuid");
                    if (!mFromUUId.equals(uuid) && !mPatientUUid.equals(uuid)) {
                        mToUUId = uuid;
                        getAllMessages(false);
                        break;
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addNewMessage(ChatMessage message) {
        if (message.getFromUser().equals(mFromUUId)) {
            message.setLayoutType(Constants.RIGHT_ITEM_HW);
        } else {
            message.setLayoutType(Constants.LEFT_ITEM_DOCT);
        }
        if (message.getCreatedAt() == null) {
            SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            rawSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            message.setCreatedAt(rawSimpleDateFormat.format(new Date()));
        }

        mChatListingAdapter.addMessage(message);
        mEmptyLinearLayout.setVisibility(View.GONE);
        sortList(mChatListingAdapter.getList());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mSocket != null) {
//            mSocket.disconnect();
//        }
        unregisterReceiver(mBroadcastReceiver);
    }

    public void sendMessageNow(View view) {
        hideSoftKeyboard();
        if (mToUUId.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_wait_for_doctor), Toast.LENGTH_SHORT).show();
            return;
        }
        String message = mMessageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            postMessages(mFromUUId, mToUUId, mPatientUUid, message, "text");
        } else {
            Toast.makeText(this, getString(R.string.empty_message_txt), Toast.LENGTH_SHORT).show();
        }
    }

    public void endChat(View view) {
        finish();
    }

    public void vCallNow(View view) {
        startActivity(new Intent(this, VideoCallActivity.class)
                .putExtra("roomId", mPatientUUid)
                .putExtra("nurseId", mFromUUId)
        );

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
        Intent cameraIntent = new Intent(ChatActivity.this, VideoCallActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
        }
//        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
//        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
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
                            Toast.makeText(ChatActivity.this, getResources().getString(R.string.max_doc_size_toast), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ChatMessage message = new ChatMessage();
                        message.setFromUser(mFromUUId);
                        message.setToUser(mToUUId);
                        message.setPatientId(mPatientUUid);
                        message.setMessage(".jpg");
                        message.setType("attachment");
                        message.setLoading(true);
                        addNewMessage(message);
                        AwsS3Utils.saveFileToS3Cloud(ChatActivity.this, mVisitUUID, currentPhotoPath);
                    }
                }
            });

    private void galleryStart() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        final CharSequence[] options = {getString(R.string.take_photo_lbl), getString(R.string.choose_from_gallery_lbl), getResources().getString(R.string.choose_documents), getString(R.string.cancel_lbl)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(getResources().getString(R.string.select));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    if (mImageCount >= 5) {
                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.max_5_image), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cameraStart();

                } else if (item == 1) {
                    if (mImageCount >= 5) {
                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.max_5_image), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    galleryStart();

                } else if (item == 2) {
                    if (mPDFCount >= 2) {
                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.max_2_image), Toast.LENGTH_SHORT).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        menu.findItem(R.id.video_call_menu).getActionView().setOnClickListener(view -> {
            onOptionsItemSelected(menu.findItem(R.id.video_call_menu));
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.video_call_menu) {
//            Intent videoIntent = getVideoIntent().putExtra("roomId", mPatientUUid)
//                    .putExtra("nurseId", mFromUUId);
//            startActivity(videoIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                cameraStart();
                selectImage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
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


                            //physicalExamMap.setImagePath(mCurrentPhotoPath);
                            Log.i(TAG, currentPhotoPath);
                            if (!RealPathUtil.isFileLessThan512Kb(new File(currentPhotoPath))) {
                                Toast.makeText(ChatActivity.this, getResources().getString(R.string.max_doc_size_toast), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ChatMessage message = new ChatMessage();
                            message.setFromUser(mFromUUId);
                            message.setToUser(mToUUId);
                            message.setPatientId(mPatientUUid);
                            message.setMessage(".jpg");
                            message.setType("attachment");
                            message.setLoading(true);
                            addNewMessage(message);
                            AwsS3Utils.saveFileToS3Cloud(ChatActivity.this, mVisitUUID, currentPhotoPath);

                        } else {
                            Toast.makeText(ChatActivity.this, getResources().getString(R.string.unable_to_pick_data), Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(ChatActivity.this, getResources().getString(R.string.max_doc_size_toast_mb), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Log.v(TAG, "currentPDFPath" + currentPDFPath);
                                ChatMessage message = new ChatMessage();
                                message.setFromUser(mFromUUId);
                                message.setToUser(mToUUId);
                                message.setPatientId(mPatientUUid);
                                message.setMessage(".pdf");
                                message.setType("attachment");
                                message.setLoading(true);
                                addNewMessage(message);

                                AwsS3Utils.saveFileToS3Cloud(ChatActivity.this, mVisitUUID, currentPDFPath);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                }
            });
    public String mImagePathRoot = "";

    private void showImageOrPdf(String url) {
        if (url.endsWith(".pdf")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            Glide.with(this)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(0.1f)
                    .into((ImageView) findViewById(R.id.preview_img));
            findViewById(R.id.image_preview_ll).setVisibility(View.VISIBLE);
        }
    }

    public void closePreview(View view) {
        findViewById(R.id.image_preview_ll).setVisibility(View.GONE);
    }
}