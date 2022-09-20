package org.intelehealth.app.activities.searchPatientActivity;

import static org.intelehealth.app.database.dao.PatientsDAO.getQueryPatients;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.utilities.Logger;

import java.util.List;

/**
 * Created by: Prajwal Waingankar On: 29/Aug/2022
 * Github: prajwalmw
 * */

public class SearchPatientActivity_New extends AppCompatActivity {
    RecyclerView search_recycelview;
    SearchPatientAdapter_New adapter;
    EditText search_txt_enter;
    TextView search_hint_text;
    String query;
    boolean fullyLoaded = false;
    FrameLayout view_nopatientfound;
    public static final String TAG = "SearchPatient_New";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient_new);

        search_recycelview = findViewById(R.id.search_recycelview);
        search_txt_enter = findViewById(R.id.search_txt_enter);
        search_hint_text = findViewById(R.id.search_hint_text);
        view_nopatientfound = findViewById(R.id.view_nopatientfound);

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
//                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchPatientActivity_New.this,
//                            SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
//                    suggestions.clearHistory();
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

    }

    private void doQuery(String query) {
        List<PatientDTO> patientDTOList = getQueryPatients(query);
        Log.v(TAG, "size: " + patientDTOList.size());
        if (patientDTOList.size() > 0) { //ie. the entered text is present in db
            searchData_Available();
            try {
                adapter = new SearchPatientAdapter_New(this, patientDTOList);
                fullyLoaded = true;
                search_recycelview.setAdapter(adapter);

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.logE("doquery", "doquery", e);
            }
        }
        else {
            searchData_Unavailable();
        }
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
