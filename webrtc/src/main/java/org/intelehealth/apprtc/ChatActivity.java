package org.intelehealth.apprtc;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import org.intelehealth.apprtc.adapter.ChatListingAdapter;
import org.intelehealth.apprtc.data.Constants;
import org.intelehealth.apprtc.utils.BitmapUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getName();
    private static final String ACTION_NAME = "org.intelehealth.app.RTC_MESSAGING_EVENT";
    private List<JSONObject> mChatList = new ArrayList<JSONObject>();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ChatListingAdapter mChatListingAdapter;

    private Socket mSocket;
    private RequestQueue mRequestQueue;

    private String mFromUUId = "";
    private String mToUUId = "";
    private String mPatientUUid = "";
    private String mVisitUUID = "";
    private String mPatientName = "";
    private LinearLayout mEmptyLinearLayout, mLoadingLinearLayout;
    private EditText mMessageEditText;
    private TextView mEmptyTextView;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mImagePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        ;
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
        ((TextView) findViewById(R.id.title_incoming_tv)).setText(mPatientName);
        //getSupportActionBar().setSubtitle(mVisitUUID);
        mRequestQueue = Volley.newRequestQueue(this);

        mEmptyTextView = findViewById(R.id.empty_tv);
        mMessageEditText = findViewById(R.id.text_etv);
        mLoadingLinearLayout = findViewById(R.id.loading_layout);
        mEmptyLinearLayout = findViewById(R.id.empty_view);
        mRecyclerView = findViewById(R.id.chats_rcv);
        mLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, true);
        mRecyclerView.setLayoutManager(mLayoutManager);


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
        mMessageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendMessageNow(null);
                    return true;
                }
                return false;
            }
        });

        if (getIntent().getBooleanExtra("isForVideo", false)) {

        }
    }

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
        Log.v(TAG, url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "getAllMessages -response - " + response.toString());
                mEmptyTextView.setText(getString(R.string.you_have_no_messages_start_sending_messages_now));
                showChat(response, isAlreadySetReadStatus);
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

    private void showChat(JSONObject response, boolean isAlreadySetReadStatus) {
        try {
            mChatList.clear();
            if (response.getBoolean("success")) {
                JSONArray jsonArray = response.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject chatJsonObject = jsonArray.getJSONObject(i);
                    //Log.v(TAG, "showChat - " + chatJsonObject);
                    if (chatJsonObject.getString("fromUser").equals(mFromUUId)) {
                        chatJsonObject.put("type", Constants.RIGHT_ITEM_HW); // HW
                    } else {
                        chatJsonObject.put("type", Constants.LEFT_ITEM_DOCT); // DOCTOR
                    }
                    mChatList.add(chatJsonObject);
                }
                if (mChatList.isEmpty()) {
                    mEmptyLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mEmptyLinearLayout.setVisibility(View.GONE);
                }

                sortList();

                mChatListingAdapter = new ChatListingAdapter(this, mChatList);
                mRecyclerView.setAdapter(mChatListingAdapter);

                if (!isAlreadySetReadStatus)
                    for (int i = 0; i < mChatList.size(); i++) {
                        //Log.v(TAG, "ID=" + mChatList.get(i).getString("id"));
                        if (mChatList.get(i).getInt("type") == Constants.LEFT_ITEM_DOCT && mChatList.get(i).getInt("isRead") == 0) {
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

    private void postMessages(String fromUUId, String toUUId, String patientUUId, String message) {
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
            mLoadingLinearLayout.setVisibility(View.VISIBLE);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.SEND_MESSAGE_URL, inputJsonObject, new Response.Listener<JSONObject>() {
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
                if(mSocket!=null) mSocket.emit("isread");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "setReadStatus - onErrorResponse - " + error.getMessage());

            }
        });
        mRequestQueue.add(jsonObjectRequest);
    }

    private void connectTOSocket() {
        try {
            String url = Constants.BASE_URL + "?userId=" + mFromUUId + "&name=" + mFromUUId;
            Log.v(TAG, "connectTOSocket - " + url);
            mSocket = IO.socket(url);
            mSocket.on("connect", args -> {
                for (Object arg : args) {
                    Log.d(TAG, "connect: " + String.valueOf(arg));
                }
            });
            mSocket.on("disconnect", args -> {
                for (Object arg : args) {
                    Log.d(TAG, "disconnect: " + String.valueOf(arg));
                }
            });
            mSocket.on("isread", args -> {
                Log.d(TAG, "isread event emit from web: ");
                getAllMessages(false);
            });
            mSocket.on("call", args -> {
                Log.d(TAG, "calling...: ");
                for (Object arg : args) {
                    Log.d(TAG, "call: " + String.valueOf(arg));
                }
                try {
                    if (args[0] instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) args[0];
                        //{"nurseId":"28cea4ab-3188-434a-82f0-055133090a38","doctorName":"Mr Doctor","roomId":"f0f3d654-a7cd-4c7e-904c-f702c1825e0c"}
                        Intent in = new Intent(this, CompleteActivity.class);

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
            });
            mSocket.on("allUsers", args -> {
                // try {
                for (Object arg : args) {
                    Log.d(TAG, "allUsers: " + String.valueOf(arg));
                }
                    /*if (mToUUId.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseForToUUID(jsonObject);
                            }
                        });
                    }*/
               /* } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            });
            // will trigger when got the new message
            mSocket.on("updateMessage", args -> {
                try {
                    for (Object arg : args) {
                        Log.d(TAG, "updateMessage: " + String.valueOf(arg));
                    }

                    JSONObject jsonObject = new JSONObject(String.valueOf(args[0]));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                                /*if (jsonObject.has("dataValues")) {
                                    try {
                                        addNewMessage(jsonObject.getJSONObject("dataValues"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    addNewMessage(jsonObject);*/
                            }


                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
            mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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

    private void addNewMessage(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("fromUser").equals(mFromUUId)) {
                jsonObject.put("type", Constants.RIGHT_ITEM_HW);
            } else {
                jsonObject.put("type", Constants.LEFT_ITEM_DOCT);
            }
            if (!jsonObject.has("createdAt")) {
                SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                rawSimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                jsonObject.put("createdAt", rawSimpleDateFormat.format(new Date()));
            }
            mChatList.add(jsonObject);

            mEmptyLinearLayout.setVisibility(View.GONE);
            sortList();

            if (mChatListingAdapter == null) {
                mChatListingAdapter = new ChatListingAdapter(this, mChatList);
                mRecyclerView.setAdapter(mChatListingAdapter);
            } else {
                mChatListingAdapter.refresh(mChatList);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }

    public void sendMessageNow(View view) {
        hideSoftKeyboard();
        if (mToUUId.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_wait_for_doctor), Toast.LENGTH_SHORT).show();
            return;
        }
        String message = mMessageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            postMessages(mFromUUId, mToUUId, mPatientUUid, message);
        } else {
            Toast.makeText(this, getString(R.string.empty_message_txt), Toast.LENGTH_SHORT).show();
        }
    }

    public void endChat(View view) {
        finish();
    }

    public void vCallNow(View view) {
        startActivity(new Intent(this, CompleteActivity.class)
                .putExtra("roomId", mPatientUUid)
                .putExtra("nurseId", mFromUUId)
        );

    }

    public void loadAttachment(View view) {
        validatePermissionAndIntent();
    }

    private String mLastSelectedImageName = "";


    private void cameraStart() {
        /*File file = new File(AppConstants.IMAGE_PATH);
        final String imagePath = file.getAbsolutePath();
        final String imageName = UUID.randomUUID().toString();
        mLastSelectedImageName = imageName;
        Intent cameraIntent = new Intent(VisitCreationActivity.this, CameraActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
        }
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        //mContext.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
        mStartForCameraResult.launch(cameraIntent);*/
    }

    private void galleryStart() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mStartForGalleryResult.launch(intent);
    }

    private static final int MY_CAMERA_REQUEST_CODE = 1001;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;

    private void selectImage() {
        final CharSequence[] options = {getString(R.string.take_photo_lbl), getString(R.string.choose_from_gallery_lbl), getString(R.string.cancel_lbl)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Add Image by");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    cameraStart();

                } else if (item == 1) {
                    galleryStart();

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
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
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
                            currentPhotoPath = mImagePath + finalImageName + ".jpg";
                            BitmapUtils.copyFile(picturePath, currentPhotoPath);

                            // Handle the Intent


                            //physicalExamMap.setImagePath(mCurrentPhotoPath);
                            Log.i(TAG, currentPhotoPath);

                            try {
                                JSONObject inputJsonObject = new JSONObject();
                                inputJsonObject.put("fromUser", mFromUUId);
                                inputJsonObject.put("toUser", mToUUId);
                                inputJsonObject.put("patientId", mPatientUUid);
                                inputJsonObject.put("message", "New Image Sent!");
                                inputJsonObject.put("ContentType", "IMAGE");
                                inputJsonObject.put("filePath", currentPhotoPath);

                                addNewMessage(inputJsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(ChatActivity.this, "Unable to pick the gallery data!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
    public String mImagePath = "";

}