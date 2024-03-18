package org.intelehealth.app.activities.medicationAidActivity;

import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.ADDITIONAL_REMARKS;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.AID;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.COLLECTED;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.MEDICATION;
import static org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity.RECEIVED;
import static org.intelehealth.app.database.dao.ObsDAO.getObsPastNotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.medicationAidActivity.adapter.PastNotesAdapter;
import org.intelehealth.app.databinding.ActivityPastNotesDispenseAdministerBinding;
import org.intelehealth.app.models.dispenseAdministerModel.AidModel;
import org.intelehealth.app.models.dispenseAdministerModel.MedicationModel;
import org.intelehealth.app.models.dispenseAdministerModel.PastNotesModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by - Prajwal W. on 08/11/23.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

public class PastNotesDispenseAdministerActivity extends BaseActivity {
    public final String TAG = PastNotesDispenseAdministerActivity.this.getClass().getName();
    ActivityPastNotesDispenseAdministerBinding binding;
    private Context context;
    private String viewtag, tag, visitUUID;
    private PastNotesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPastNotesDispenseAdministerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = PastNotesDispenseAdministerActivity.this;

        Intent intent = getIntent();
        viewtag = intent.getStringExtra("viewtag");
        tag = intent.getStringExtra("mtag");
        visitUUID = intent.getStringExtra("visitUUID");
        if (tag != null && viewtag != null && visitUUID != null) {
            Log.d(TAG, "pastnotes Intent: " + viewtag + " : " + tag + " : " + visitUUID);
            try {
                toolBarSetup();
                initUI();
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void initUI() throws DAOException {
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        binding.rvPastnotes.setLayoutManager(manager);

        List<PastNotesModel> pastNotesModelList = new ArrayList<>();
        // todo: Fetch notes and datetime for both medication and aid -- Dispense/Admininster ENCOUNTERS.
        String encounterTypeUUID = null;
        String obsConceptUUID = null;
        Gson gson = new Gson();

        if (tag.equalsIgnoreCase("dispense")) {
            encounterTypeUUID = UuidDictionary.ENCOUNTER_DISPENSE;
            if (viewtag.equalsIgnoreCase("medication")) {
                obsConceptUUID = UuidDictionary.OBS_DISPENSE_MEDICATION;
            }
            else if (viewtag.equalsIgnoreCase("aid")) {
                obsConceptUUID = UuidDictionary.OBS_DISPENSE_AID;
            }
        }
        else if (tag.equalsIgnoreCase("administer")) {
            encounterTypeUUID = UuidDictionary.ENCOUNTER_ADMINISTER;
            if (viewtag.equalsIgnoreCase("medication")) {
                obsConceptUUID = UuidDictionary.OBS_ADMINISTER_MEDICATION;
            }
            else if (viewtag.equalsIgnoreCase("aid")) {
                obsConceptUUID = UuidDictionary.OBS_ADMINISTER_AID;
            }
        }
        else if (tag.equalsIgnoreCase(COLLECTED)) {
            encounterTypeUUID = UuidDictionary.ENCOUNTER_TEST_COLLECT;
            obsConceptUUID = UuidDictionary.OBS_TEST_COLLECT;
        }
        else if (tag.equalsIgnoreCase(RECEIVED)) {
            encounterTypeUUID = UuidDictionary.ENCOUNTER_TEST_RECEIVE;
            obsConceptUUID = UuidDictionary.OBS_TEST_RECEIVE;
        }
        else if (tag.equalsIgnoreCase(ADDITIONAL_REMARKS)) {
            encounterTypeUUID = UuidDictionary.ENCOUNTER_ADULTINITIAL;
            obsConceptUUID = UuidDictionary.ADDITIONAL_REMARKS;
        }


        if (encounterTypeUUID != null && obsConceptUUID != null) {
            List<PastNotesModel> list = getObsPastNotes(visitUUID, encounterTypeUUID, obsConceptUUID);
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    PastNotesModel model = gson.fromJson(list.get(i).getValue(), PastNotesModel.class);
                    String dateTime = DateAndTimeUtils.formatDateFromOnetoAnother
                            (model.getDateTime(),
                            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                            "dd MMM, yyyy HH:mm a");

                    if (viewtag.equalsIgnoreCase("medication") || viewtag.equalsIgnoreCase(COLLECTED) || viewtag.equalsIgnoreCase(RECEIVED))
                        pastNotesModelList.add(new PastNotesModel(model.getMedicationNotesList().get(0), dateTime));
                    else if (viewtag.equalsIgnoreCase("aid")) {
                        pastNotesModelList.add(new PastNotesModel(model.getAidNotesList().get(0), dateTime));
                    }
                    else if (viewtag.equalsIgnoreCase(ADDITIONAL_REMARKS)) {
                        pastNotesModelList.add(new PastNotesModel(model.getAdditional_remark(), dateTime));
                    }
                }
            }
        }

        if (pastNotesModelList.size() > 0) {
            binding.tvNodatafound.setVisibility(View.GONE);
            binding.rvPastnotes.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            adapter = new PastNotesAdapter(context, pastNotesModelList);
            binding.rvPastnotes.setAdapter(adapter);
        }
        else
            binding.tvNodatafound.setVisibility(View.VISIBLE);
    }

    private void toolBarSetup() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        binding.toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        String title = "";
        if (viewtag.equalsIgnoreCase(MEDICATION))
            title = getString(R.string.medicine_past_notes);
        else if (viewtag.equalsIgnoreCase(AID))
            title = getString(R.string.aid_past_notes);
        else if (viewtag.equalsIgnoreCase(COLLECTED))
            title = getString(R.string.view_past_test_notes);
        else if (viewtag.equalsIgnoreCase(RECEIVED))
            title = getString(R.string.view_resulted_by_past_notes);
        else if (viewtag.equalsIgnoreCase(ADDITIONAL_REMARKS))
            title = getString(R.string.additional_remarks);

        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_today_patient, menu);
        inflater.inflate(R.menu.today_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}