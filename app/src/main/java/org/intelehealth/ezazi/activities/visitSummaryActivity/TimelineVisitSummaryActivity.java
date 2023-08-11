package org.intelehealth.ezazi.activities.visitSummaryActivity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.epartogramActivity.EpartogramViewActivity;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.SyncDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.databinding.BirthOutcomeDialogBinding;
import org.intelehealth.ezazi.databinding.DialogOutOfTimeEzaziBinding;
import org.intelehealth.ezazi.databinding.DialogReferHospitalEzaziBinding;
import org.intelehealth.ezazi.databinding.DialogStage2AdditionalDataEzaziBinding;
import org.intelehealth.ezazi.databinding.LabourCompleteAndMotherDeceasedDialogBinding;
import org.intelehealth.ezazi.databinding.MotherDeceasedDialogBinding;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.models.dto.VisitDTO;
import org.intelehealth.ezazi.services.firebase_services.FirebaseRealTimeDBUtils;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.dialog.CustomViewDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziChatActivity;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

public class TimelineVisitSummaryActivity extends BaseActionBarActivity implements View.OnClickListener {
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

    private boolean hwHasEditAccess = true;
    private MaterialTextView tvReferToOtherHospital, tvSelfDischarge, tvReferToICU, tvShiftToCSection;
    private String dialogFor = "";
    private EditText etOtherCommentLabour, etOtherCommentOutcome, etReasonMotherDeceased;
    private boolean birthOutcomeSelected = false;
    private CheckBox cbLabourCompleted, cbMotherDeceased;
    private TextView selectedTextview;
    private String apgar1Min, apgar5Min, birthWeightInKg, birthWeightUnit, gender, labourCompletedValue;

