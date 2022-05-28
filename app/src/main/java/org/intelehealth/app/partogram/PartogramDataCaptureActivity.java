package org.intelehealth.app.partogram;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.partogram.adapter.PartogramQueryListingAdapter;
import org.intelehealth.app.partogram.model.ParamInfo;
import org.intelehealth.app.partogram.model.PartogramItemData;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class PartogramDataCaptureActivity extends AppCompatActivity {

    private TextView mSaveTextView;
    private RecyclerView mRecyclerView;
    private String mVisitUUID = "";
    private String mEncounterUUID = "";
    private String mEncounterNameUUID = "";
    private static final int HOURLY = 0;
    private static final int HALF_HOUR = 1;
    private static final int FIFTEEN_MIN = 2;
    private int mQueryFor = HOURLY;
    private List<PartogramItemData> mItemList = new ArrayList<PartogramItemData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partogram_data_capture);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("History Collection");
        mSaveTextView = findViewById(R.id.tvSave);
        mRecyclerView = findViewById(R.id.rvQuery);
        mVisitUUID = getIntent().getStringExtra("visitUuid");
        mEncounterUUID = getIntent().getStringExtra("encounterUuid");
        Log.v("visitUuid", mVisitUUID);
        Log.v("EncounterUUID", mEncounterUUID);
        mQueryFor = getIntent().getIntExtra("type", 0);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        if (mQueryFor == HOURLY) {
            prepareDataForHourly();
        } else if (mQueryFor == HALF_HOUR) {
            prepareDataForHalfHourly();
        }
        mSaveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveObs();
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
            try {
                obsDAO.insertObsToDb(obsDTOList);
                new EncounterDAO().updateEncounterSync("false", mEncounterUUID);
                SyncUtils syncUtils = new SyncUtils();
                boolean isSynced = syncUtils.syncForeground("visitSummary");
                if (isSynced) {
                    AppConstants.notificationUtils.DownloadDone(getString(R.string.visit_data_upload),
                            getString(R.string.visit_uploaded_successfully), 3, PartogramDataCaptureActivity.this);
                    finish();
                } else {
                    AppConstants.notificationUtils.DownloadDone(
                            getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, PartogramDataCaptureActivity.this);

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
                if (PartogramConstants.getSectionParamInfoMasterMap().get(section).get(j).isHalfHourField()) {
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
        PartogramQueryListingAdapter partogramQueryListingAdapter = new PartogramQueryListingAdapter(mRecyclerView, this, mItemList, new PartogramQueryListingAdapter.OnItemSelection() {
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