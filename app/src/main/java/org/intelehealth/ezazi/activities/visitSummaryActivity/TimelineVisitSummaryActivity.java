package org.intelehealth.ezazi.activities.visitSummaryActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.BuildConfig;
import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.epartogramActivity.EpartogramViewActivity;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.RTCConnectionDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.databinding.DialogReferHospitalEzaziBinding;
import org.intelehealth.ezazi.databinding.DialogStage2AdditionalDataEzaziBinding;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.models.dto.RTCConnectionDTO;
import org.intelehealth.ezazi.models.pushRequestApiCall.Attribute;
import org.intelehealth.ezazi.services.firebase_services.FirebaseRealTimeDBUtils;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.ui.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziChatActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziVideoCallActivity;
import org.intelehealth.ezazi.ui.rtc.call.CallInitializer;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.NetworkConnection;
import org.intelehealth.ezazi.utilities.NotificationReceiver;
import org.intelehealth.ezazi.utilities.NotificationUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;
import org.intelehealth.klivekit.utils.RemoteActionType;
import org.intelehealth.klivekit.utils.RtcUtilsKt;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TimelineVisitSummaryActivity extends BaseActionBarActivity {
    RecyclerView recyclerView;
    TimelineAdapter adapter;
    Context context;
    private String patientName;
    Intent intent;
    ArrayList<String> timeList;
    String startVisitTime, patientUuid, visitUuid, whichScreenUserCameFromTag, providerID, Stage1_Hour1_1;
    SessionManager sessionManager;
    EncounterDAO encounterDAO = new EncounterDAO();
    ArrayList<EncounterDTO> encounterListDTO;
    Button endStageButton;
    int stageNo = 0;
    String value = "";
    String isVCEPresent = "";
    Button fabc, fabv, fabSOS;
    private SQLiteDatabase db;
    TextView outcomeTV;
    //    String valueStage = "";
//    int positionStage = -1;
    public static final String TAG = "TimelineVisitSummary";
    boolean isAdded = false;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            fetchAllEncountersFromVisitForTimelineScreen(visitUuid);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(AppConstants.NEW_CARD_INTENT_ACTION));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_timeline_ezazi);
//        observeKeyboardEvent();
        super.onCreate(savedInstanceState);
        initUI();
//        adapter = new TimelineAdapter(context, intent, encounterDTO, sessionManager);
//        recyclerView.setAdapter(adapter);
        //  triggerAlarm5MinsBefore(); // Notification to show 5min before for every 30min interval.

        fabSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(1000);
                }

                showEmergencyDialog();

//                MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
//                alertDialog.setTitle("Emergency!");
//                alertDialog.setMessage("Are you sure to capture the emergency data now?");
//                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid);
//                        String latestEncounterName = encounterDAO.getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid());
//                        Log.v(TAG, "latestEncounterName - " + latestEncounterName);
//                        if (!latestEncounterName.toLowerCase().contains("stage") && !latestEncounterName.toLowerCase().contains("hour"))
//                            return;
//                        String[] parts = latestEncounterName.toLowerCase().replaceAll("stage", "").replaceAll("hour", "").split("_");
//                        if (parts.length != 3) return;
//                        int stageNumber = Integer.parseInt(parts[0]);
//                        int hourNumber = Integer.parseInt(parts[1]) + 1;
//                        int cardNumber = 1;//Integer.parseInt(parts[2]);
//
//
//                        String nextEncounterTypeName = "Stage" + stageNumber + "_" + "Hour" + hourNumber + "_" + cardNumber;
//                        Log.v(TAG, "nextEncounterTypeName - " + nextEncounterTypeName);
//
//                        createNewEncounter(visitUuid, nextEncounterTypeName);
//                        fetchAllEncountersFromVisitForTimelineScreen(visitUuid);
//
//                    }
//                });
//                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                alertDialog.show();
            }
        });
        fabc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                // EncounterDAO encounterDAO = new EncounterDAO();
                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid);
                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitUuid);
                Intent chatIntent = new Intent(TimelineVisitSummaryActivity.this, EzaziChatActivity.class);
                chatIntent.putExtra("patientName", patientName);
                chatIntent.putExtra("visitUuid", visitUuid);
                chatIntent.putExtra("patientUuid", patientUuid);
                chatIntent.putExtra("fromUuid", /*sessionManager.getProviderID()*/ encounterDTO.getProvideruuid()); // provider uuid
                chatIntent.putExtra("isForVideo", false);
                if (rtcConnectionDTO != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(rtcConnectionDTO.getConnectionInfo());
                        chatIntent.putExtra("toUuid", jsonObject.getString("toUUID")); // assigned doctor uuid
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    chatIntent.putExtra("toUuid", ""); // assigned doctor uuid
                }
                startActivity(chatIntent);
            }
        });
        fabv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: ");
                showDoctorSelectionDialog();
