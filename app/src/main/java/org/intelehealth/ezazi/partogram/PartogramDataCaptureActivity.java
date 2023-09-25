package org.intelehealth.ezazi.partogram;

import static org.intelehealth.ezazi.partogram.PartogramConstants.STAGE_1;
import static org.intelehealth.ezazi.partogram.PartogramConstants.STAGE_2;
import static org.intelehealth.ezazi.partogram.PartogramConstants.TIMELINE_MODE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.content.IntentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.epartogramActivity.EpartogramViewActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.database.dao.EncounterDAO;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.VisitAttributeListDAO;
import org.intelehealth.ezazi.database.dao.VisitsDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.adapter.PartogramQueryListingAdapter;
import org.intelehealth.ezazi.partogram.model.Medicine;
import org.intelehealth.ezazi.partogram.model.ParamInfo;
import org.intelehealth.ezazi.partogram.model.PartogramItemData;
import org.intelehealth.ezazi.partogram.model.ValidatePartogramFields;
import org.intelehealth.ezazi.syncModule.SyncUtils;
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.dialog.SingleChoiceDialogFragment;
import org.intelehealth.ezazi.ui.dialog.model.SingChoiceItem;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziChatActivity;
import org.intelehealth.ezazi.ui.rtc.activity.EzaziVideoCallActivity;
import org.intelehealth.ezazi.ui.rtc.call.CallInitializer;
import org.intelehealth.ezazi.ui.visit.model.LabourInfo;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.UuidDictionary;
import org.intelehealth.ezazi.utilities.exception.DAOException;
import org.intelehealth.klivekit.model.RtcArgs;
import org.intelehealth.klivekit.socket.SocketManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

    private PartogramConstants.AccessMode accessMode;

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
        accessMode = (PartogramConstants.AccessMode) getIntent().getSerializableExtra(TIMELINE_MODE);
        context = PartogramDataCaptureActivity.this;
        if (accessMode != PartogramConstants.AccessMode.WRITE) {
            String title = accessMode == PartogramConstants.AccessMode.EDIT ? "Edit : History Collection" : "History Collection";
            getSupportActionBar().setTitle(title);
            mSaveTextView.setText(accessMode == PartogramConstants.AccessMode.EDIT ? "Update" : "Save");
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

        mSaveTextView.setOnClickListener(v -> {
            try {
                saveObs();
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
        });

        mSaveTextView.setEnabled(accessMode != PartogramConstants.AccessMode.READ);

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
                showDoctorSelectionDialog(true);
//                EncounterDAO encounterDAO = new EncounterDAO();
//                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(mVisitUUID);
//                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
//                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(mVisitUUID);
//                Intent chatIntent = new Intent(PartogramDataCaptureActivity.this, EzaziChatActivity.class);
//                chatIntent.putExtra("patientName", mPatientName);
//                chatIntent.putExtra("visitUuid", mVisitUUID);
//                chatIntent.putExtra("patientUuid", mPatientUuid);
//                chatIntent.putExtra("fromUuid", /*sessionManager.getProviderID()*/ encounterDTO.getProvideruuid()); // provider uuid
//                chatIntent.putExtra("isForVideo", false);
//                if (rtcConnectionDTO != null) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(rtcConnectionDTO.getConnectionInfo());
//                        chatIntent.putExtra("toUuid", jsonObject.getString("toUUID")); // assigned doctor uuid
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
//                    chatIntent.putExtra("toUuid", ""); // assigned doctor uuid
//                }
//                startActivity(chatIntent);
            }
        });
        Button btnVideoCall = findViewById(R.id.btnVideoOnOff);
        btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDoctorSelectionDialog(false);
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

    private void showDoctorSelectionDialog(boolean isChat) {
        LinkedList<SingChoiceItem> choiceItems = CallInitializer.getDoctorsDetails(mPatientUuid);

        SingleChoiceDialogFragment dialog = new SingleChoiceDialogFragment.Builder(this)
                .title(R.string.select_doctor)
                .content(choiceItems)
                .build();

        dialog.setListener(item -> {
            if (isChat) startChatActivity(item);
            else startVideoCallActivity(item);
        });

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }

    private void startChatActivity(SingChoiceItem item) {
        if (SocketManager.getInstance().checkUserIsOnline(item.getItemId())) {
            RtcArgs args = new RtcArgs();
            args.setPatientName(mPatientName);
            args.setPatientId(mPatientUuid);
            args.setVisitId(mVisitUUID);
            args.setNurseId(new SessionManager(getApplicationContext()).getProviderID());
            args.setDoctorUuid(item.getItemId());
            EzaziChatActivity.startChatActivity(this, args);
        } else Toast.makeText(this, item.getItem() + " is offline ", Toast.LENGTH_SHORT).show();
    }

    private void startVideoCallActivity(SingChoiceItem item) {
        if (SocketManager.getInstance().checkUserIsOnline(item.getItemId())) {
            SessionManager sessionManager = new SessionManager(this);
            Toast.makeText(this, item.getItem(), Toast.LENGTH_LONG).show();
            Log.v(TAG, "doctors  - " + item.getItem());
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
            args.setDoctorName(item.getItem());
            args.setDoctorUuid(item.getItemId());
            args.setIncomingCall(false);
            args.setNurseId(nurseId);
            args.setNurseName(sessionManager.getChwname());
            args.setRoomId(roomId);
            new CallInitializer(args).initiateVideoCall(args1 -> EzaziVideoCallActivity.startVideoCallActivity(PartogramDataCaptureActivity.this, args1));
        } else {
            Toast.makeText(this, item.getItem() + " is offline", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected int getScreenTitle() {
        return R.string.observations;
    }


    private void saveObs() throws DAOException {
        ObsDAO obsDAO = new ObsDAO();

        // validation
        Log.v("PartogramData", new Gson().toJson(mItemList));
        List<ObsDTO> obsDTOList = new ArrayList<>();
        String systolicBp = null;
        String diastolicBp = null;
        boolean isValidIVFluid = true;
        boolean isValidOxytocin = true;
        boolean isValidMedicine = true;
        boolean isValidBaselineFHR = true;
        boolean isValidPulse = true;
        boolean isValidSystolicBP = true;
        boolean isValidDiastolicBP = true;
        boolean isValidTemperature = true;
        boolean isValidDuration = true;

        List<String> voidedMedicines = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            for (int j = 0; j < mItemList.get(i).getParamInfoList().size(); j++) {
                ParamInfo info = mItemList.get(i).getParamInfoList().get(j);

                if (!info.getParamName().equalsIgnoreCase("Initial")) {

                    if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.SYSTOLIC_BP.value)) {
                        systolicBp = info.getCapturedValue();
                    }

                    if (info.getParamName().equalsIgnoreCase(PartogramConstants.Params.DIASTOLIC_BP.value)) {
                        diastolicBp = info.getCapturedValue();
                    }

                    if (info.getConceptUUID().equals(UuidDictionary.IV_FLUIDS)) {
                        isValidIVFluid = info.isValidJson();
                    }

                    if (info.getConceptUUID().equals(UuidDictionary.OXYTOCIN_UL_DROPS_MIN)) {
                        isValidOxytocin = info.isValidJson();
                    }
                    if (info.getConceptUUID().equals(UuidDictionary.BASELINE_FHR)) {
                        isValidBaselineFHR = ValidatePartogramFields.isValidParameter(info.getCapturedValue(), UuidDictionary.BASELINE_FHR);
                    }
                    if (info.getConceptUUID().equals(UuidDictionary.PULSE)) {
                        isValidPulse = ValidatePartogramFields.isValidParameter(info.getCapturedValue(), UuidDictionary.PULSE);
                    }
                    if (info.getConceptUUID().equals(UuidDictionary.SYSTOLIC_BP)) {
                        isValidSystolicBP = ValidatePartogramFields.isValidSystolicBP(info.getCapturedValue());
                    }
                    if (info.getConceptUUID().equals(UuidDictionary.DIASTOLIC_BP)) {
                        isValidDiastolicBP = ValidatePartogramFields.isValidDiastolicBP(systolicBp, diastolicBp);
                    }
                    if (info.getConceptUUID().equals(UuidDictionary.TEMPERATURE)) {
                        isValidTemperature = ValidatePartogramFields.isValidParameter(info.getCapturedValue(), UuidDictionary.TEMPERATURE);
                    }
                    if (info.getConceptUUID().equals(UuidDictionary.DURATION_OF_CONTRACTION)) {
                        isValidDuration = ValidatePartogramFields.isValidParameter(info.getCapturedValue(), UuidDictionary.DURATION_OF_CONTRACTION);
                    }

                    if (info.getConceptUUID().equals(UuidDictionary.MEDICINE)) {
                        isValidMedicine = info.isValidMedicine();
                        if (isValidMedicine && info.getCheckedRadioOption() != ParamInfo.RadioOptions.NO) {
                            obsDTOList.addAll(info.getMedicinesObsList(mEncounterUUID, new SessionManager(this).getCreatorID()));
                        }
                        voidedMedicines = info.getVoidedMedicineUuid();
                    } else {
                        ObsDTO obsDTOData = buildObservation(info);

                        String uuid = obsDAO.getObsuuid(mEncounterUUID, info.getConceptUUID());
                        obsDTOData.setUuid(uuid);

                        if (uuid != null && !uuid.isEmpty()) {
                            //update
                            obsDTOList.add(obsDTOData);

                        } else {
                            //insert
                            if (info.getCapturedValue() != null && !info.getCapturedValue().isEmpty()) {
                                obsDTOList.add(obsDTOData);
                            }
                        }
                    }
                }
            }
        }


        if (obsDTOList.isEmpty()) {
            showErrorDialog(R.string.please_enter_field_value);
        } /*else if (systolicBp != null && systolicBp.length() > 0
                && (diastolicBp == null || diastolicBp.length() == 0)) {
            showErrorDialog(R.string.error_diastolic_require);
        } */ else if (!isValidOxytocin) {
            showErrorDialog(R.string.error_oxytocin);
        } else if (!isValidIVFluid) {
            showErrorDialog(R.string.error_iv_fluid);
        } else if (!isValidMedicine) {
            showErrorDialog(R.string.error_medicine);
        } else if (!isValidBaselineFHR) {
            showErrorDialogForValidations(getString(R.string.baseline_fhr_err, AppConstants.MINIMUM_BASELINE_FHR, AppConstants.MAXIMUM_BASELINE_FHR));
        } else if (!isValidPulse) {
            showErrorDialogForValidations(getString(R.string.pulse_err, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
        } else if (!isValidSystolicBP) {
            //showErrorDialog(R.string.systolic_bp_range);
            showErrorDialogForValidations(getString(R.string.systolic_bp_range_toast_err, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

        } else if (!isValidDiastolicBP) {
            // showErrorDialog(R.string.diastolic_bp_range);
            showErrorDialogForValidations(getString(R.string.diastolic_bp_range_toast_err, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));

        } else if (!isValidTemperature) {
            showErrorDialogForValidations(getString(R.string.temp_error_new, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
        } else if (!isValidDuration) {
            showErrorDialogForValidations(getString(R.string.contraction_duration_err, AppConstants.MINIMUM_CONTRACTION_DURATION, AppConstants.MAXIMUM_CONTRACTION_DURATION));
        } else {

            try {
                if (accessMode == PartogramConstants.AccessMode.EDIT) {

                    //new logic
                    for (int i = 0; i < obsDTOList.size(); i++) {
                        ObsDTO obsDTO = obsDTOList.get(i);
                        if (obsDTO.getUuid() != null) {
                            obsDAO.updateObs(obsDTO);
                        } else {
                            obsDAO.insertObs(obsDTO);
                        }
                    }

                    if (voidedMedicines.size() > 0) {
                        obsDAO.markedAsVoidedObsToDb(voidedMedicines);
                    }
                } else {
                    obsDAO.insertObsToDb(obsDTOList, TAG);
                }
                new EncounterDAO().updateEncounterSync("false", mEncounterUUID);


                new VisitsDAO().updateVisitSync(mVisitUUID, "false");
                new VisitAttributeListDAO().markVisitAsRead(mVisitUUID);

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

    private ObsDTO buildObservation(ParamInfo info) {
        ObsDTO obsDTOData = new ObsDTO();
        obsDTOData.setCreator(new SessionManager(this).getCreatorID());
        obsDTOData.setEncounteruuid(mEncounterUUID);
        obsDTOData.setConceptuuid(info.getConceptUUID());
        obsDTOData.setValue(info.getCapturedValue());
        obsDTOData.setComment(PartogramAlertEngine.getAlertName(info));
        return obsDTOData;
    }

    private List<ObsDTO> mObsDTOList = new ArrayList<>();

    private void setEditData() {
        if (accessMode != PartogramConstants.AccessMode.WRITE) {
            mObsDTOList = new ObsDAO().getOBSByEncounterUUID(mEncounterUUID);
            for (int i = 0; i < mObsDTOList.size(); i++) {
                ObsDTO obsDTO = mObsDTOList.get(i);
                for (int j = 0; j < mItemList.size(); j++) {
                    for (int k = 0; k < mItemList.get(j).getParamInfoList().size(); k++) {
                        ParamInfo info = mItemList.get(j).getParamInfoList().get(k);
                        if (obsDTO.getConceptuuid().equals(info.getConceptUUID())) {
                            if (obsDTO.getConceptuuid().equals(UuidDictionary.MEDICINE)) {
                                info.setCapturedValue(ParamInfo.RadioOptions.YES.name());
                                info.convertToMedicine(obsDTO.getUuid(), obsDTO.getValue());
                            } else {
                                info.setCapturedValue(obsDTO.getValue());
                            }
                            break;
                        }
                    }

                }
            }
            Log.v("partogram", new Gson().toJson(mItemList));
        }
    }

    private void showErrorDialog(@StringRes int errorRes) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .content(getString(errorRes))
                .hideNegativeButton(true)
                .positiveButtonLabel(R.string.okay).build();
        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
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

        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter(mRecyclerView, this, mItemList, partogramItemData -> {

        });
        partogramQueryListingAdapter.setAccessMode(accessMode);
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

    private void showErrorDialogForValidations(String errorString) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .content(errorString)
                .hideNegativeButton(true)
                .positiveButtonLabel(R.string.okay).build();
        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());
    }
}