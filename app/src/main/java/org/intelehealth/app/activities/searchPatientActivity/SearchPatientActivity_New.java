package org.intelehealth.app.activities.searchPatientActivity;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.getQueryPatients;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;

/**
 * Created by: Prajwal Waingankar On: 29/Aug/2022
 * Github: prajwalmw
 */

public class SearchPatientActivity_New extends AppCompatActivity {
    RecyclerView search_recycelview;
    SearchPatientAdapter_New adapter;
    EditText search_txt_enter;
    TextView search_hint_text;
    String query;
    boolean fullyLoaded = false;
    FrameLayout view_nopatientfound;
    public static final String TAG = "SearchPatient_New";
    private SearchRecentSuggestions suggestions;
    private SessionManager sessionManager;
    private SQLiteDatabase db;
    private ImageButton backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient_new);

        sessionManager = new SessionManager(this);
        search_recycelview = findViewById(R.id.search_recycelview);
        search_txt_enter = findViewById(R.id.search_txt_enter);
        search_hint_text = findViewById(R.id.search_hint_text);
        view_nopatientfound = findViewById(R.id.view_nopatientfound);
        backbtn = findViewById(R.id.backbtn);

        previous_SearchResults();

        search_txt_enter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!search_txt_enter.getText().toString().isEmpty()) {
                    search_hint_text.setText("Results for \"" + s + "\"");
                    search_txt_enter.setTextColor(getResources().getColor(R.color.white));
                    String text = search_txt_enter.getText().toString();
                    sessionManager.setPreviousSearchQuery(text); // previous search feature.
                    query = text;
                    doQuery(text);
                }
            }

        });
/*
        search_txt_enter.setOnClickListener(v -> {
            search_txt_enter.clearFocus();
            String text = search_txt_enter.getText().toString();
            if(text!=null || !text.isEmpty() || text.equalsIgnoreCase(" "))
            {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchPatientActivity_New.this,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                query = text;
                doQuery(text);
            }
        });
*/
        backbtn.setOnClickListener(v -> {
            finish();
        });

    }

    private void previous_SearchResults() {
        if (search_txt_enter.getText().toString().isEmpty() ||
                search_txt_enter.getText().toString().equalsIgnoreCase("")) {
            if (sessionManager != null && !sessionManager.getPreviousSearchQuery().equalsIgnoreCase("")) {
                doQuery(sessionManager.getPreviousSearchQuery());
            }
        }
    }

    private void doQuery(String query) {
        List<PatientDTO> patientDTOList = getQueryPatients(query);  // fetches all the list of patients.

        if (patientDTOList.size() > 0) { // ie. the entered text is present in db
            patientDTOList = fetchDataforTags(patientDTOList);
            Log.v(TAG, "size: " + patientDTOList.size());

            searchData_Available();
            try {
                adapter = new SearchPatientAdapter_New(this, patientDTOList);
                fullyLoaded = true;
                search_recycelview.setAdapter(adapter);

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.logE("doquery", "doquery", e);
            }
        } else {
            searchData_Unavailable();
        }
    }

    private List<PatientDTO> fetchDataforTags(List<PatientDTO> patientDTOList) {
        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

        /**
         * 1. Check first if visit is present for this patient or not if yes than do other code logic.
         */
        for (int i = 0; i < patientDTOList.size(); i++) {
            VisitDTO visitDTO = isVisitPresentForPatient_fetchVisitValues(patientDTOList.get(i).getUuid());

            /**
             * 2. now check if only visit is present than only proceed to get value for priority tag, presc tag, startdate tag.
             */
            if (visitDTO.getUuid() != null && visitDTO.getStartdate() != null) {
                //  1. Priority Tag.
                EncounterDAO encounterDAO = new EncounterDAO();
                String emergencyUuid = "";
                try {
                    emergencyUuid = encounterDAO.getEmergencyEncounters(visitDTO.getUuid(), encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    emergencyUuid = "";
                }
                if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                    patientDTOList.get(i).setEmergency(true);
                } else { //ie. visit not emergency.
                    patientDTOList.get(i).setEmergency(false);
                }

                //  2. startdate added.
                String visit_start_date = DateAndTimeUtils.date_formatter(visitDTO.getStartdate(),
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        "dd MMM 'at' HH:mm a");    // Eg. 26 Sep 2022 at 03:15 PM
                Log.v("SearchPatient", "date: " + visit_start_date);

                patientDTOList.get(i).setVisit_startdate(visit_start_date);

                //  3. prescription received/pending tag logic.
                String encounteruuid = getStartVisitNoteEncounterByVisitUUID(visitDTO.getUuid());
                if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                    patientDTOList.get(i).setPrescription_exists(true);
                } else {
                    patientDTOList.get(i).setPrescription_exists(false);
                }
            } else {
                /**
                 * no visit for this patient.
                 * dont add startvisitdate value into this model keep it null and later check for null check and add logic
                 */
            }
        }

        return patientDTOList;
    }

    private void searchData_Available() {
        search_hint_text.setVisibility(View.VISIBLE);
        view_nopatientfound.setVisibility(View.GONE);
        search_recycelview.setVisibility(View.VISIBLE);
    }

    private void searchData_Unavailable() {
        search_hint_text.setVisibility(View.GONE);
        view_nopatientfound.setVisibility(View.VISIBLE);
        search_recycelview.setVisibility(View.GONE);
    }

}
