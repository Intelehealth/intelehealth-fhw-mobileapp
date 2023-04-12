package org.intelehealth.app.activities.searchPatientActivity;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.getQueryPatients;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Prajwal Waingankar On: 29/Aug/2022
 * Github: prajwalmw
 */

public class SearchPatientActivity_New extends AppCompatActivity {
    RecyclerView search_recycelview;
    SearchPatientAdapter_New adapter;
    EditText search_txt_enter;
    TextView search_hint_text, allPatientsTV, addPatientTV;
    String query;
    boolean fullyLoaded = false;
    RelativeLayout view_nopatientfound;
    public static final String TAG = "SearchPatient_New";
    private SearchRecentSuggestions suggestions;
    private SessionManager sessionManager;
    private SQLiteDatabase db;
    private ImageButton backbtn;
    View dividerView;
    ImageView iconSearch;

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
        dividerView = findViewById(R.id.divider_view);
        allPatientsTV = findViewById(R.id.all_patients_tv);
        addPatientTV = findViewById(R.id.add_new_patientTV);
        iconSearch = findViewById(R.id.icon_search);

        previous_SearchResults();
        queryAllPatients();

        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchPatientActivity_New.this, PrivacyPolicyActivity_New.class);
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                finish();
            }
        });

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!search_txt_enter.getText().toString().isEmpty()) {
                    dividerView.setVisibility(View.GONE);
                    allPatientsTV.setVisibility(View.GONE);
                    String text = search_txt_enter.getText().toString();
                    search_hint_text.setText("Results for \"" + text + "\"");
                    search_txt_enter.setTextColor(getResources().getColor(R.color.white));
                    managePreviousSearchStorage(text);
//                    sessionManager.setPreviousSearchQuery(text); // previous search feature.
                    query = text;
                    doQuery(text);
                }
            }
        });
        backbtn.setOnClickListener(v -> {
            finish();
        });

    }

    private void managePreviousSearchStorage(String text) {
        String retrievedPreviousData = sessionManager.getPreviousSearchQuery();
        List<String> retrievedPreviousSearchList = new ArrayList<>();
        if (retrievedPreviousData != null && !retrievedPreviousData.isEmpty() && !retrievedPreviousData.equalsIgnoreCase("")) {
            if (retrievedPreviousData.contains(",")) {
                retrievedPreviousSearchList = new ArrayList<String>(Arrays.asList(retrievedPreviousData.split(",")));
            } else
                retrievedPreviousSearchList.add(retrievedPreviousData);

            if (retrievedPreviousSearchList.size() == 3) {
                retrievedPreviousSearchList.remove(0);
            }
            retrievedPreviousSearchList.add(text);
            StringBuffer sb = new StringBuffer();
            for (String s : retrievedPreviousSearchList) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();
            sessionManager.setPreviousSearchQuery(str);
        } else {
            sessionManager.setPreviousSearchQuery(text);
        }
    }

    private void queryAllPatients() {
        List<PatientDTO> patientDTOList = PatientsDAO.getAllPatientsFromDB();
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

    private void previous_SearchResults() {
        if (search_txt_enter.getText().toString().isEmpty() ||
                search_txt_enter.getText().toString().equalsIgnoreCase("")) {
            String previousSearchText = sessionManager.getPreviousSearchQuery();
            List<String> retrievedPreviousSearchList = new ArrayList<>();

            if (previousSearchText.equalsIgnoreCase("") || previousSearchText.isEmpty()) {
                search_hint_text.setVisibility(View.GONE);
                dividerView.setVisibility(View.GONE);
            } else {
                if (previousSearchText.contains(",")) {
                    retrievedPreviousSearchList = new ArrayList<String>(Arrays.asList(previousSearchText.split(",")));
                } else
                    retrievedPreviousSearchList.add(previousSearchText);

                String value = "";
                for (int i = retrievedPreviousSearchList.size()-1; i >=0; i--) {
                    value = value +  "\"" + retrievedPreviousSearchList.get(i) + "\"" + "\n";
                }

                search_hint_text.setVisibility(View.VISIBLE);
                search_hint_text.setText("Previously Searched..\n" + value);
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
        if (sessionManager.getPreviousSearchQuery().isEmpty() && sessionManager.getPreviousSearchQuery().equalsIgnoreCase("")) {
            search_hint_text.setVisibility(View.GONE);
            allPatientsTV.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.GONE);
        }
    }

    private void searchData_Unavailable() {
        search_hint_text.setVisibility(View.GONE);
        view_nopatientfound.setVisibility(View.VISIBLE);
        search_recycelview.setVisibility(View.GONE);
        dividerView.setVisibility(View.GONE);
        allPatientsTV.setVisibility(View.GONE);
    }

}