//                try {
//
//
//                    // show the patient primary & secondary doctor
//                    PatientsDAO patientsDAO = new PatientsDAO();
//                    List<Attribute> patientAttributes = patientsDAO.getPatientAttributes(patientUuid);
//                    String[] doctNames = new String[2];
//                    String[] doctUUIDs = new String[2];
//                    for (int i = 0; i < patientAttributes.size(); i++) {
//                        String name = patientsDAO.getAttributesName(patientAttributes.get(i).getAttributeType());
//                        if (name.equalsIgnoreCase("PrimaryDoctor")) {
//                            doctUUIDs[0] = patientAttributes.get(i).getValue().split("@#@")[0];
//                            doctNames[0] = patientAttributes.get(i).getValue().split("@#@")[1];
//                        }
//                        if (name.equalsIgnoreCase("SecondaryDoctor")) {
//                            doctUUIDs[1] = patientAttributes.get(i).getValue().split("@#@")[0];
//                            doctNames[1] = patientAttributes.get(i).getValue().split("@#@")[1];
//                        }
//                    }
//
//                    AlertDialog.Builder builder =
//                            new AlertDialog.Builder(TimelineVisitSummaryActivity.this);
//
//                    builder.setTitle("Select Doctor")
//                            .setItems(doctNames, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    //  EncounterDAO encounterDAO = new EncounterDAO();
//                                    EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid);
//                                    RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
//                                    RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitUuid);
//                                    Intent in = new Intent(TimelineVisitSummaryActivity.this, VideoCallActivity.class);
//                                    String roomId = patientUuid;
//                                    String doctorName = doctNames[which];
//                                    String nurseId = encounterDTO.getProvideruuid();
//                                    in.putExtra("roomId", roomId);
//                                    in.putExtra("isInComingRequest", false);
//                                    in.putExtra("doctorname", doctorName);
//                                    in.putExtra("nurseId", nurseId);
//                                    in.putExtra("startNewCall", true);
//                                    in.putExtra("doctorUUID", doctUUIDs[which]);
//
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                                        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    }
//                                    int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
//                                    if (callState == TelephonyManager.CALL_STATE_IDLE) {
//                                        startActivity(in);
//                                    }
//                                }
//                            });
//                    builder.create().show();
//
//
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
            }
        });

    }

    @Override
    protected int getScreenTitle() {
        return 0;
    }

    /**
     * Find the primary and secondary doctor's details of name and uuid
     * from patient attributes list as a map, key present as a doctor name
     * and value is doctor uuid
     *
     * @return doctors HashMap<String, String>
     */
    private HashMap<String, String> getDoctorsDetails() {
        PatientsDAO patientsDAO = new PatientsDAO();
        HashMap<String, String> doctors = new HashMap<>();
        try {
            List<Attribute> patientAttributes = patientsDAO.getPatientAttributes(patientUuid);
            for (int i = 0; i < patientAttributes.size(); i++) {
                String name = patientsDAO.getAttributesName(patientAttributes.get(i).getAttributeType());
                if (name.equalsIgnoreCase("PrimaryDoctor")) {
                    String doctorUuid = patientAttributes.get(i).getValue().split("@#@")[0];
                    String doctorName = patientAttributes.get(i).getValue().split("@#@")[1];
                    doctors.put(doctorName, doctorUuid);
                }
                if (name.equalsIgnoreCase("SecondaryDoctor")) {
                    String doctorUuid = patientAttributes.get(i).getValue().split("@#@")[0];
                    String doctorName = patientAttributes.get(i).getValue().split("@#@")[1];
                    doctors.put(doctorName, doctorUuid);
                }
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

        return doctors;
    }

    /**
     * Show the single choice doctor selection dialog and move forward
     * to video call with selected doctor from list
     */
    private void showDoctorSelectionDialog() {
        HashMap<String, String> doctors = getDoctorsDetails();
        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this)
                .title(R.string.select_doctor)
                .content(new ArrayList<>(doctors.keySet()))
                .build();

        dialog.setListener((position, value) -> startVideoCallActivity(doctors, value));

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    /**
     * Start video call with selected doctor from primary and secondary list.
     *
     * @param doctors    HashMap
     * @param doctorName String
     */
    private void startVideoCallActivity(HashMap<String, String> doctors, String doctorName) {
        Toast.makeText(this, doctorName, Toast.LENGTH_LONG).show();
        Log.v(TAG, "doctors  - " + doctorName);
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid);
        RtcArgs args = new RtcArgs();
        try {
            String patientOpenMrsId = new PatientsDAO().getOpenmrsId(patientUuid);
            args.setPatientOpenMrsId(patientOpenMrsId);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }
//        RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
//        RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitUuid);
//        Intent in = new Intent(TimelineVisitSummaryActivity.this, EzaziVideoCallActivity.class);


        String nurseId = encounterDTO.getProvideruuid();
        String roomId = patientUuid;

        args.setVisitId(visitUuid);
        args.setPatientId(patientUuid);
        args.setPatientPersonUuid(patientUuid);
        args.setPatientName(patientName);
        args.setDoctorName(doctorName);
        args.setDoctorUuid(doctors.get(doctorName));
        args.setIncomingCall(false);
        args.setNurseId(nurseId);
        args.setNurseName(sessionManager.getChwname());
        args.setRoomId(roomId);
        new CallInitializer(args).initiateVideoCall(args1 -> EzaziVideoCallActivity.startVideoCallActivity(TimelineVisitSummaryActivity.this, args1));

//        in.putExtra("roomId", roomId);
//        in.putExtra("isInComingRequest", false);
//        in.putExtra("doctorname", doctorName);
//        in.putExtra("nurseId", nurseId);
//        in.putExtra("startNewCall", true);
//        in.putExtra("doctorUUID", doctors.get(doctorName));
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
//        if (callState == TelephonyManager.CALL_STATE_IDLE) {
//            startActivity(in);
//        }
    }

    private void showEmergencyDialog() {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .title(R.string.emergency)
                .positiveButtonLabel(R.string.yes)
                .content(getString(R.string.are_you_sure_to_capture_emergency_data))
                .build();

        dialog.setListener(this::collectEmergencyData);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void collectEmergencyData() {
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid);
        String latestEncounterName = encounterDAO.getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid());
        if (latestEncounterName != null && latestEncounterName.length() > 0) {
            Log.v(TAG, "latestEncounterName - " + latestEncounterName);
            if (!latestEncounterName.toLowerCase().contains("stage") && !latestEncounterName.toLowerCase().contains("hour"))
                return;
            String[] parts = latestEncounterName.toLowerCase().replaceAll("stage", "").replaceAll("hour", "").split("_");
            if (parts.length != 3) return;
            int stageNumber = Integer.parseInt(parts[0]);
            int hourNumber = Integer.parseInt(parts[1]) + 1;
            int cardNumber = 1;//Integer.parseInt(parts[2]);


            String nextEncounterTypeName = "Stage" + stageNumber + "_" + "Hour" + hourNumber + "_" + cardNumber;
            Log.v(TAG, "nextEncounterTypeName - " + nextEncounterTypeName);
            String encounterUuid = UUID.randomUUID().toString();
            new ObsDAO().createEncounterType(encounterUuid, EncounterDTO.Type.SOS.name(), sessionManager.getCreatorID());
            Log.e(TAG, "SOS Encounter uuid " + encounterUuid);
            createNewEncounter(encounterUuid, visitUuid, nextEncounterTypeName);
            fetchAllEncountersFromVisitForTimelineScreen(visitUuid);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timeline_menu, menu);
        Button btn = menu.findItem(R.id.action_view_partogram).getActionView().findViewById(R.id.btnViewPartogram);
        btn.setOnClickListener(view -> {
            onOptionsItemSelected(menu.findItem(R.id.action_view_partogram));
        });
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_partogram:
                boolean isTablet = getResources().getBoolean(R.bool.isTablet);
                if (isTablet)
                    showEpartogram();
                else showRequireTabletView();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }

    private void showRequireTabletView() {
        new ConfirmationDialogFragment.Builder(this)
                .content(getString(R.string.this_option_available_tablet_device))
                .positiveButtonLabel(R.string.ok)
                .hideNegativeButton(true)
                .build().show(getSupportFragmentManager(), "ConfirmationDialogFragment");
    }

    private void showEpartogram() {
        Map<String, String> log = new HashMap<>();
        log.put("TAG", TAG);
        log.put("action", "showEpartogram");
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        Logger.logV("PHONE_TYPE_NONE", String.valueOf(Objects.requireNonNull(manager).getPhoneType()));
        /*if (getResources().getBoolean(R.bool.isTablet)) {
            Intent intent = new Intent(context, Epartogram.class);
            intent.putExtra("patientuuid", patientUuid);
            intent.putExtra("visituuid", visitUuid);
            startActivity(intent);
            log.put("message", "Detected... You're using a Tablet");

        } else {
            log.put("message", "Detected... You're using a Mobile Phone");
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showOkDialog(TimelineVisitSummaryActivity.this, "",
                    context.getString(R.string.this_option_available_tablet_device) , context.getString(R.string.ok));
        }*/
        Intent intent = new Intent(context, EpartogramViewActivity.class);
        intent.putExtra("patientuuid", patientUuid);
        intent.putExtra("visituuid", visitUuid);
        startActivity(intent);
        FirebaseRealTimeDBUtils.logData(log);
       /* DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;

        float scaleFactor = metrics.density;

        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);
        Log.v("epartog", "smallest width: " + smallestWidth);

      *//*  float widthInches = widthPixels / widthDp;
        float heightInches = heightPixels / heightDp;

        double diagonalInches = Math.sqrt((widthInches * widthInches) + (heightInches * heightInches));
        Log.v("epartog", "Device Size: " + diagonalInches);
*/

        //  if (smallestWidth >= 720) { // 8inch = 720 and 7inch == 600
        //Device is a 8" tablet
        // Call webview here...
          /*  Intent intent = new Intent(context, Epartogram.class);
            intent.putExtra("patientuuid", patientUuid);
            intent.putExtra("visituuid", visitUuid);
            startActivity(intent);*/
        /*} else {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showOkDialog(TimelineVisitSummaryActivity.this, "",
                    context.getString(R.string.this_option_available_tablet_device) *//*+ ": " + dpi*//*, context.getString(R.string.ok));
        }*/

    }

    private void initUI() {
        fabSOS = findViewById(R.id.btnSOS);
        fabv = findViewById(R.id.btnVideoOnOff);
        outcomeTV = findViewById(R.id.outcomeTV);
        fabc = findViewById(R.id.btnFlipCamera);
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        timeList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerview_timeline);
        endStageButton = findViewById(R.id.btnEndStage);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(linearLayout);
        context = TimelineVisitSummaryActivity.this;
        intent = this.getIntent(); // The intent was passed to the activity

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        if (intent != null) {
            startVisitTime = intent.getStringExtra("startdate");
            timeList.add(startVisitTime);
            patientName = intent.getStringExtra("patientNameTimeline");
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            providerID = intent.getStringExtra("providerID");
            whichScreenUserCameFromTag = intent.getStringExtra("tag");
            Stage1_Hour1_1 = intent.getStringExtra("Stage1_Hour1_1");

            Log.v("timeline", "patientname_1 " + patientName + " " + patientUuid + " " + visitUuid);

            if (whichScreenUserCameFromTag != null &&
                    whichScreenUserCameFromTag.equalsIgnoreCase("new")) {
                triggerAlarm_Stage1_every30mins(); // Notification to show every 30min.
                Log.v("timeline", "whichscreen: " + whichScreenUserCameFromTag);
            } else {
                // do nothing
            }

            fetchAllEncountersFromVisitForTimelineScreen(visitUuid); // fetch all records...
        }

        setTitle(patientName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitUuid); // get latest encounter.
        // String latestEncounterTypeId = encounterDTO.getEncounterTypeUuid();