    //private boolean isLabourCompleteSelected, isMotherDeceasedSelected, isLabourAndMotherDeceased;
    private String selectedBirthOutcome = "";
    private final String LABOUR_AND_MOTHER = "labourAndMother";
    private final String LABOUR_COMPLETED = "labourCompleted";
    private final String MOTHER_DECEASED = "motherDeceased";
    private String babyStatus, motherStatus, otherCommentLabour, motherDeceasedReason;
    private boolean isLabourCompletedChecked = false;
    private boolean isMotherDeceasedChecked = false;
    private boolean isLabourAndMotherDeceased = false;
    private String selectedViewText = "";
    private boolean labourCompletedSelected;
    private BottomSheetDialog bottomSheetDialogVisitComplete;

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
        fabc.setOnClickListener(view -> {
            showDoctorSelectionDialog(true);
        });
        fabv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: ");
                showDoctorSelectionDialog(false);
            }
        });

    }

    @Override
    protected int getScreenTitle() {
        return 0;
    }

    /**
     * Show the single choice doctor selection dialog and move forward
     * to video call with selected doctor from list
     */
    private void showDoctorSelectionDialog(boolean isChat) {
        HashMap<String, String> doctors = CallInitializer.getDoctorsDetails(patientUuid);
        ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
        for (String key : doctors.keySet()) {
            SingChoiceItem item = new SingChoiceItem();
            item.setItemId(doctors.get(key));
            item.setItem(key);
            choiceItems.add(item);
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this).title(R.string.select_doctor).content(choiceItems).build();

        dialog.setListener(item -> {
            if (isChat) {
                startChatActivity(doctors.get(item.getItem()), item.getItem());
            } else {
                startVideoCallActivity(doctors, item.getItem());
            }
        });

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void startChatActivity(String doctorUuid, String doctorName) {
        if (SocketManager.getInstance().checkUserIsOnline(doctorUuid)) {
            RtcArgs args = new RtcArgs();
            args.setPatientName(patientName);
            args.setPatientId(patientUuid);
            args.setVisitId(visitUuid);
            args.setNurseId(sessionManager.getProviderID());
            args.setDoctorUuid(doctorUuid);
            EzaziChatActivity.startChatActivity(this, args);
//            Intent chatIntent = new Intent(TimelineVisitSummaryActivity.this, EzaziChatActivity.class);
//            chatIntent.putExtra("patientName", patientName);
//            chatIntent.putExtra("visitUuid", visitUuid);
//            chatIntent.putExtra("patientUuid", patientUuid);
//            chatIntent.putExtra("fromUuid", sessionManager.getProviderID()); // provider uuid
//            chatIntent.putExtra("isForVideo", false);
//            chatIntent.putExtra("toUuid", doctorUuid);
//            startActivity(chatIntent);
        } else Toast.makeText(this, doctorName + " is offline ", Toast.LENGTH_SHORT).show();
    }

    /**
     * Start video call with selected doctor from primary and secondary list.
     *
     * @param doctors    HashMap
     * @param doctorName String
     */
    private void startVideoCallActivity(HashMap<String, String> doctors, String doctorName) {
        if (SocketManager.getInstance().checkUserIsOnline(doctors.get(doctorName))) {
            Toast.makeText(this, doctorName, Toast.LENGTH_LONG).show();
            Log.v(TAG, "doctors  - " + doctorName);
//        SocketManager.getInstance().setEmitterListener(emitter);
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
        } else {
            Toast.makeText(this, doctorName + " is offline", Toast.LENGTH_LONG).show();
        }
    }

//    private Function1<? super String, ? extends Emitter.Listener> emitter = (event) ->
//            (Emitter.Listener) args -> {
//
//            };

    private void showEmergencyDialog() {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this).title(R.string.emergency).positiveButtonLabel(R.string.yes).content(getString(R.string.are_you_sure_to_capture_emergency_data)).build();

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
                if (isTablet) showEpartogram();
                else showRequireTabletView();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }

    private void showRequireTabletView() {
        new ConfirmationDialogFragment.Builder(this).content(getString(R.string.this_option_available_tablet_device)).positiveButtonLabel(R.string.ok).hideNegativeButton(true).build().show(getSupportFragmentManager(), "ConfirmationDialogFragment");
    }

    private void showEpartogram() {
        Map<String, String> log = new HashMap<>();
        log.put("TAG", TAG);
        log.put("action", "showEpartogram");
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        Logger.logV("PHONE_TYPE_NONE", String.valueOf(Objects.requireNonNull(manager).getPhoneType()));

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
            hwHasEditAccess = new VisitsDAO().checkLoggedInUserAccessVisit(visitUuid, sessionManager.getProviderID());
            Log.v("timeline", "patientname_1 " + patientName + " " + patientUuid + " " + visitUuid);

            if (whichScreenUserCameFromTag != null && whichScreenUserCameFromTag.equalsIgnoreCase("new")) {
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

            if (!hwHasEditAccess) {
                fabc.setEnabled(false);
                fabv.setEnabled(false);
                fabSOS.setEnabled(false);
                endStageButton.setEnabled(false);
            }

        } else {
            String outcome = new ObsDAO().getCompletedVisitType(isVCEPresent);
            endStageButton.setVisibility(View.INVISIBLE);
            if (!outcome.equalsIgnoreCase("")) {
                outcomeTV.setVisibility(View.VISIBLE);
                setOutcomeText(outcome);
                outcomeTV.setGravity(Gravity.CENTER);
                checkForOutOfTime(outcome);
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

    private void checkForOutOfTime(String outcome) {
        boolean isOutOfTime = new ObsDAO().checkIsOutOfTimeEncounter(isVCEPresent);
        if (isOutOfTime) {
            MaterialButton button = findViewById(R.id.btnAddOutOfTimeReason);
            button.setTag(1);
            button.setVisibility(View.VISIBLE);
            button.setTag(R.id.btnAddOutOfTimeReason, outcome);
            if (!outcome.equals(VisitDTO.CompletedStatus.OUT_OF_TIME.value)) {
                button.setTag(2);
                button.setText(getString(R.string.update_out_of_time_reason));
                updateOutOfTimeOutcomeText(outcome);
            }
            button.setOnClickListener(outOfTimeClickListener);
            button.setVisibility(hwHasEditAccess ? View.VISIBLE : View.GONE);
        }
    }

    private void updateOutOfTimeOutcomeText(String reason) {
        outcomeTV.setGravity(Gravity.START);
        String label = VisitDTO.CompletedStatus.OUT_OF_TIME.value;
        String mainReason = getString(R.string.outcome_reason, reason);
        setOutcomeText(label + mainReason);
//        outcomeTV.setText(HtmlCompat.fromHtml(getString(R.string.lbl_outcome, outcome), 0));
    }

    private void setOutcomeText(String outcome) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outcomeTV.setText(Html.fromHtml(getString(R.string.lbl_outcome, outcome), 0));
        } else {
            outcomeTV.setText(Html.fromHtml(getString(R.string.lbl_outcome, outcome)));
        }
    }

    private View.OnClickListener outOfTimeClickListener = v -> {
        int isUpdateRequest = (int) v.getTag();
        String reason = (String) v.getTag(R.id.btnAddOutOfTimeReason);
        showOutOfTimeReasonInputDialog(isUpdateRequest, reason);
    };

    private void showOutOfTimeReasonInputDialog(int isUpdateRequest, String content) {
        DialogOutOfTimeEzaziBinding binding = DialogOutOfTimeEzaziBinding.inflate(getLayoutInflater(), null, true);
        binding.etOutOfTimeReason.setText(content);
        binding.etOutOfTimeReasonLayout.setMultilineInputEndIconGravity();
        showCustomViewDialog(R.string.time_out_reason, isUpdateRequest == 2 ? R.string.update_out_of_time_reason : R.string.add_out_of_time_reason, R.string.cancel, binding.getRoot(), () -> {
            String reason = binding.etOutOfTimeReason.getText().toString();
            if (reason.length() > 0) {
                int updated = new ObsDAO().updateOutOfTimeEncounterReason(reason, isVCEPresent, visitUuid);
                if (updated > 0) {
                    Toast.makeText(context, context.getString(R.string.time_out_info_submitted_successfully), Toast.LENGTH_SHORT).show();
//                            outcomeTV.setText(getString(R.string.lbl_outcome, reason));
                    updateOutOfTimeOutcomeText(reason);
                    updateButtonText(R.string.update_out_of_time_reason, 2, reason);
                    SyncUtils syncUtils = new SyncUtils();
                    syncUtils.syncBackground();
                } else {
                    Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Time out reason should not be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonText(int label, int tag, String content) {
        MaterialButton button = findViewById(R.id.btnAddOutOfTimeReason);
        button.setTag(tag);
        button.setTag(R.id.btnAddOutOfTimeReason, content);
        button.setText(getString(label));
    }

    private void showEndShiftDialog() {
        Log.e(TAG, "showEndShiftDialog");
        final String stage1Options[] = {getString(R.string.move_to_stage2), getString(R.string.refer_to_other_hospital), getString(R.string.self_discharge_medical_advice)};
        ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
        int count = 0;
        for (String str : stage1Options) {
            SingChoiceItem item = new SingChoiceItem();
            item.setItem(str);
            item.setItemIndex(count);
            choiceItems.add(item);
            count++;
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this).title(R.string.select_an_option).positiveButtonLabel(R.string.yes).content(choiceItems).build();

        dialog.setListener(item -> manageStageSelection(item.getItemIndex(), item.getItem()));

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
        if (position == 0) cancelStage1ConfirmationDialog(); // cancel and start stage 2
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
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this).content(getString(content)).positiveButtonLabel(R.string.yes).build();

        dialog.setListener(listener);

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void showCustomViewDialog(@StringRes int title, @StringRes int positiveLbl, @StringRes int negLbl, View view, CustomViewDialogFragment.OnConfirmationActionListener listener) {
        CustomViewDialogFragment dialog = new CustomViewDialogFragment.Builder(this).title(title).positiveButtonLabel(R.string.yes).view(view).build();
        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());

        dialog.setListener(listener);

    }

    private void referOtherHospitalDialog(String referType) {
//        String referOptions[] = {getString(R.string.refer_hospital_name), getString(R.string.refer_doctor_name), getString(R.string.refer_note)};

        DialogReferHospitalEzaziBinding binding = DialogReferHospitalEzaziBinding.inflate(getLayoutInflater(), null, false);

        showCustomViewDialog(R.string.refer_section, R.string.yes, R.string.no, binding.getRoot(), () -> {
            boolean isInserted = false;
            String hospitalName = binding.referHospitalName.getText().toString(), doctorName = binding.referDoctorName.getText().toString(), note = binding.referNote.getText().toString();

            // call visitcompleteenc and add obs for refer type and referal values entered...
            try {
                isInserted = insertVisitCompleteEncounterAndObs_ReferHospital(visitUuid, referType, hospitalName, doctorName, note);
            } catch (DAOException e) {
                e.printStackTrace();
            }

            Log.v(TAG, "referValue: " + "visit uuid: " + visitUuid + ", " + referType + ", " + hospitalName + ", " + doctorName + ", " + note);

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
        dialogFor = "birthoutcome";
        BirthOutcomeDialogBinding binding = BirthOutcomeDialogBinding.inflate(getLayoutInflater(), null, true);
        cbLabourCompleted = binding.cbLabourCompleted;
        cbMotherDeceased = binding.cbMotherDeceased;
        tvReferToOtherHospital = binding.tvReferToOtherHospital;
        tvSelfDischarge = binding.tvSelfDischarge;
        etOtherCommentOutcome = binding.etOtherCommentOutcomes;
        tvShiftToCSection = binding.tvShiftToSection;
        tvReferToICU = binding.tvReferToICU;

        tvReferToOtherHospital.setOnClickListener(this);
        tvSelfDischarge.setOnClickListener(this);
        tvShiftToCSection.setOnClickListener(this);
        tvReferToICU.setOnClickListener(this);

        cbLabourCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedTextview != null) {
                    selectedTextview.clearFocus();
                    selectedTextview.setSelected(false);
                    etOtherCommentOutcome.clearFocus();
                    etOtherCommentOutcome.setCursorVisible(false);
                    hideKeyboard(TimelineVisitSummaryActivity.this);

                }
            }
        });
        cbMotherDeceased.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedTextview != null) {
                    selectedTextview.clearFocus();
                    selectedTextview.setSelected(false);
                    etOtherCommentOutcome.clearFocus();
                    etOtherCommentOutcome.setCursorVisible(false);
                    hideKeyboard(TimelineVisitSummaryActivity.this);

                }
            }
        });

        etOtherCommentOutcome.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                birthOutcomeSelected = false;
                cbLabourCompleted.setChecked(false);
                cbMotherDeceased.setChecked(false);
                tvReferToOtherHospital.setSelected(false);
                tvSelfDischarge.setSelected(false);
                tvShiftToCSection.setSelected(false);
                tvReferToICU.setSelected(false);
                etOtherCommentOutcome.setCursorVisible(true);

                showKeyboard(v);
            }
        });

        showCustomViewDialog(R.string.additional_information, R.string.cancel, R.string.next, binding.getRoot(), this::manageBirthOutcomeSelection);

