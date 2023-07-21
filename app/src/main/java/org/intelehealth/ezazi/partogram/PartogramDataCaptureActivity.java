package org.intelehealth.ezazi.partogram;

import static org.intelehealth.ezazi.partogram.PartogramConstants.STAGE_1;
import static org.intelehealth.ezazi.partogram.PartogramConstants.STAGE_2;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.epartogramActivity.EpartogramViewActivity;
import org.intelehealth.ezazi.activities.visitSummaryActivity.TimelineVisitSummaryActivity;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.RTCConnectionDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.models.dto.RTCConnectionDTO;
import org.intelehealth.ezazi.partogram.adapter.PartogramQueryListingAdapter;
import org.intelehealth.ezazi.partogram.model.ParamInfo;
import org.intelehealth.ezazi.partogram.model.PartogramItemData;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.ui.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziChatActivity;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziVideoCallActivity;
import org.intelehealth.ezazi.ui.rtc.call.CallInitializer;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PartogramDataCaptureActivity extends BaseActionBarActivity {
    private static final String TAG = "PartogramDataCaptureAct";
    private Button mSaveTextView, mEpartogramTextView;
    private RecyclerView mRecyclerView;
    private String mVisitUUID = "";
    private String mEncounterUUID = "";
    private String mEncounterNameUUID = "";
    private String mPatientUuid = "", mPatientName = "";
    private static final int HOURLY = 0;
    private static final int HALF_HOUR = 1;
    private static final int FIFTEEN_MIN = 2;
    //    private static final int STAGE_1 = 1;
//    private static final int STAGE_2 = 2;
    private int mQueryFor = HOURLY;
    private List<PartogramItemData> mItemList = new ArrayList<PartogramItemData>();
    private int mStageNumber = STAGE_1;
    private boolean mIsEditMode = false;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_partogram_data_capture_ezazi);
        super.onCreate(savedInstanceState);
        mSaveTextView = findViewById(R.id.btnSave);
        mEpartogramTextView = findViewById(R.id.btnView);
        mRecyclerView = findViewById(R.id.rvQuery);
        mVisitUUID = getIntent().getStringExtra("visitUuid");
        mEncounterUUID = getIntent().getStringExtra("encounterUuid");
        mPatientName = getIntent().getStringExtra("name");
        mPatientUuid = getIntent().getStringExtra("patientUuid");
        mStageNumber = getIntent().getIntExtra("stage", STAGE_1);
        mQueryFor = getIntent().getIntExtra("type", 0);
        mIsEditMode = getIntent().getBooleanExtra("isEditMode", false);
        context = PartogramDataCaptureActivity.this;
        if (mIsEditMode) {
            getSupportActionBar().setTitle("Edit : History Collection");
            mSaveTextView.setText("Update");
        }

        Log.v("visitUuid", mVisitUUID);
        Log.v("EncounterUUID", mEncounterUUID);
        Log.v("StageNumber", String.valueOf(mStageNumber));
        Log.v("QueryFor", String.valueOf(mQueryFor));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        if (mQueryFor == HOURLY) {
            prepareDataForHourly();
        } else if (mQueryFor == HALF_HOUR) {
            prepareDataForHalfHourly();
        } else if (mQueryFor == FIFTEEN_MIN) {
            prepareDataForFifteenMins();
        }

        mSaveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveObs();
                } catch (DAOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        mEpartogramTextView.setOnClickListener(v -> {

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            int widthPixels = metrics.widthPixels;
            int heightPixels = metrics.heightPixels;

            float scaleFactor = metrics.density;

            float widthDp = widthPixels / scaleFactor;
            float heightDp = heightPixels / scaleFactor;

            float smallestWidth = Math.min(widthDp, heightDp);
            Log.v("epartog", "smallest width: " + smallestWidth);

            /*if (smallestWidth >= 720) { // 8inch = 720 and 7inch == 600
                //Device is a 8" tablet
                // Call webview here...
                Intent intent = new Intent(this, EpartogramViewActivity.class);
                intent.putExtra("patientuuid", mPatientUuid);
                intent.putExtra("visituuid", mVisitUUID);
                startActivity(intent);
            }
            else {
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showOkDialog(PartogramDataCaptureActivity.this, "",
                        context.getString(R.string.this_option_available_tablet_device) *//*+ ": " + dpi*//*, context.getString(R.string.ok));
            }*/

            boolean isTablet = getResources().getBoolean(R.bool.isTablet);
            if (isTablet) {
                Intent intent = new Intent(this, EpartogramViewActivity.class);
                intent.putExtra("patientuuid", mPatientUuid);
                intent.putExtra("visituuid", mVisitUUID);
                startActivity(intent);
            } else {
                new ConfirmationDialogFragment.Builder(this).content(getString(R.string.this_option_available_tablet_device)).positiveButtonLabel(R.string.ok).hideNegativeButton(true).build().show(getSupportFragmentManager(), "ConfirmationDialogFragment");
            }

//            int dpi = context.getResources().getConfiguration().densityDpi;
//            Log.i("Timeline", "Screen size in DP: " + dpi);
//            if(dpi > 600) {
//                Intent intent = new Intent(this, Epartogram.class);
//                intent.putExtra("patientuuid", mPatientUuid);
//                intent.putExtra("visituuid", mVisitUUID);
//                startActivity(intent);
//            }
//            else {
//                Toast.makeText(context, R.string.this_option_available_tablet_device, Toast.LENGTH_SHORT).show();
//            }

        });

        Button btnChat = findViewById(R.id.btnFlipCamera);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EncounterDAO encounterDAO = new EncounterDAO();
                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(mVisitUUID);
                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(mVisitUUID);
                Intent chatIntent = new Intent(PartogramDataCaptureActivity.this, EzaziChatActivity.class);
                chatIntent.putExtra("patientName", mPatientName);
                chatIntent.putExtra("visitUuid", mVisitUUID);
                chatIntent.putExtra("patientUuid", mPatientUuid);
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
        Button btnVideoCall = findViewById(R.id.btnVideoOnOff);
        btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDoctorSelectionDialog();
//                EncounterDAO encounterDAO = new EncounterDAO();
//                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(mVisitUUID);
////                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
////                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(mVisitUUID);
//                Intent in = new Intent(PartogramDataCaptureActivity.this, EzaziVideoCallActivity.class);
//                String roomId = mPatientUuid;
//                String doctorName = "";
//                String nurseId = encounterDTO.getProvideruuid();
//                in.putExtra("roomId", roomId);
//                in.putExtra("isInComingRequest", false);
//                in.putExtra("doctorname", doctorName);
//                in.putExtra("nurseId", nurseId);
//                in.putExtra("startNewCall", true);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                }
//                int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
//                if (callState == TelephonyManager.CALL_STATE_IDLE) {
//                    startActivity(in);
//                }
            }
        });

    }

    private void showDoctorSelectionDialog() {
        HashMap<String, String> doctors = CallInitializer.getDoctorsDetails(mPatientUuid);
        ArrayList<SingChoiceItem> choiceItems = new ArrayList<>();
        for (String key : doctors.keySet()) {
            SingChoiceItem item = new SingChoiceItem();
            item.setItemId(doctors.get(key));
            item.setItem(key);
            choiceItems.add(item);
        }

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this)
                .title(R.string.select_doctor)
                .content(choiceItems)
                .build();

        dialog.setListener(item -> startVideoCallActivity(doctors, item.getItem()));

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void startVideoCallActivity(HashMap<String, String> doctors, String doctorName) {
        if (SocketManager.getInstance().checkUserIsOnline(doctors.get(doctorName))) {
            SessionManager sessionManager = new SessionManager(this);
            Toast.makeText(this, doctorName, Toast.LENGTH_LONG).show();
            Log.v(TAG, "doctors  - " + doctorName);
            EncounterDTO encounterDTO = new EncounterDAO().getEncounterByVisitUUIDLimit1(mVisitUUID);
            RtcArgs args = new RtcArgs();
            try {
                String patientOpenMrsId = new PatientsDAO().getOpenmrsId(mPatientUuid);
                args.setPatientOpenMrsId(patientOpenMrsId);
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }

            String nurseId = encounterDTO.getProvideruuid();
            String roomId = mPatientUuid;

            args.setVisitId(mVisitUUID);
            args.setPatientId(mPatientUuid);
            args.setPatientPersonUuid(mPatientUuid);
            args.setPatientName(mPatientName);
            args.setDoctorName(doctorName);
            args.setDoctorUuid(doctors.get(doctorName));
            args.setIncomingCall(false);
            args.setNurseId(nurseId);
            args.setNurseName(sessionManager.getChwname());
            args.setRoomId(roomId);
            new CallInitializer(args).initiateVideoCall(args1 -> EzaziVideoCallActivity.startVideoCallActivity(PartogramDataCaptureActivity.this, args1));
        } else {
            Toast.makeText(this, doctorName + " is offline", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected int getScreenTitle() {
        return R.string.observations;
    }


    private void saveObs() throws DAOException {
        ObsDAO obsDAO = new ObsDAO();

        // validation
        int count = 0;
        Log.v("PartogramData", new Gson().toJson(mItemList));
        List<ObsDTO> obsDTOList = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            for (int j = 0; j < mItemList.get(i).getParamInfoList().size(); j++) {

                if (!mItemList.get(i).getParamInfoList().get(j).getParamName().equalsIgnoreCase("Initial")) {
                    ObsDTO obsDTOData = new ObsDTO();
                    obsDTOData.setCreator(new SessionManager(this).getCreatorID());
                    obsDTOData.setEncounteruuid(mEncounterUUID);
                    obsDTOData.setConceptuuid(mItemList.get(i).getParamInfoList().get(j).getConceptUUID());
                    obsDTOData.setValue(mItemList.get(i).getParamInfoList().get(j).getCapturedValue());
                    obsDTOData.setComment(PartogramAlertEngine.getAlertName(mItemList.get(i).getParamInfoList().get(j)));

                    String uuid = obsDAO.getObsuuid(mEncounterUUID, mItemList.get(i).getParamInfoList().get(j).getConceptUUID());
                    obsDTOData.setUuid(uuid);

                    if (uuid != null && !uuid.isEmpty()) {
                        //update
                        obsDTOList.add(obsDTOData);

                    } else {
                        //insert
                        if (mItemList.get(i).getParamInfoList().get(j).getCapturedValue() != null && !mItemList.get(i).getParamInfoList().get(j).getCapturedValue().isEmpty()) {
                            obsDTOList.add(obsDTOData);
                        }
                    }
                    count++;
                }
            }
        }

        if (obsDTOList.isEmpty()) {
            ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this).content(getString(R.string.please_enter_field_value)).positiveButtonLabel(R.string.ok).build();
            dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());

//            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
//            alertDialogBuilder.setMessage("Please enter/select at least one field value!");
//            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//            alertDialogBuilder.show();
        } else {

            try {
                Log.d(TAG, "saveObs: main list size : " + obsDTOList.size());
                if (mIsEditMode) {

                    //new logic
                    for (int i = 0; i < obsDTOList.size(); i++) {
                        ObsDTO obsDTO = obsDTOList.get(i);
                        if (obsDTO.getUuid() != null) {
                            obsDAO.updateObs(obsDTO);
                        } else {
                            obsDAO.insertObs(obsDTO);
                        }
                    }
                } else {
                    obsDAO.insertObsToDb(obsDTOList);
                }
                new EncounterDAO().updateEncounterSync("false", mEncounterUUID);


                //visitsDAO.updateVisitSync(mVisitUUID, "false");

                SyncUtils syncUtils = new SyncUtils();
                boolean isSynced = syncUtils.syncForeground("visitSummary");
                if (isSynced) {
                    Toast.makeText(this, "Data uploaded successfully!", Toast.LENGTH_SHORT).show();
                    // AppConstants.notificationUtils.DownloadDone(getString(R.string.visit_data_upload), getString(R.string.visit_uploaded_successfully), 3, PartogramDataCaptureActivity.this);
                    finish();
                } else {
                    Toast.makeText(this, "Unable to upload the data!", Toast.LENGTH_SHORT).show();
                }

            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<ObsDTO> mObsDTOList = new ArrayList<>();

    private void setEditData() {
        if (mIsEditMode) {
            mObsDTOList = new ObsDAO().getOBSByEncounterUUID(mEncounterUUID);
            for (int i = 0; i < mObsDTOList.size(); i++) {
                ObsDTO obsDTO = mObsDTOList.get(i);
                for (int j = 0; j < mItemList.size(); j++) {
                    for (int k = 0; k < mItemList.get(j).getParamInfoList().size(); k++) {
                        if (obsDTO.getConceptuuid().equals(mItemList.get(j).getParamInfoList().get(k).getConceptUUID())) {
                            mItemList.get(j).getParamInfoList().get(k).setCapturedValue(obsDTO.getValue());
                            break;
                        }
                    }

                }
            }
            Log.v("partogram", new Gson().toJson(mItemList));
        }
    }

    private void prepareDataForHourly() {
        mItemList.clear();
        for (int i = 0; i < PartogramConstants.SECTION_LIST.length; i++) {
            String section = PartogramConstants.SECTION_LIST[i];
            List<ParamInfo> paramInfoList = PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section);
            PartogramItemData partogramItemData = new PartogramItemData();
            partogramItemData.setParamSectionName(section);
            partogramItemData.setParamInfoList(paramInfoList);
            mItemList.add(partogramItemData);
        }

        setEditData();

        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter(mRecyclerView, this, mItemList, new PartogramQueryListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(PartogramItemData partogramItemData) {

            }
        });
        mRecyclerView.setAdapter(partogramQueryListingAdapter);
    }

    private void prepareDataForHalfHourly() {
        mItemList.clear();
        for (int i = 0; i < PartogramConstants.SECTION_LIST.length; i++) {
            String section = PartogramConstants.SECTION_LIST[i];
            List<ParamInfo> paramInfoList = new ArrayList<>();
            for (int j = 0; j < PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).size(); j++) {
                ParamInfo paramInfo = PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).get(j);
                paramInfo.setCurrentStage(mStageNumber);
                if (mStageNumber == STAGE_1 && PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).get(j).isHalfHourField()) {
                    paramInfoList.add(paramInfo);
                } else if (mStageNumber == STAGE_2 && !PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).get(j).isOnlyOneHourField()) {
                    paramInfoList.add(paramInfo);
                }
            }
            if (!paramInfoList.isEmpty()) {
                PartogramItemData partogramItemData = new PartogramItemData();
                partogramItemData.setParamSectionName(section);
                partogramItemData.setParamInfoList(paramInfoList);
                mItemList.add(partogramItemData);
            }


        }
        setEditData();
        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter(mRecyclerView, this, mItemList, new PartogramQueryListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(PartogramItemData partogramItemData) {

            }
        });
        mRecyclerView.setAdapter(partogramQueryListingAdapter);
    }

    private void prepareDataForFifteenMins() {
        // TODO: Add logic here for 15mins section... @Lincoln
        mItemList.clear();
        for (int i = 0; i < PartogramConstants.SECTION_LIST.length; i++) {
            String section = PartogramConstants.SECTION_LIST[i];
            List<ParamInfo> paramInfoList = new ArrayList<>();
            for (int j = 0; j < PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).size(); j++) {
                if (PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).get(j).isFifteenMinField()) {
                    paramInfoList.add(PartogramConstants.getSectionParamInfoMasterMap(mStageNumber).get(section).get(j));
                }
            }
            if (!paramInfoList.isEmpty()) {
                PartogramItemData partogramItemData = new PartogramItemData();
                partogramItemData.setParamSectionName(section);
                partogramItemData.setParamInfoList(paramInfoList);
                mItemList.add(partogramItemData);
            }


        }
        setEditData();
        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter(mRecyclerView, this, mItemList, partogramItemData -> {
        });
        mRecyclerView.setAdapter(partogramQueryListingAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}