//        String latestEncounterName = encounterDAO.getEncounterTypeNameByUUID(encounterDTO.getEncounterTypeUuid());
        String latestEncounterName = encounterDAO.findCurrentStage(encounterDTO.getVisituuid());
        // TODO: check for visit complete and if yes than disable the button.
        if (isVCEPresent.equalsIgnoreCase("")) { // "" ie. not present
            endStageButton.setEnabled(true);
            endStageButton.setClickable(true);
            endStageButton.setBackground(context.getResources().getDrawable(R.drawable.ic_rectangle_76));
            if (latestEncounterName.toLowerCase().contains("stage2")) {
                stageNo = 2;
                endStageButton.setText(context.getResources().getText(R.string.end2StageButton));
            } else if (latestEncounterName.toLowerCase().contains("stage1")) {
                stageNo = 1;
                endStageButton.setText(context.getResources().getText(R.string.endStageButton));
            } else {
                stageNo = 0;
                // do not hing
            }
        } else {
            String outcome = new ObsDAO().getCompletedVisitType(isVCEPresent);
            endStageButton.setVisibility(View.INVISIBLE);
            if (!outcome.equalsIgnoreCase("")) {
                outcomeTV.setVisibility(View.VISIBLE);
                outcomeTV.setText("Outcome: " + outcome);
            }
            fabc.setVisibility(View.GONE);
            fabv.setVisibility(View.GONE);
            fabSOS.setVisibility(View.GONE);
        }

        // clicking on this open dialog to confirm and start stage 2 | If stage 2 already open then ends visit.
        endStageButton.setOnClickListener(v -> {
            Log.e(TAG, "endStageButton stage = " + stageNo);
            if (stageNo == 1) {
                showEndShiftDialog();
                // cancelStage1_ConfirmationDialog();// cancel and start stage 2
            } else if (stageNo == 2) {
                // show dialog and add birth outcome also show extra options like: Refer to other hospital & Self Discharge
                birthOutcomeSelectionDialog();
            }
        });
        mCountDownTimer.start();
    }

    private void showEndShiftDialog() {
        Log.e(TAG, "showEndShiftDialog");
        final String stage1Options[] = {getString(R.string.move_to_stage2),
                getString(R.string.refer_to_other_hospital), getString(R.string.self_discharge_medical_advice)};

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this)
                .title(R.string.select_an_option)
                .positiveButtonLabel(R.string.yes)
                .content(Arrays.asList(stage1Options))
                .build();

        dialog.setListener(this::manageStageSelection);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());