/*

//        positionStage = -1;
        final String[] items = {getString(R.string.live_birth), getString(R.string.still_birth),
                getString(R.string.refer_to_other_hospital), getString(R.string.self_discharge_medical_advice)};
        ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
        int count = 0;
        for (String str : items) {
            SingChoiceItem item = new SingChoiceItem();
            item.setItem(str);
            item.setItemIndex(count);
            choiceItems.add(item);
            count++;
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this)
                .title(R.string.select_birth_outcome)
                .positiveButtonLabel(R.string.yes)
                .content(choiceItems)
                .build();

        dialog.setListener(item -> manageBirthOutcomeSelection(item.getItemIndex(), item.getItem()));

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
*/

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

/*
    private void manageBirthOutcomeSelection(int position, String value) {
        boolean isInserted = false;
        // Birth Outcome
        if (position == 0 || position == 1) {
            Log.v("birthoutcome", "value: " + value);
            stage2captureAdditionalData();
            Log.v("isInserted", "isInserted_livebirth: " + isInserted);
*/
/*                            try {
                                isInserted = insertVisitComplete_Obs(visitUuid, value, UuidDictionary.BIRTH_OUTCOME);
                            } catch (DAOException e) {
                                e.printStackTrace();
                                Log.e("birthoutcome", "insert visit complete: " + e);
                            }*//*

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
*/

    private boolean insertStage2_AdditionalData(String visitUuid, String birthoutcome, String birthWeight, String apgar1min, String apgar5Min, String sex, String babyStatus, String mother_status) throws DAOException {
        Log.d(TAG, "insertStage2_AdditionalData: visitUuid : " + visitUuid);
        Log.d(TAG, "insertStage2_AdditionalData: birthoutcome : " + birthoutcome);
        Log.d(TAG, "insertStage2_AdditionalData: birthWeight : " + birthWeight);
        Log.d(TAG, "insertStage2_AdditionalData: apgar1min : " + apgar1min);
        Log.d(TAG, "insertStage2_AdditionalData: apgar5Min : " + apgar5Min);
        Log.d(TAG, "insertStage2_AdditionalData: sex : " + sex);
        Log.d(TAG, "insertStage2_AdditionalData: babyStatus : " + babyStatus);
        Log.d(TAG, "insertStage2_AdditionalData: mother_status : " + mother_status);

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

    private boolean insertVisitCompleteEncounterAndObs_ReferHospital(String visitUuid, String referType, String hospitalName, String doctorName, String note) throws DAOException {
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

    private boolean stage2captureAdditionalData(String selectedBirthOutcome) {
        isAdded = false;

         /*   String birthW = binding.birthWeight.getText().toString(),
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
            }*/


//        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context);
//        String referOptions[] = {getString(R.string.birth_weight), getString(R.string.apgar_1min),
//                getString(R.string.apgar_5min), getString(R.string.sex), getString(R.string.baby_status), getString(R.string.mother_status)};
      /*  old code - start - DialogStage2AdditionalDataEzaziBinding binding = DialogStage2AdditionalDataEzaziBinding.inflate(getLayoutInflater(), null, true);
        showCustomViewDialog(R.string.additional_information, R.string.yes, R.string.no, binding.getRoot(), () -> {
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
        });  -- end*/
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

    private void collectDataForMotherDeceased() {
        try {
            boolean isInsertedMotherDeceaseFlag = insertVisitComplete_Obs(visitUuid, String.valueOf(isMotherDeceasedChecked), UuidDictionary.MOTHER_DECEASED_FLAG);
            boolean isInsertedMotherDecease = insertVisitComplete_Obs(visitUuid, motherDeceasedReason, UuidDictionary.MOTHER_DECEASED);
            if (isInsertedMotherDecease && isInsertedMotherDeceaseFlag) {
                Toast.makeText(context, context.getString(R.string.reason_added_successful), Toast.LENGTH_SHORT).show();
                bottomSheetDialogVisitComplete.dismiss();
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                checkInternetAndUploadVisit_Encounter();
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private boolean insertVisitComplete_Obs(String visitUuid, String value, String conceptId) throws DAOException {
        Log.d(TAG, "insertStage2_AdditionalData: visitUuid : " + visitUuid);
        Log.d(TAG, "insertStage2_AdditionalData: value : " + value);
        Log.d(TAG, "insertStage2_AdditionalData: conceptId : " + conceptId);

        //  EncounterDAO encounterDAO = new EncounterDAO();
        //##remove ##kz
      /*  if (!visitUuid.isEmpty()) {

            return false;
        }*/

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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 5)), intent, NotificationUtils.getPendingIntentFlag());
        // to set different alarms for different patients.
        // vistiuuid: 0 - 4 index for stage 2
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void cancelStage1_Alarm() { // visituuid : 2 - 7
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        Log.v("timeline", "visituuid_int " + visitUuid.replaceAll("[^\\d]", ""));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(2, 7)), intent, NotificationUtils.getPendingIntentFlag());
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(0, 5)), intent, NotificationUtils.getPendingIntentFlag());
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.parseInt(visitUuid.replaceAll("[^\\d]", "").substring(2, 7)), intent, NotificationUtils.getPendingIntentFlag()); // to set different alarams for different patients.

        /*AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
        }*/
    }

    @Override
    public void onBackPressed() {
        if (whichScreenUserCameFromTag.equals("new")) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(AppConstants.REFRESH_SCREEN_EVENT, true);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
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

    @Override
    public void onClick(View view) {
        hideKeyboard(TimelineVisitSummaryActivity.this);

        isLabourAndMotherDeceased = false;
        labourCompletedSelected = false;

        if (dialogFor.equals("birthoutcome")) {
            birthOutcomeSelected = true;
            if (etOtherCommentOutcome != null && etOtherCommentOutcome.hasFocus())
                etOtherCommentOutcome.clearFocus();
            cbLabourCompleted.setChecked(false);
            cbMotherDeceased.setChecked(false);

        } /*else if (dialogFor.equals("labourCompleted")) {
            labourCompletedSelected = true;
            if (etOtherCommentLabour != null && etOtherCommentLabour.hasFocus())
                etOtherCommentLabour.clearFocus();
        } else if (dialogFor.equals("labourCompletedMotherDeceased")) {
            if ((etOtherCommentLabour != null && etOtherCommentLabour.hasFocus())) {
                etOtherCommentOutcome.clearFocus();
            }
        }*/

        //for labour completed and mother deceased both selected
        if (selectedTextview != null) {
           /* if ((selectedTextview.getId() == tvLabourCompleted.getId() && view.getId() == tvMotherDeceased.getId() || selectedTextview.getId() == tvMotherDeceased.getId() && view.getId() == tvLabourCompleted.getId())) {
                Log.d(TAG, "onClick: in for labour completed and mother deceased");
                isLabourAndMotherDeceased = true;
            }
            if (view.getId() == tvReferToOtherHospital.getId() || view.getId() == tvSelfDischarge.getId()) {
                //unselect the other options
                isLabourAndMotherDeceased = false;
                tvLabourCompleted.setSelected(false);
                tvMotherDeceased.setSelected(false);
            }*/

            if (cbLabourCompleted.isChecked() && cbMotherDeceased.isChecked()) {

            } else {
                // Unselect the previously selected view
                selectedTextview.setSelected(false);
            }
        }
        // Select the clicked view
        view.setSelected(true);
        selectedTextview = (TextView) view;
        selectedViewText = "";
        selectedViewText = ((TextView) view).getText().toString();
    }

    private void manageBirthOutcomeSelection() {
        isLabourCompletedChecked = cbLabourCompleted.isChecked();
        isMotherDeceasedChecked = cbMotherDeceased.isChecked();

        if (!isLabourCompletedChecked && !isMotherDeceasedChecked && selectedTextview == null && etOtherCommentOutcome.getText().toString().isEmpty()) {
            Toast.makeText(context, context.getString(R.string.please_select_an_option), Toast.LENGTH_SHORT).show();
        } else {
            if (isLabourCompletedChecked && isMotherDeceasedChecked) {
                // show ui for both labour completed and mother deceased
                selectedBirthOutcome = LABOUR_AND_MOTHER;
                showBottomSheetDialog(selectedBirthOutcome);

            } else if (isLabourCompletedChecked) {

                // show ui for labour completed only
                selectedBirthOutcome = LABOUR_COMPLETED;
                showBottomSheetDialog(selectedBirthOutcome);
            } else if (isMotherDeceasedChecked) {
                // show ui for mother deceased only
                selectedBirthOutcome = MOTHER_DECEASED;
                //  showBottomSheetDialog(selectedBirthOutcome);
                showMotherDeceasedDialog();

            } else if (selectedTextview.getId() == R.id.tvReferToOtherHospital) {
                // refer other hospital // call visit complete enc.
                referOtherHospitalDialog(value);

            } else if (selectedTextview.getId() == R.id.tvSelfDischarge) {
                // self discharge // call visit complete enc.
                try {
                    boolean isInserted = insertVisitComplete_Obs(visitUuid, context.getString(R.string.self_discharge_medical_advice), UuidDictionary.REFER_TYPE);
                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));

                } catch (DAOException e) {
                    e.printStackTrace();
                }
            } else if (selectedTextview.getId() == R.id.tvShiftToSection) {
                // Shift to C-Section // call visit complete enc.
                try {
                    boolean isInserted = insertVisitComplete_Obs(visitUuid, context.getString(R.string.shift_to_c_section), UuidDictionary.REFER_TYPE);
                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));

                } catch (DAOException e) {
                    e.printStackTrace();
                }
            } else if (selectedTextview.getId() == R.id.tvReferToICU) {
                //Refer to high dependency unit / ICU// call visit complete enc.
                try {
                    boolean isInserted = insertVisitComplete_Obs(visitUuid, context.getString(R.string.refer_to_icu), UuidDictionary.REFER_TYPE);
                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));

                } catch (DAOException e) {
                    e.printStackTrace();
                }
            } else if (!etOtherCommentOutcome.getText().toString().isEmpty()) {
                //for other comments - REFER_TYPE
                try {
                    boolean isInserted = insertVisitComplete_Obs(visitUuid, etOtherCommentOutcome.getText().toString(), UuidDictionary.REFER_TYPE);
                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));

                } catch (DAOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void showToastAndUploadVisit(boolean isInserted, String message) {
        if (isInserted) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            cancelStage2_Alarm(); // cancel stage 2 alarm so that again 15mins interval doesnt starts.
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
            checkInternetAndUploadVisit_Encounter();
        } else {
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void setDropdownsData(LabourCompleteAndMotherDeceasedDialogBinding binding) {
        //for labour completed dropdown
        final String[] birthOutcomeList = {getString(R.string.live_birth), getString(R.string.still_birth), getString(R.string.other_comment)};
        ArrayAdapter<String> labourCompletedAdapter = new ArrayAdapter<>(this, R.layout.spinner_textview, birthOutcomeList);
        binding.autotvLabourCompleted.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvLabourCompleted.setAdapter(labourCompletedAdapter);
        binding.autotvLabourCompleted.setOnItemClickListener((parent, view, position, id) -> {
            hideKeyboard(TimelineVisitSummaryActivity.this);
            labourCompletedValue = parent.getItemAtPosition(position).toString();
            String otherString = getString(R.string.other_comment).toLowerCase();
            if (!labourCompletedValue.isEmpty() && labourCompletedValue.equalsIgnoreCase(otherString)) {
                binding.etOtherComment.setEnabled(true);
                binding.etLayoutBirthWeight.setEnabled(false);
                binding.etLayoutApgar1.setEnabled(false);
                binding.etLayoutApgar5.setEnabled(false);
                binding.etLayoutBabyGender.setEnabled(false);
                binding.etLayoutBabyStatus.setEnabled(false);
                binding.etLayoutMotherStatus.setEnabled(false);
            } else {
                binding.etOtherComment.setEnabled(false);
                binding.etLayoutBirthWeight.setEnabled(true);
                binding.etLayoutApgar1.setEnabled(true);
                binding.etLayoutApgar5.setEnabled(true);
                binding.etLayoutBabyGender.setEnabled(true);
                binding.etLayoutBabyStatus.setEnabled(true);
                binding.etLayoutMotherStatus.setEnabled(true);
            }
        });

        List<Integer> itemsList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            itemsList.add(i);
        }
        List<Double> birthWeightList = new ArrayList<>();
        for (double i = 0.5; i <= 5; i += 0.5) {
            birthWeightList.add(i);
        }
        //for gender dropdown
        final String[] items = {getString(R.string.female), getString(R.string.male), getString(R.string.other)};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, R.layout.spinner_textview, items);
        binding.autotvBabyGender.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvBabyGender.setAdapter(genderAdapter);
        binding.autotvBabyGender.setOnItemClickListener((parent, view, position, id) -> {
            hideKeyboard(TimelineVisitSummaryActivity.this);
            gender = parent.getItemAtPosition(position).toString();
        });

        //for birth weight dropdown - in kg
        ArrayAdapter<Double> weightAdapter = new ArrayAdapter<>(this, R.layout.spinner_textview, birthWeightList);
        binding.autotvBirthWeight.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvBirthWeight.setAdapter(weightAdapter);
        binding.autotvBirthWeight.setOnItemClickListener((parent, view, position, id) -> {
            hideKeyboard(TimelineVisitSummaryActivity.this);
            birthWeightInKg = parent.getItemAtPosition(position).toString();
        });

        //for Apgar at 1min
        ArrayAdapter<Integer> apgar1 = new ArrayAdapter<>(this, R.layout.spinner_textview, itemsList);
        binding.autotvApgar1min.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvApgar1min.setAdapter(apgar1);
        binding.autotvApgar1min.setOnItemClickListener((parent, view, position, id) -> {
            hideKeyboard(TimelineVisitSummaryActivity.this);
            apgar1Min = parent.getItemAtPosition(position).toString();
        });

        //for Apgar at 5min
        ArrayAdapter<Integer> apgar2 = new ArrayAdapter<>(this, R.layout.spinner_textview, itemsList);
        binding.autotvApgar5min.setDropDownBackgroundResource(R.drawable.rounded_corner_white_with_gray_stroke);
        binding.autotvApgar5min.setAdapter(apgar2);
        binding.autotvApgar5min.setOnItemClickListener((parent, view, position, id) -> {
            hideKeyboard(TimelineVisitSummaryActivity.this);
            apgar5Min = parent.getItemAtPosition(position).toString();
        });

    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void collectDataForLabourCompleted() {

        // call visitcompleteenc and add obs for additional values entered...
        try {

            isAdded = insertStage2_AdditionalData(visitUuid, getSelectedBirthOutcomeValue(selectedBirthOutcome), birthWeightInKg, apgar1Min, apgar5Min, gender, babyStatus, motherStatus);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if (isAdded) {
            Toast.makeText(context, context.getString(R.string.additional_info_submitted_successfully), Toast.LENGTH_SHORT).show();
            bottomSheetDialogVisitComplete.dismiss();
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
            checkInternetAndUploadVisit_Encounter();

        } else {
            Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void collectDataForLabourAndMotherBoth() {
        try {
            boolean isLabourCompletedInserted = insertStage2_AdditionalData(visitUuid, getSelectedBirthOutcomeValue(selectedBirthOutcome), birthWeightInKg, apgar1Min, apgar5Min, gender, babyStatus, motherStatus);
            boolean isMotherDeceaseInsertedFlag = insertVisitComplete_Obs(visitUuid, String.valueOf(cbMotherDeceased.isChecked()), UuidDictionary.MOTHER_DECEASED_FLAG);
            boolean isMotherDeceaseInserted = insertVisitComplete_Obs(visitUuid, motherDeceasedReason, UuidDictionary.MOTHER_DECEASED);
            if (isLabourCompletedInserted && isMotherDeceaseInserted && isMotherDeceaseInsertedFlag) {
                Toast.makeText(context, context.getString(R.string.data_added_successfully), Toast.LENGTH_SHORT).show();
                bottomSheetDialogVisitComplete.dismiss();
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                checkInternetAndUploadVisit_Encounter();
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private void manageUIAsPerSelectedBirthOutcome(String selectedBirthOutcome, LabourCompleteAndMotherDeceasedDialogBinding binding) {

        if (selectedBirthOutcome.equals(LABOUR_AND_MOTHER)) {

        } else if (selectedBirthOutcome.equals(LABOUR_COMPLETED)) {
            binding.etLayoutDeceasedReason.setEnabled(false);

        } else if (selectedBirthOutcome.equals(MOTHER_DECEASED)) {
            showMotherDeceasedDialog();
           /* binding.etLayoutLabour.setEnabled(false);
            binding.etLayoutBirthWeight.setEnabled(false);
            binding.etLayoutApgar1.setEnabled(false);
            binding.etLayoutApgar5.setEnabled(false);
            binding.etLayoutBabyGender.setEnabled(false);
            binding.etLayoutBabyStatus.setEnabled(false);
            binding.etLayoutMotherStatus.setEnabled(false);
            binding.etLayoutOtherComment.setEnabled(false);*/
        }
    }

    private void showBottomSheetDialog(String selectedBirthOutcome) {
        bottomSheetDialogVisitComplete = new BottomSheetDialog(TimelineVisitSummaryActivity.this);
        LabourCompleteAndMotherDeceasedDialogBinding binding = LabourCompleteAndMotherDeceasedDialogBinding.inflate(getLayoutInflater(), null, true);
        bottomSheetDialogVisitComplete.setContentView(binding.getRoot());
        bottomSheetDialogVisitComplete.setCancelable(false);
        bottomSheetDialogVisitComplete.getWindow().setWindowAnimations(R.style.DialogAnimationSlideIn);
        enableAndDisableAllFields(binding, false);
        setDropdownsData(binding);
        manageUIAsPerSelectedBirthOutcome(selectedBirthOutcome, binding);

        binding.toolbar.setTitle(getString(R.string.complete_visit));
        binding.toolbar.setNavigationOnClickListener(v -> bottomSheetDialogVisitComplete.dismiss());
        bottomSheetDialogVisitComplete.setOnDismissListener(dialogInterface -> dialogInterface.dismiss());
        binding.btnSubmit.setOnClickListener(v -> {
            saveVisitCompletionDetails(binding);
        });

        bottomSheetDialogVisitComplete.show();
    }

    private void saveVisitCompletionDetails(LabourCompleteAndMotherDeceasedDialogBinding binding) {
        babyStatus = binding.etBabyStatus.getText().toString();
        motherStatus = binding.etMotherStatus.getText().toString();
        otherCommentLabour = binding.etOtherComment.getText().toString();
        motherDeceasedReason = binding.etDeceasedReason.getText().toString();

        if (selectedBirthOutcome.equals(LABOUR_AND_MOTHER)) {
            if (isValidForLabourAndMotherDeceased()) {
                collectDataForLabourAndMotherBoth();
            }

        } else if (selectedBirthOutcome.equals(LABOUR_COMPLETED)) {
            if ((labourCompletedValue.isEmpty() && gender.isEmpty() && birthWeightInKg.isEmpty() &&
                    apgar1Min.isEmpty() && apgar5Min.isEmpty() && etOtherCommentLabour.getText().toString().isEmpty())) {
                //please select/enter birth outcome
                Toast.makeText(context, getString(R.string.add_details_for_labour_completed), Toast.LENGTH_SHORT).show();
            } else {
                collectDataForLabourCompleted();
            }

        } else if (selectedBirthOutcome.equals(MOTHER_DECEASED)) {
            if (!motherDeceasedReason.isEmpty()) {
                collectDataForMotherDeceased();
            } else {
                Toast.makeText(context, getString(R.string.please_enter_reason), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showKeyboard(View editText) {
        InputMethodManager imm = (InputMethodManager) TimelineVisitSummaryActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showMotherDeceasedDialog() {
        MotherDeceasedDialogBinding binding = MotherDeceasedDialogBinding.inflate(getLayoutInflater(), null, false);

        showCustomViewDialog(R.string.mother_deceased, R.string.yes, R.string.no, binding.getRoot(), () -> {
            boolean isInserted = false;
            boolean isInsertedFlag = false;


            // call visitcompleteenc and add obs for mother deceased
            if (!binding.etMotherDeceasedReason.getText().toString().isEmpty()) {

            } else {
                Toast.makeText(context, getString(R.string.please_enter_reason), Toast.LENGTH_SHORT).show();
            }
            try {
                isInsertedFlag = insertVisitComplete_Obs(visitUuid, String.valueOf(cbMotherDeceased.isChecked()), UuidDictionary.MOTHER_DECEASED_FLAG);
                isInserted = insertVisitComplete_Obs(visitUuid, binding.etMotherDeceasedReason.getText().toString(), UuidDictionary.MOTHER_DECEASED);
                if (isInsertedFlag && isInserted)
                    showToastAndUploadVisit(isInserted, getResources().getString(R.string.data_added_successfully));

            } catch (DAOException e) {
                e.printStackTrace();
            }

            if (isInserted) {
                Toast.makeText(context, context.getString(R.string.refer_data_submitted_successfully), Toast.LENGTH_SHORT).show();
                bottomSheetDialogVisitComplete.dismiss();
                Intent intent = new Intent(context, HomeActivity.class);
                startActivity(intent);
                checkInternetAndUploadVisit_Encounter();
            } else {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedBirthOutcomeValue(String selectedBirthOutcome) {
        String selectedBirthOutcomeValue = "";
        if (!selectedBirthOutcome.isEmpty()) {
            if (selectedBirthOutcome.equalsIgnoreCase(LABOUR_COMPLETED)) {
                selectedBirthOutcomeValue = getString(R.string.labour_completed);
            } else if (selectedBirthOutcome.equalsIgnoreCase(LABOUR_AND_MOTHER)) {
                selectedBirthOutcomeValue = getString(R.string.labour_completed);

            } else if (selectedBirthOutcome.equalsIgnoreCase(MOTHER_DECEASED)) {
                selectedBirthOutcomeValue = getString(R.string.mother_deceased);
            }
        }
        return selectedBirthOutcomeValue;
    }

    private void enableAndDisableAllFields(LabourCompleteAndMotherDeceasedDialogBinding binding, boolean flag) {
        binding.etLayoutBirthWeight.setEnabled(flag);
        binding.etLayoutApgar1.setEnabled(flag);
        binding.etLayoutApgar5.setEnabled(flag);
        binding.etLayoutBabyGender.setEnabled(flag);
        binding.etLayoutBabyStatus.setEnabled(flag);
        binding.etLayoutMotherStatus.setEnabled(flag);
        binding.etLayoutOtherComment.setEnabled(flag);
    }

    private boolean isValidForLabourAndMotherDeceased() {
        //validation for labour completed and mother deceased
        // labourCompletedValue  gender birthWeightInKg apgar1Min apgar5Min - fields for labour completed
        boolean result = false;
        if ((labourCompletedValue.isEmpty() && gender.isEmpty() && birthWeightInKg.isEmpty() &&
                apgar1Min.isEmpty() && apgar5Min.isEmpty() && etOtherCommentLabour.getText().toString().isEmpty())) {
            Toast.makeText(context, getString(R.string.add_details_for_labour_completed), Toast.LENGTH_SHORT).show();
        } else if (motherDeceasedReason.isEmpty()) {
            Toast.makeText(context, getString(R.string.please_enter_reason_deceased), Toast.LENGTH_SHORT).show();
        } else {
            result = true;
        }
        return result;
    }
}