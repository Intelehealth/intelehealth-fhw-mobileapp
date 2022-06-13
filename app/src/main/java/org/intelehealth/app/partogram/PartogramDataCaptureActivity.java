package org.intelehealth.app.partogram;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.epartogramActivity.Epartogram;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.partogram.adapter.PartogramQueryListingAdapter;
import org.intelehealth.app.partogram.model.ParamInfo;
import org.intelehealth.app.partogram.model.PartogramItemData;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.apprtc.ChatActivity;
import org.intelehealth.apprtc.CompleteActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PartogramDataCaptureActivity extends AppCompatActivity {

    private TextView mSaveTextView, mEpartogramTextView;
    private RecyclerView mRecyclerView;
    private String mVisitUUID = "";
    private String mEncounterUUID = "";
    private String mEncounterNameUUID = "";
    private String mPatientUuid = "", mPatientName = "";
    private static final int HOURLY = 0;
    private static final int HALF_HOUR = 1;
    private static final int FIFTEEN_MIN = 2;
    private static final int STAGE_1 = 1;
    private static final int STAGE_2 = 2;
    private int mQueryFor = HOURLY;
    private List<PartogramItemData> mItemList = new ArrayList<PartogramItemData>();
    private int mStageNumber = STAGE_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partogram_data_capture);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("History Collection");
        mSaveTextView = findViewById(R.id.tvSave);
        mEpartogramTextView = findViewById(R.id.tvEpartogram);
        mRecyclerView = findViewById(R.id.rvQuery);
        mVisitUUID = getIntent().getStringExtra("visitUuid");
        mEncounterUUID = getIntent().getStringExtra("encounterUuid");
        mPatientName = getIntent().getStringExtra("name");
        mPatientUuid = getIntent().getStringExtra("patientUuid");
        mStageNumber = getIntent().getIntExtra("stage", STAGE_1);
        mQueryFor = getIntent().getIntExtra("type", 0);

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
                saveObs();
            }
        });

        mEpartogramTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, Epartogram.class);
            intent.putExtra("patientuuid", mPatientUuid);
            intent.putExtra("visituuid", mVisitUUID);
            startActivity(intent);
        });

        FloatingActionButton fabc = findViewById(R.id.fabc);
        fabc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EncounterDAO encounterDAO = new EncounterDAO();
                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(mVisitUUID);
                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(mVisitUUID);
                Intent chatIntent = new Intent(PartogramDataCaptureActivity.this, ChatActivity.class);
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
        FloatingActionButton fabv = findViewById(R.id.fabv);
        fabv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EncounterDAO encounterDAO = new EncounterDAO();
                EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(mVisitUUID);
                RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
                RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(mVisitUUID);
                Intent in = new Intent(PartogramDataCaptureActivity.this, CompleteActivity.class);
                String roomId = mPatientUuid;
                String doctorName = "";
                String nurseId = encounterDTO.getProvideruuid();
                in.putExtra("roomId", roomId);
                in.putExtra("isInComingRequest", false);
                in.putExtra("doctorname", doctorName);
                in.putExtra("nurseId", nurseId);
                in.putExtra("startNewCall", true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
                if (callState == TelephonyManager.CALL_STATE_IDLE) {
                    startActivity(in);
                }
            }
        });

    }


    private void saveObs() {
        // validation
        int count = 0;
        List<ObsDTO> obsDTOList = new ArrayList<>();
        for (int i = 0; i < mItemList.size(); i++) {
            for (int j = 0; j < mItemList.get(i).getParamInfoList().size(); j++) {
                if (mItemList.get(i).getParamInfoList().get(j).getCapturedValue() != null
                        && !mItemList.get(i).getParamInfoList().get(j).getCapturedValue().isEmpty()) {

                    if (!mItemList.get(i).getParamInfoList().get(j).getParamName().equalsIgnoreCase("Initial")) {
                        ObsDTO obsDTOData = new ObsDTO();
                        obsDTOData.setCreator(new SessionManager(this).getCreatorID());
                        obsDTOData.setEncounteruuid(mEncounterUUID);
                        obsDTOData.setConceptuuid(mItemList.get(i).getParamInfoList().get(j).getConceptUUID());
                        obsDTOData.setValue(mItemList.get(i).getParamInfoList().get(j).getCapturedValue());
                        obsDTOData.setComment(PartogramAlertEngine.getAlertName(mItemList.get(i).getParamInfoList().get(j)));
                        obsDTOList.add(obsDTOData);
                        count++;
                    }
                }
            }
        }

        if (obsDTOList.isEmpty()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage("Please enter/select at least one field value!");
            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
        } else {
            ObsDAO obsDAO = new ObsDAO();
            VisitsDAO visitsDAO = new VisitsDAO();
            try {
                obsDAO.insertObsToDb(obsDTOList);
                new EncounterDAO().updateEncounterSync("false", mEncounterUUID);
                //visitsDAO.updateVisitSync(mVisitUUID, "false");

                SyncUtils syncUtils = new SyncUtils();
                boolean isSynced = syncUtils.syncForeground("visitSummary");
                if (isSynced) {
                    Toast.makeText(this, "Data uploaded successfully!", Toast.LENGTH_SHORT).show();
                    /*AppConstants.notificationUtils.DownloadDone(getString(R.string.visit_data_upload),
                            getString(R.string.visit_uploaded_successfully), 3, PartogramDataCaptureActivity.this);*/
                    finish();
                } else {
                    Toast.makeText(this, "Unable to upload the data!", Toast.LENGTH_SHORT).show();
                }


            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
    }

    private void prepareDataForHourly() {
        mItemList.clear();
        for (int i = 0; i < PartogramConstants.SECTION_LIST.length; i++) {
            String section = PartogramConstants.SECTION_LIST[i];
            List<ParamInfo> paramInfoList = PartogramConstants.getSectionParamInfoMasterMap().get(section);
            PartogramItemData partogramItemData = new PartogramItemData();
            partogramItemData.setParamSectionName(section);
            partogramItemData.setParamInfoList(paramInfoList);
            mItemList.add(partogramItemData);

        }
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
            for (int j = 0; j < PartogramConstants.getSectionParamInfoMasterMap().get(section).size(); j++) {
                if (mStageNumber == STAGE_1 && PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j).isHalfHourField()) {
                    paramInfoList.add(PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j));
                } else if (mStageNumber == STAGE_2 && !PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j).isOnlyOneHourField()) {
                    paramInfoList.add(PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j));
                }
            }
            if (!paramInfoList.isEmpty()) {
                PartogramItemData partogramItemData = new PartogramItemData();
                partogramItemData.setParamSectionName(section);
                partogramItemData.setParamInfoList(paramInfoList);
                mItemList.add(partogramItemData);
            }


        }
        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter
                (mRecyclerView, this, mItemList, new PartogramQueryListingAdapter.OnItemSelection() {
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
            for (int j = 0; j < PartogramConstants.getSectionParamInfoMasterMap().get(section).size(); j++) {
                if (PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j).isFifteenMinField()) {
                    paramInfoList.add(PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j));
                }
            }
            if (!paramInfoList.isEmpty()) {
                PartogramItemData partogramItemData = new PartogramItemData();
                partogramItemData.setParamSectionName(section);
                partogramItemData.setParamInfoList(paramInfoList);
                mItemList.add(partogramItemData);
            }


        }
        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter
                (mRecyclerView, this, mItemList, new PartogramQueryListingAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(PartogramItemData partogramItemData) {

                    }
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