//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
//        dialogBuilder.setTitle(R.string.select_an_option);
//
//        dialogBuilder.setSingleChoiceItems(stage1Options, -1, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int position) {
//                valueStage = String.valueOf(stage1Options[position]);
//                positionStage = position;
//            }
//        });

//        dialogBuilder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                boolean isInserted = false;
//                if (positionStage == 0)
//                    cancelStage1_ConfirmationDialog(); // cancel and start stage 2
//                else if (positionStage == 1) // refer other hospital // call visit complete enc.
//                    referOtherHospitalDialog(valueStage);
//                else if (positionStage == 2) { // self discharge // call visit complete enc.
//                    try {
//                        isInserted = insertVisitComplete_Obs(visitUuid, valueStage, UuidDictionary.REFER_TYPE);
//                    } catch (DAOException e) {
//                        e.printStackTrace();
//                    }
//                    if (isInserted) {
//                        Toast.makeText(context, context.getString(R.string.self_discharge_successful), Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(context, HomeActivity.class);
//                        startActivity(intent);
//                        checkInternetAndUploadVisit_Encounter();
//                    } else {
//                        Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                    }
//                    dialog.dismiss();
//                } else
//                    Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
//
//            }
//        });

//        dialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                dialog.dismiss();
//            }
//        });

//        AlertDialog dialog = dialogBuilder.show();
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//
//        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    private void manageStageSelection(int position, String value) {
        if (position == 0)
            cancelStage1ConfirmationDialog(); // cancel and start stage 2
        else if (position == 1) // refer other hospital // call visit complete enc.
            showReferToOtherHospitalConfirmationDialog(value);
        else if (position == 2) { // self discharge // call visit complete enc.
            showSelfDischargeConfirmationDialog(value);
        } else
            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
    }

    private void showReferToOtherHospitalConfirmationDialog(String value) {
        showConfirmationDialog(R.string.are_you_sure_want_to_refer_other, () -> {
            referOtherHospitalDialog(value);
        });
    }

    private void showSelfDischargeConfirmationDialog(String value) {
        showConfirmationDialog(R.string.are_you_sure_want_to_self_discharge, () -> {
            selfDischarge(value);
        });
    }

    private void selfDischarge(String value) {
        boolean isInserted = false;
        try {
            isInserted = insertVisitComplete_Obs(visitUuid, value, UuidDictionary.REFER_TYPE);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if (isInserted) {
            Toast.makeText(context, context.getString(R.string.self_discharge_successful), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
            checkInternetAndUploadVisit_Encounter();
        } else {
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog(@StringRes int content, ConfirmationDialogFragment.OnConfirmationActionListener listener) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .content(getString(content))
                .positiveButtonLabel(R.string.yes)
                .build();

        dialog.setListener(listener);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void showCustomViewDialog(@StringRes int title, View view, CustomViewDialogFragment.OnConfirmationActionListener listener) {
        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(this)
                .title(title)
                .positiveButtonLabel(R.string.yes)
                .view(view)
                .build();

        dialog.setListener(listener);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void referOtherHospitalDialog(String referType) {
//        String referOptions[] = {getString(R.string.refer_hospital_name), getString(R.string.refer_doctor_name), getString(R.string.refer_note)};

        DialogReferHospitalEzaziBinding binding = DialogReferHospitalEzaziBinding.inflate(getLayoutInflater(), null, false);

        showCustomViewDialog(R.string.refer_section, binding.getRoot(), () -> {
            boolean isInserted = false;
            String hospitalName = binding.referHospitalName.getText().toString(),
                    doctorName = binding.referDoctorName.getText().toString(),
                    note = binding.referNote.getText().toString();

            // call visitcompleteenc and add obs for refer type and referal values entered...
            try {
                isInserted = insertVisitCompleteEncounterAndObs_ReferHospital(visitUuid, referType, hospitalName, doctorName, note);
            } catch (DAOException e) {
                e.printStackTrace();
            }

            Log.v(TAG, "referValue: " + "visit uuid: " + visitUuid + ", " + referType + ", "
                    + hospitalName + ", " + doctorName + ", " + note);

            if (isInserted) {
                Toast.makeText(context, context.getString(R.string.refer_data_submitted_successfully), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                checkInternetAndUploadVisit_Encounter();
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
//
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_refer_hospital, null);
//        dialogBuilder.setView(view);
//        dialogBuilder.setTitle(R.string.refer_section);
//        EditText refer_hospitalName = view.findViewById(R.id.refer_hospitalName);
//        EditText refer_doctorName = view.findViewById(R.id.refer_doctorName);
//        EditText referNote = view.findViewById(R.id.referNote);
//
//        dialogBuilder.setPositiveButton(context.getString(R.string.submit), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                boolean isInserted = false;
//                String hospitalName = refer_hospitalName.getText().toString(),
//                        doctorName = refer_doctorName.getText().toString(),
//                        note = referNote.getText().toString();
//
//                // call visitcompleteenc and add obs for refer type and referal values entered...
//                try {
//                    isInserted = insertVisitCompleteEncounterAndObs_ReferHospital(visitUuid, referType, hospitalName, doctorName, note);
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
//
//                Log.v(TAG, "referValue: " + "visit uuid: " + visitUuid + ", " + referType + ", "
//                        + hospitalName + ", " + doctorName + ", " + note);
//
//                if (isInserted) {
//                    Toast.makeText(context, context.getString(R.string.refer_data_submitted_successfully), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(context, HomeActivity.class);
//                    startActivity(intent);
//                    checkInternetAndUploadVisit_Encounter();
//                } else {
//                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                }
//                dialog.dismiss();
//            }
//        });
//
//        dialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                dialog.dismiss();
//            }
//        });
//
//        AlertDialog dialog = dialogBuilder.show();
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//
//        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }


    private CountDownTimer mCountDownTimer = new CountDownTimer(24 * 60 * 60 * 1000, 60 * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            fetchAllEncountersFromVisitForTimelineScreen(visitUuid);
        }

        @Override
        public void onFinish() {

        }
    };

    private static void createNewEncounter(String encounterUuid, String visit_UUID, String nextEncounterTypeName) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        String typeUuid = encounterDAO.getEncounterTypeUuid(nextEncounterTypeName);
        Log.e(TAG, "TypeUuid=>" + typeUuid);
        if (typeUuid != null && typeUuid.length() > 0) {
            encounterDTO.setUuid(encounterUuid);
            encounterDTO.setVisituuid(visit_UUID);
            encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            encounterDTO.setProvideruuid(new SessionManager(IntelehealthApplication.getAppContext()).getProviderID());
            encounterDTO.setEncounterTypeUuid(typeUuid);
            encounterDTO.setSyncd(false); // false as this is the one that is started and would be pushed in the payload...
            encounterDTO.setVoided(0);
            encounterDTO.setPrivacynotice_value("true");

            try {
                encounterDAO.createEncountersToDB(encounterDTO);
            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
    }

//    private String fetchOutcome(String encounterID) {
//        String outcome = "";
//        String query = "SELECT value FROM tbl_obs WHERE encounteruuid = ? AND conceptuuid IN (?, ?)";
//        final Cursor searchCursor = db.rawQuery(query, new String[]{encounterID, UuidDictionary.BIRTH_OUTCOME, UuidDictionary.REFER_TYPE});
//        if (searchCursor.moveToFirst()) {
//            do {
//                try {
//                    outcome = searchCursor.getString(searchCursor.getColumnIndexOrThrow("value"));
//                    if (outcome.equals(getString(R.string.refer_to_other_hospital))) {
//                        outcome = "ROH";
//                    } else if (outcome.equals(getString(R.string.self_discharge_medical_advice))) {
//                        outcome = "DAMA";
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            while (searchCursor.moveToNext());
//        }
//        searchCursor.close();
//        return outcome;
//    }


    private void birthOutcomeSelectionDialog() {

//        positionStage = -1;
        final String[] items = {getString(R.string.live_birth), getString(R.string.still_birth),
                getString(R.string.refer_to_other_hospital), getString(R.string.self_discharge_medical_advice)};
        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this)
                .title(R.string.select_birth_outcome)
                .positiveButtonLabel(R.string.yes)
                .content(Arrays.asList(items))
                .build();

        dialog.setListener(this::manageBirthOutcomeSelection);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());

//        value = "";
//        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
//        alertDialog.setTitle(R.string.select_birth_outcome);
//        alertDialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int position) {
//                value = String.valueOf(items[position]);
//                positionStage = position;
//            }
//        });

//        alertDialog.setPositiveButton(context.getResources().getString(R.string.yes),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        boolean isInserted = false;
//                        // Birth Outcome
//                        if (positionStage == 0 || positionStage == 1) {
//                            Log.v("birthoutcome", "value: " + value);
//                            stage2_captureAdditionalData(value);
//                            Log.v("isInserted", "isInserted_livebirth: " + isInserted);
///*                            try {
//                                isInserted = insertVisitComplete_Obs(visitUuid, value, UuidDictionary.BIRTH_OUTCOME);
//                            } catch (DAOException e) {
//                                e.printStackTrace();
//                                Log.e("birthoutcome", "insert visit complete: " + e);
//                            }*/
//                        } else if (positionStage == 2) // refer other hospital // call visit complete enc.
//                            referOtherHospitalDialog(valueStage);
//                        else if (positionStage == 3) { // self discharge // call visit complete enc.
//                            try {
//                                isInserted = insertVisitComplete_Obs(visitUuid,
//                                        context.getString(R.string.self_discharge_medical_advice), UuidDictionary.REFER_TYPE);
//                            } catch (DAOException e) {
//                                e.printStackTrace();
//                            }
//                        } else
//                            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
//
//
//                        if (isInserted) {
//                            cancelStage2_Alarm(); // cancel stage 2 alarm so that again 15mins interval doesnt starts.
//                            Intent intent = new Intent(context, HomeActivity.class);
//                            startActivity(intent);
//                            checkInternetAndUploadVisit_Encounter();
//                        }
//                        dialog.dismiss();
//                    }
//                });

//        alertDialog.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                dialog.dismiss();
//            }
//        });
//
//        AlertDialog dialog = alertDialog.show();
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//
//        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private void manageBirthOutcomeSelection(int position, String value) {
        boolean isInserted = false;
        // Birth Outcome
        if (position == 0 || position == 1) {
            Log.v("birthoutcome", "value: " + value);
            stage2captureAdditionalData(value);
            Log.v("isInserted", "isInserted_livebirth: " + isInserted);
/*                            try {
                                isInserted = insertVisitComplete_Obs(visitUuid, value, UuidDictionary.BIRTH_OUTCOME);
                            } catch (DAOException e) {
                                e.printStackTrace();
                                Log.e("birthoutcome", "insert visit complete: " + e);
                            }*/
        } else if (position == 2) // refer other hospital // call visit complete enc.
            referOtherHospitalDialog(value);
        else if (position == 3) { // self discharge // call visit complete enc.
            try {
                isInserted = insertVisitComplete_Obs(visitUuid,
                        context.getString(R.string.self_discharge_medical_advice), UuidDictionary.REFER_TYPE);
            } catch (DAOException e) {
                e.printStackTrace();
            }
        } else
            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();


        if (isInserted) {
            cancelStage2_Alarm(); // cancel stage 2 alarm so that again 15mins interval doesnt starts.
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
            checkInternetAndUploadVisit_Encounter();
        }
    }

    private boolean insertStage2_AdditionalData(String visitUuid, String birthoutcome, String birthWeight, String apgar1min, String apgar5Min,
                                                String sex, String babyStatus, String mother_status) throws DAOException {

        boolean isInserted = false;
        String encounterUuid = "";
        encounterUuid = encounterDAO.insert_VisitCompleteEncounterToDb(visitUuid, sessionManager.getProviderID());

        VisitsDAO visitsDAO = new VisitsDAO();
        visitsDAO.updateVisitEnddate(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime());

        ////
        // Now get this encounteruuid and create refer obs table.
        if (!encounterUuid.isEmpty()) {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO;
            List<ObsDTO> obsDTOList = new ArrayList<>();

            // *. Birth Outcome
            obsDTO = new ObsDTO();
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setValue(birthoutcome);
            obsDTO.setConceptuuid(UuidDictionary.BIRTH_OUTCOME);
            obsDTOList.add(obsDTO);

            // 1. Birth Weight
            if (birthWeight != null && birthWeight.length() > 0) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(birthWeight);
                obsDTO.setConceptuuid(UuidDictionary.BIRTH_WEIGHT);
                obsDTOList.add(obsDTO);
            }

            // 2. Apgar 1 min
            if (apgar1min != null && apgar1min.length() > 0) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(apgar1min);
                obsDTO.setConceptuuid(UuidDictionary.APGAR_1_MIN);
                obsDTOList.add(obsDTO);
            }

            // 3. Apgar 5min
            if (apgar5Min != null && !apgar5Min.isEmpty()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(apgar5Min);
                obsDTO.setConceptuuid(UuidDictionary.APGAR_5_MIN);
                obsDTOList.add(obsDTO);
            }

            // 4. Sex
            if (sex != null && !sex.isEmpty()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(sex);
                obsDTO.setConceptuuid(UuidDictionary.SEX);
                obsDTOList.add(obsDTO);
            }

            // 5. Baby Status
            if (babyStatus != null && !babyStatus.isEmpty()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(babyStatus);
                obsDTO.setConceptuuid(UuidDictionary.BABY_STATUS);
                obsDTOList.add(obsDTO);
            }

            // 6. Mother Status
            if (mother_status != null && !mother_status.isEmpty()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(mother_status);
                obsDTO.setConceptuuid(UuidDictionary.MOTHER_STATUS);
                obsDTOList.add(obsDTO);
            }

            isInserted = obsDAO.insertObsToDb(obsDTOList);
        }

        return isInserted;
    }

    private boolean insertVisitCompleteEncounterAndObs_ReferHospital(String visitUuid, String referType,
                                                                     String hospitalName, String doctorName, String note) throws DAOException {
        boolean isInserted = true;
        String encounterUuid = "";
        encounterUuid = encounterDAO.insert_VisitCompleteEncounterToDb(visitUuid, sessionManager.getProviderID());

        VisitsDAO visitsDAO = new VisitsDAO();
        visitsDAO.updateVisitEnddate(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime());

        ////
        // Now get this encounteruuid and create refer obs table.
        if (!encounterUuid.isEmpty()) {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO;
            List<ObsDTO> obsDTOList = new ArrayList<>();

            // 1. Refer Type
            obsDTO = new ObsDTO();
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounterUuid);
            obsDTO.setValue(referType);
            obsDTO.setConceptuuid(UuidDictionary.REFER_TYPE);
            obsDTOList.add(obsDTO);

            // 2. Refer Hospital Name
            if (hospitalName != null && hospitalName.length() > 0) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(hospitalName);
                obsDTO.setConceptuuid(UuidDictionary.REFER_HOSPITAL);
                obsDTOList.add(obsDTO);
            }

            // 3. Refer Doctor Name
            if (doctorName != null && doctorName.length() > 0) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(doctorName);
                obsDTO.setConceptuuid(UuidDictionary.REFER_DR_NAME);
                obsDTOList.add(obsDTO);
            }

            // 4. Refer Note
            if (note != null && note.length() > 0) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(UUID.randomUUID().toString());
                obsDTO.setEncounteruuid(encounterUuid);
                obsDTO.setValue(note);
                obsDTO.setConceptuuid(UuidDictionary.REFER_NOTE);
                obsDTOList.add(obsDTO);
            }

            isInserted = obsDAO.insertObsToDb(obsDTOList);
        }

        return isInserted;
    }

    private boolean stage2captureAdditionalData(String value) {
        isAdded = false;
//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
//        String referOptions[] = {getString(R.string.birth_weight), getString(R.string.apgar_1min),
//                getString(R.string.apgar_5min), getString(R.string.sex), getString(R.string.baby_status), getString(R.string.mother_status)};
        DialogStage2AdditionalDataEzaziBinding binding = DialogStage2AdditionalDataEzaziBinding.inflate(getLayoutInflater(), null, true);
        showCustomViewDialog(R.string.additional_information, binding.getRoot(), () -> {
            String birthW = binding.birthWeight.getText().toString(),
                    apgar1min = binding.apgar1min.getText().toString(),
                    apgar5min = binding.apgar5min.getText().toString(),
                    sexValue = binding.sex.getText().toString(),
                    babyStatus = binding.babyStatus.getText().toString(),
                    motherStatus = binding.motherStatus.getText().toString();

            // call visitcompleteenc and add obs for additional values entered...
            try {
                isAdded = insertStage2_AdditionalData(visitUuid, value, birthW, apgar1min, apgar5min, sexValue, babyStatus, motherStatus);
            } catch (DAOException e) {
                e.printStackTrace();
            }

            if (isAdded) {
                Toast.makeText(context, context.getString(R.string.additional_info_submitted_successfully), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                checkInternetAndUploadVisit_Encounter();
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_stage2_additional_data, null);
//        dialogBuilder.setView(view);
//        dialogBuilder.setTitle(R.string.additional_information);
//        EditText birth_weight = view.findViewById(R.id.birth_weight);
//        EditText apgar_1min = view.findViewById(R.id.apgar_1min);
//        EditText apgar_5min = view.findViewById(R.id.apgar_5min);
//        EditText sex = view.findViewById(R.id.sex);
//        EditText baby_status = view.findViewById(R.id.baby_status);
//        EditText mother_status = view.findViewById(R.id.mother_status);
//
//        dialogBuilder.setPositiveButton(context.getString(R.string.submit), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                String birthW = birth_weight.getText().toString(),
//                        apgar1min = apgar_1min.getText().toString(),
//                        apgar5min = apgar_5min.getText().toString(),
//                        sexValue = sex.getText().toString(),
//                        babyStatus = baby_status.getText().toString(),
//                        motherStatus = mother_status.getText().toString();
//
//                // call visitcompleteenc and add obs for additional values entered...
//                try {
//                    isAdded = insertStage2_AdditionalData(visitUuid, value, birthW, apgar1min, apgar5min, sexValue, babyStatus, motherStatus);
//                } catch (DAOException e) {
//                    e.printStackTrace();
//                }
//
//                if (isAdded) {
//                    Toast.makeText(context, context.getString(R.string.additional_info_submitted_successfully), Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(context, HomeActivity.class);
//                    startActivity(intent);
//                    checkInternetAndUploadVisit_Encounter();
//                } else {
//                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                }
//                dialogInterface.dismiss();
//            }
//        });
//
//        dialogBuilder.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                dialog.dismiss();
//            }
//        });
//
//        AlertDialog dialog = dialogBuilder.show();
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//
//        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

        return isAdded;
    }


    private boolean insertVisitComplete_Obs(String visitUuid, String value, String conceptId) throws DAOException {
        //  EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();
        boolean isInserted = false;
        String encounterUuid = "";
        encounterUuid = encounterDAO.insert_VisitCompleteEncounterToDb(visitUuid, sessionManager.getProviderID());

        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            visitsDAO.updateVisitEnddate(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        ////

        // Now get this encounteruuid and create BIRTH_OUTCOME in obs table.
        isInserted = obsDAO.insert_Obs(encounterUuid, sessionManager.getCreatorID(), value, conceptId);

        return isInserted;
    }

    public void checkInternetAndUploadVisit_Encounter() {
        if (NetworkConnection.isOnline(getApplication())) {
            Toast.makeText(context, getResources().getString(R.string.syncInProgress), Toast.LENGTH_LONG).show();
            SyncDAO syncDAO = new SyncDAO();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //   Added the 4 sec delay and then push data.For some reason doing immediately does not work
                    //Do something after 100ms
                    SyncUtils syncUtils = new SyncUtils();
                    boolean isSynced = syncUtils.syncForeground("timeline");
                }
            }, 4000);
        } else {
            Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
        }
    }

    // Timeline stage 1 end confirmation dialog
    private void cancelStage1ConfirmationDialog() {
        showConfirmationDialog(R.string.are_you_sure_want_to_end_stage_1, () -> {
            // now start 15mins alarm for Stage 2 -> since 30mins is cancelled for Stage 1.
            //triggerAlarm_Stage2_every15mins(visitUuid);
            //cancelStage1_Alarm(); // cancel's stage 1 alarm
            String encounterUuid = UUID.randomUUID().toString();
            new ObsDAO().createEncounterType(encounterUuid, EncounterDTO.Type.NORMAL.name(), sessionManager.getCreatorID());
            createNewEncounter(encounterUuid, visitUuid, "Stage2_Hour1_1");
            fetchAllEncountersFromVisitForTimelineScreen(visitUuid);
            stageNo = 2;
            endStageButton.setText(context.getResources().getText(R.string.end2StageButton));
        });

//        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(context);
//        alertDialog.setTitle(R.string.are_you_sure_you_want_to_end_stage_1);
//        // alertDialog.setMessage("");
//        alertDialog.setPositiveButton(context.getResources().getString(R.string.yes),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // now start 15mins alarm for Stage 2 -> since 30mins is cancelled for Stage 1.
//                        //triggerAlarm_Stage2_every15mins(visitUuid);
//                        //cancelStage1_Alarm(); // cancel's stage 1 alarm
//                        createNewEncounter(visitUuid, "Stage2_Hour1_1");
//                        fetchAllEncountersFromVisitForTimelineScreen(visitUuid);
//                        endStageButton.setText(context.getResources().getText(R.string.end2StageButton));
//                        dialog.dismiss();
//                    }
//                });
//
//
//        alertDialog.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//                dialog.dismiss();
//            }
//        });

//        AlertDialog dialog = alertDialog.show();
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//        negativeButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//
//        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private void cancelStage2_Alarm() { // visituuid : 0 - 5
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        Log.v("timeline", "visituuid_int " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 5)), intent, NotificationUtils.getPendingIntentFlag());
        // to set different alarms for different patients.
        // vistiuuid: 0 - 4 index for stage 2
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void cancelStage1_Alarm() { // visituuid : 2 - 7
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        Log.v("timeline", "visituuid_int " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(2, 7)), intent, NotificationUtils.getPendingIntentFlag());
        // to set different alarms for different patients.
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    // create a new encounter for the first interval so that a new card is populated for Stage1Hr1_1...
/*
    private void createNewEncounter(String visitUuid) {
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();

        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("Stage1_Hour1_1"));
        encounterDTO.setSyncd(false); // false as this is the one that is started and would be pushed in the payload...
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value("true");

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }
*/

    private int mLastCount = 0;

    // fetch all encounters from encounter tbl local db for this particular visit and show on timeline...
    private void fetchAllEncountersFromVisitForTimelineScreen(String visitUuid) {
        //  encounterDAO = new EncounterDAO();
        encounterListDTO = encounterDAO.getEncountersByVisitUUID(visitUuid);
        for (int i = 0; i < encounterListDTO.size(); i++) {
            String name = encounterDAO.getEncounterTypeNameByUUID(encounterListDTO.get(i).getEncounterTypeUuid());
            EncounterDTO.Type type = new ObsDAO().getEncounterType(encounterListDTO.get(i).getUuid(), sessionManager.getCreatorID());
            encounterListDTO.get(i).setEncounterTypeName(name);
            encounterListDTO.get(i).setEncounterType(type);
        }
        isVCEPresent = encounterDAO.getVisitCompleteEncounterByVisitUUID(visitUuid);

        adapter = new TimelineAdapter(context, intent, encounterListDTO, sessionManager, isVCEPresent);
        Collections.reverse(encounterListDTO);
        recyclerView.setAdapter(adapter);
       /* if (encounterListDTO.size() != mLastCount) {
            mLastCount = encounterListDTO.size();
            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
        }*/
    }

/*
    public void triggerAlarm5MinsBefore() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 25); // So that just before 5mins of 30mins we get the notification
        Log.v("timeline", "25min: "+ calendar.getTime().toString());

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("timeTag", 5);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("providerID", providerID);

        Log.v("timeline", "patientname_5 "+ patientName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                5, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() ,
                    AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
            // trigger in millisec: here we state at what time do we want to trigger this notifi so when user comes to this screen from there
            // TODO: after 25mins this trigger should start up....
            //  triggerTime: 25mins from the time user came to this screen & repeatTime: 30mins everytime...
        }
    }
*/

    private void triggerAlarm_Stage2_every15mins(String visitUuid) { // TODO: change 1min to 15mins..... // visituuid : 0 - 5
        Calendar calendar = Calendar.getInstance(); // current time and from there evey 15mins notifi will be triggered...
        calendar.add(Calendar.MINUTE, 15); // So that after 15mins this notifi is triggered and scheduled...
        //  calendar.add(Calendar.MINUTE, 1); // Testing

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("timeTag", 15);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("providerID", providerID);
        intent.putExtra("Stage2_Hour1_1", "Stage1_Hour1_1");
//        intent.putExtra("Stage2_Hour1_1","Stage2_Hour1_1");

        Log.v("timeline", "patientname_3 " + patientName + " " + patientUuid + " " + visitUuid);
        Log.v("timeline", "visituuid_int_15min " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 5)), intent, NotificationUtils.getPendingIntentFlag());
        // to set different alarams for different patients.

        /*AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }*/
    }

    private void triggerAlarm_Stage1_every30mins() { // TODO: change 1min to 15mins..... // visituuid : 2 - 7
        Calendar calendar = Calendar.getInstance(); // current time and from there evey 30mins notifi will be triggered...
        calendar.add(Calendar.MINUTE, 30); // So that after 15mins this notifi is triggered and scheduled...
        // calendar.add(Calendar.MINUTE, 2); // Testing

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("patientNameTimeline", patientName);
        intent.putExtra("timeTag", 30);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("providerID", providerID);
        intent.putExtra("Stage1_Hour1_1", "Stage1_Hour1_1");

        Log.v("timeline", "patientname_3 " + patientName + " " + patientUuid + " " + visitUuid);
        Log.v("timeline", "visituuid_int_30min " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(2, 7)), intent, NotificationUtils.getPendingIntentFlag()); // to set different alarams for different patients.

        /*AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        }*/
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() { // when finish() called in Epartogram screen than onStart() is called here.
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    private void observeKeyboardEvent() {
//        getWindow().setDecorFitsSystemWindows(false);
        View decorView = getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            decorView.getWindowVisibleDisplayFrame(r);

            int height = decorView.getHeight();
            if (height - r.bottom > height * 0.1399) {
                //keyboard is open
                Log.e(TAG, "onGlobalLayout: keyboard open");
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            } else {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                Log.e(TAG, "onGlobalLayout: keyboard close");
            }
        });
    }
}