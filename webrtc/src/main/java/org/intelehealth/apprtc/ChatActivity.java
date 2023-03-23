package org.intelehealth.apprtc;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Cache;
import com.android.volley.Network;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mPatientName);
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

        getAllMessages();
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
    }

    public void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAllMessages() {
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
                showChat(response);
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

    private void showChat(JSONObject response) {
        try {
            mChatList.clear();
            if (response.getBoolean("success")) {
                JSONArray jsonArray = response.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject chatJsonObject = jsonArray.getJSONObject(i);
                    if (chatJsonObject.getString("fromUser").equals(mFromUUId)) {
                        chatJsonObject.put("type", Constants.RIGHT_ITEM);
                    } else {
                        chatJsonObject.put("type", Constants.LEFT_ITEM);
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
                    //Date a = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(o1.getString("createdAt"));
                    Date a = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").parse(o1.getString("createdAt"));
                    //Date b = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(o2.getString("createdAt"));
                    Date b = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").parse(o2.getString("createdAt"));
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
            mLoadingLinearLayout.setVisibility(View.VISIBLE);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Constants.SEND_MESSAGE_URL, inputJsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(TAG, "postMessages - response - " + response.toString());
                    mMessageEditText.setText("");
                    getAllMessages();
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

    private void connectTOSocket() {
        try {
            mSocket = IO.socket(Constants.BASE_URL + "?userId=" + mFromUUId + "&name=" + mFromUUId);
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
                        // not required bcz from firebase listener it working fine
                        /*if (callState == TelephonyManager.CALL_STATE_IDLE) {
                            startActivity(in);
                        }*/
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

                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    String packageName = pInfo.packageName;

                                    Intent intent = new Intent(ACTION_NAME);
                                    intent.putExtra("visit_uuid", mVisitUUID);
                                    intent.putExtra("connection_info", connectionInfoObject.toString());
                                    intent.setComponent(new ComponentName(packageName, "org.intelehealth.ekalarogya.services.firebase_services.RTCMessageReceiver"));

                                    getApplicationContext().sendBroadcast(intent);
                                    getAllMessages();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                if (jsonObject.has("dataValues")) {
                                    try {
                                        addNewMessage(jsonObject.getJSONObject("dataValues"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else
                                    addNewMessage(jsonObject);
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
                        getAllMessages();
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
                jsonObject.put("type", Constants.RIGHT_ITEM);
            } else {
                jsonObject.put("type", Constants.LEFT_ITEM);
            }
            if (!jsonObject.has("createdAt")) {
                //SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                SimpleDateFormat rawSimpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
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

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.video_call_menu) {
            startActivity(new Intent(this, CompleteActivity.class)
                    .putExtra("roomId", mPatientUUid)
                    .putExtra("nurseId", mFromUUId)
            );

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}