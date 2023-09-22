package org.intelehealth.unicef.activities.searchPatientActivity;

import static org.intelehealth.unicef.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.unicef.database.dao.PatientsDAO.getQueryPatients;
import static org.intelehealth.unicef.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.BaseActivity;
import org.intelehealth.unicef.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.unicef.activities.searchPatientActivity.adapter.SearchChipsPreviewGridAdapter;
import org.intelehealth.unicef.activities.visit.VisitAdapter;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.database.dao.EncounterDAO;
import org.intelehealth.unicef.database.dao.PatientsDAO;
import org.intelehealth.unicef.database.dao.ProviderDAO;
import org.intelehealth.unicef.models.dto.PatientDTO;
import org.intelehealth.unicef.models.dto.VisitDTO;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by: Prajwal Waingankar On: 29/Aug/2022
 * Github: prajwalmw
 */

public class SearchPatientActivity_New extends BaseActivity {
    RecyclerView search_recycelview;
    SearchPatientAdapter_New adapter;
    EditText mSearchEditText;
    TextView search_hint_text, allPatientsTV;
    LinearLayout addPatientTV;
    String query;
    boolean fullyLoaded = false;
    RelativeLayout view_nopatientfound;
    public static final String TAG = "SearchPatient_New";
    private SearchRecentSuggestions suggestions;
    private SessionManager sessionManager;
    private SQLiteDatabase db;
    private ImageButton backbtn;
    View dividerView;
    ImageView iconSearch, iconClear;
    private ImageButton searchFilter;
    private FrameLayout flFrameLayout;
    private SwitchMaterial flag;

    private RecyclerView mSearchHistoryRecyclerView;
    private ProviderDAO providerDAO = new ProviderDAO();

    private boolean isFlFrameLayoutVisible = false;

    // Pagination - start
    private int limit_default = 50, limit = 50;
    private int start_default = 0, end_default = start_default + limit_default,
            start = 0, end = start + limit;
    private boolean isFullyLoaded_default = false, isFullyLoaded = false;
    List<PatientDTO> recent_default = new ArrayList<>();
    List<PatientDTO> recent = new ArrayList<>();
    List<PatientDTO> patientDTOList_default, patientDTOList;
    LinearLayoutManager lm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient_new);
        sessionManager = new SessionManager(this);
        super.setLocale(sessionManager.getAppLanguage());

        mSearchEditText = findViewById(R.id.search_txt_enter);
        search_hint_text = findViewById(R.id.search_hint_text);
        view_nopatientfound = findViewById(R.id.view_nopatientfound);
        backbtn = findViewById(R.id.backbtn);
        dividerView = findViewById(R.id.divider_view);
        allPatientsTV = findViewById(R.id.all_patients_tv);
        addPatientTV = findViewById(R.id.add_new_patientTV);
        iconSearch = findViewById(R.id.icon_search);
        iconClear = findViewById(R.id.icon_clear);
        searchFilter = findViewById(R.id.search_filter);
        flFrameLayout = findViewById(R.id.filter_framelayout);
        flag = findViewById(R.id.flaggedcheckbox);
        flag.setChecked(true);

        search_recycelview = findViewById(R.id.search_recycelview);
        lm = new LinearLayoutManager(getApplicationContext());
        search_recycelview.setLayoutManager(lm);
        initializeRecyclerView(lm);

        mSearchHistoryRecyclerView = findViewById(R.id.rcv_selected_container);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);

        mSearchHistoryRecyclerView.setLayoutManager(layoutManager);

        previous_SearchResults();
//        queryAllPatients();
        doQueryForProvider(); // only query provider's patients

        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchPatientActivity_New.this, PrivacyPolicyActivity_New.class);
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                finish();
            }
        });

        searchFilter.setOnClickListener(v -> {
            if (isFlFrameLayoutVisible) {
                flFrameLayout.setVisibility(View.GONE);
            } else {
                flFrameLayout.setVisibility(View.VISIBLE);
            }
            isFlFrameLayoutVisible = !isFlFrameLayoutVisible;
        });

        flag.setOnCheckedChangeListener((buttonView, isChecked) -> {
            initializeRecyclerView(lm);
            resetData();

            if (isChecked) {
                doQueryForProvider();
            } else {
                queryAllPatients();
            }
            flFrameLayout.setVisibility(View.GONE);
            isFlFrameLayoutVisible = !isFlFrameLayoutVisible;
        });

        iconClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mSearchEditText.getText().toString().isEmpty()) {
                    recent_default.clear();
                    recent.clear();
                    mSearchEditText.setText("");
                }
            }
        });

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* if (!mSearchEditText.getText().toString().isEmpty()) {
                    //dividerView.setVisibility(View.GONE);
                    //allPatientsTV.setVisibility(View.GONE);
                    String text = mSearchEditText.getText().toString();
                    allPatientsTV.setText(getResources().getString(R.string.results_for) + " \"" + text + "\"");
                    mSearchEditText.setTextColor(getResources().getColor(R.color.white));
                    managePreviousSearchStorage(text);
//                  sessionManager.setPreviousSearchQuery(text); // previous search feature.
                    query = text;
                    doQuery(text);

                } else {
                    allPatientsTV.setText(getString(R.string.all_patients_txt));
                    query = "";
                    doQueryForProvider();

                }*/
                String searchText = mSearchEditText.getText().toString();
                performSearch(searchText);
            }
        });

        backbtn.setOnClickListener(v -> {
            finish();
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
/*
                if (editable.toString().isEmpty()) {
                    allPatientsTV.setText(getString(R.string.all_patients_txt));
                    query = "";
                    doQueryForProvider();
                }
*/
                // start
                if (editable.toString().isEmpty()) {
                    iconClear.setVisibility(View.GONE);
                    iconSearch.setVisibility(View.VISIBLE);
                    allPatientsTV.setText(getString(R.string.all_patients_txt));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            query = "";
                            doQuery(query); // todo: handle case here.
                        }
                    }, 100);

                } else {
                    iconClear.setVisibility(View.VISIBLE);
                    iconSearch.setVisibility(View.GONE);
                }
                // end
            }
        });

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(mSearchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }

    private void managePreviousSearchStorage(String text) {
        String retrievedPreviousData = sessionManager.getPreviousSearchQuery();
        List<String> retrievedPreviousSearchList = new ArrayList<>();
        if (retrievedPreviousData != null && !retrievedPreviousData.isEmpty()) {
            if (retrievedPreviousData.contains(",")) {
                retrievedPreviousSearchList = new ArrayList<String>(Arrays.asList(retrievedPreviousData.split(",")));
            } else
                retrievedPreviousSearchList.add(retrievedPreviousData);

            if (retrievedPreviousSearchList.size() == 5) {
                retrievedPreviousSearchList.remove(0);
            }
            if (!retrievedPreviousSearchList.contains(text))
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
        previous_SearchResults();
    }

    private void queryAllPatients() {
        patientDTOList = PatientsDAO.getAllPatientsFromDB(limit, start);   // fetch first 15 records and dont skip any records ie. start = 0 for 2nd itertion skip first 15records.
        Log.d(TAG, "queryAllPatients: " + patientDTOList.size());

        if (patientDTOList.size() > 0) { // ie. the entered text is present in db
            patientDTOList = fetchDataforTags(patientDTOList);
            Log.v(TAG, "size: " + patientDTOList.size());
            searchData_Available();
            try {
                adapter = new SearchPatientAdapter_New(this, patientDTOList);
                search_recycelview.setAdapter(adapter);
                start = end;
                end += limit;
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.logE("doquery", "doquery", e);
            }
        } else {
            searchData_Unavailable();
        }
    }

    private void previous_SearchResults() {

        String previousSearchText = sessionManager.getPreviousSearchQuery();
        List<String> raWPreviousSearchList = new ArrayList<>();
        List<String> retrievedPreviousSearchList = new ArrayList<>();

        if (previousSearchText.isEmpty()) {
            search_hint_text.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        } else {
            allPatientsTV.setVisibility(View.VISIBLE);
            if (previousSearchText.contains(",")) {
                raWPreviousSearchList = new ArrayList<String>(Arrays.asList(previousSearchText.split(",")));
            } else
                raWPreviousSearchList.add(previousSearchText);

            String value = "";
            for (int i = raWPreviousSearchList.size() - 1; i >= 0; i--) {
                //value = value + "\"" + retrievedPreviousSearchList.get(i) + "\"" + "\n";
                retrievedPreviousSearchList.add(raWPreviousSearchList.get(i));
            }

            search_hint_text.setVisibility(View.VISIBLE);
            //search_hint_text.setText("Previously Searched..\n" + value);
        }
        SearchChipsPreviewGridAdapter searchChipsPreviewGridAdapter = new SearchChipsPreviewGridAdapter(this, mSearchHistoryRecyclerView, retrievedPreviousSearchList, new SearchChipsPreviewGridAdapter.OnItemSelection() {
            @Override
            public void onSelect(String data) {
                mSearchEditText.setText(data);
                iconSearch.performClick();
            }

            @Override
            public void onRemoved(String data) {

            }
        });
        mSearchHistoryRecyclerView.setAdapter(searchChipsPreviewGridAdapter);


    }

    private void doQuery(String query) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);

        if (flag.isChecked()) {
            recent_default.clear();
            if (query.equalsIgnoreCase("")) {
                try {
                    searchData_Available();
                    adapter = new SearchPatientAdapter_New(this, patientDTOList_default);
                    isFullyLoaded_default = true;
                    search_recycelview.setAdapter(adapter);

                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Logger.logE("doquery", "doquery", e);
                }
                return;
            }

            recent_default = getQueryPatients(query);  // fetches all the list of patients.

            if (recent_default.size() > 0) { // ie. the entered text is present in db
                recent_default = fetchDataforTags(recent_default);
                Log.v(TAG, "size: " + recent_default.size());

                searchData_Available();
                try {
                    adapter = new SearchPatientAdapter_New(this, recent_default);
                    isFullyLoaded_default = true;
                    search_recycelview.setAdapter(adapter);

                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Logger.logE("doquery", "doquery", e);
                }
            } else {
                searchData_Unavailable();
            }
        }
        else {
            recent.clear();
            if (query.equalsIgnoreCase("")) {
                try {
                    searchData_Available();
                    adapter = new SearchPatientAdapter_New(this, patientDTOList);
                    fullyLoaded = true;
                    search_recycelview.setAdapter(adapter);

                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Logger.logE("doquery", "doquery", e);
                }
                return;
            }

            recent = getQueryPatients(query);  // fetches all the list of patients.

            if (recent.size() > 0) { // ie. the entered text is present in db
                recent = fetchDataforTags(recent);
                Log.v(TAG, "size: " + recent.size());

                searchData_Available();
                try {
                    adapter = new SearchPatientAdapter_New(this, recent);
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


      /*  patientDTOList = getQueryPatients(query);  // fetches all the list of patients.

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
        }*/
    }

    private void doQueryForProvider() {
        patientDTOList_default = providerDAO.doQueryWithProviders(limit_default, start_default, sessionManager.getProviderID());
        Log.d(TAG, "doQueryForProvider: " + patientDTOList_default.size());

        if (patientDTOList_default.size() > 0) {
            patientDTOList_default = fetchDataforTags(patientDTOList_default);
            Log.v(TAG, "size: " + patientDTOList_default.size());

            searchData_Available();
            try {
                adapter = new SearchPatientAdapter_New(this, patientDTOList_default);
                isFullyLoaded_default = true;
                search_recycelview.setAdapter(adapter);
                start_default = end_default;
                end_default += limit_default;
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
        mSearchHistoryRecyclerView.setVisibility(View.VISIBLE);
        search_hint_text.setVisibility(View.VISIBLE);
        view_nopatientfound.setVisibility(View.GONE);
        search_recycelview.setVisibility(View.VISIBLE);
        if (sessionManager.getPreviousSearchQuery().isEmpty() && sessionManager.getPreviousSearchQuery().equalsIgnoreCase("")) {
            search_hint_text.setVisibility(View.GONE);
            allPatientsTV.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.GONE);
        }
        findViewById(R.id.search_clear_tv).setVisibility(View.VISIBLE);
    }

    private void searchData_Unavailable() {
        mSearchHistoryRecyclerView.setVisibility(View.GONE);
        search_hint_text.setVisibility(View.GONE);
        view_nopatientfound.setVisibility(View.VISIBLE);
        search_recycelview.setVisibility(View.GONE);
        dividerView.setVisibility(View.GONE);
        allPatientsTV.setVisibility(View.GONE);
        findViewById(R.id.search_clear_tv).setVisibility(View.GONE);
    }

    public void clearSearch(View view) {
        mSearchEditText.setText("");
        view.setVisibility(View.GONE);
    }

    private void initializeRecyclerView(LinearLayoutManager linearLayoutManager) {
        if (flag.isChecked()) {
            search_recycelview.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (patientDTOList_default != null && patientDTOList_default.size() == 0) {
                        isFullyLoaded_default = true;
                        return;
                    }
                    if (!isFullyLoaded_default && newState == RecyclerView.SCROLL_STATE_IDLE &&
                            linearLayoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1) {
                        if (recent_default != null) {
                            if (recent_default.size() > 0) {

                            } else {
                                Toast.makeText(SearchPatientActivity_New.this, R.string.loading_more, Toast.LENGTH_SHORT).show();
                                setMoreDataIntoRecyclerView();
                            }
                        }
                    }
                }
            });
        }
        else {
            search_recycelview.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (patientDTOList != null && patientDTOList.size() == 0) {
                        isFullyLoaded = true;
                        return;
                    }
                    if (!isFullyLoaded && newState == RecyclerView.SCROLL_STATE_IDLE &&
                            linearLayoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1) {
                        if (recent != null) {
                            if (recent.size() > 0) {

                            } else {
                                Toast.makeText(SearchPatientActivity_New.this, R.string.loading_more, Toast.LENGTH_SHORT).show();
                                setMoreDataIntoRecyclerView();
                            }
                        }
                    }
                }
            });

        }
    }

    private void performSearch(String searchText) {
        if (!searchText.isEmpty()) {
            iconSearch.setVisibility(View.GONE);
            iconClear.setVisibility(View.VISIBLE);
            String text = searchText;

            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                allPatientsTV.setText("\"" + text + "\"" + " " + getResources().getString(R.string.results_for));
            else
                allPatientsTV.setText(getResources().getString(R.string.results_for) + " \"" + text + "\"");

            mSearchEditText.setTextColor(getResources().getColor(R.color.white));
            managePreviousSearchStorage(text);
            query = text;
            doQuery(text);
        } else {
            allPatientsTV.setText(getString(R.string.all_patients_txt));
            query = "";
            doQuery(query);
        }
    }

    private void setMoreDataIntoRecyclerView() {
        if (flag.isChecked()) {
            if (recent_default.size() > 0) {    // on scroll, new data loads issue fix.

            } else {
                if (patientDTOList_default != null && patientDTOList_default.size() == 0) {
                    isFullyLoaded_default = true;
                    return;
                }

                //   patientDTOList = PatientsDAO.getAllPatientsFromDB(limit, start);    // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
                List<PatientDTO> tempList = PatientsDAO.getAllPatientsFromDB(limit_default, start_default); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
                if (tempList.size() > 0) {
                    patientDTOList_default.addAll(tempList);
                    Log.d(TAG, "queryAllPatients: " + patientDTOList_default.size());
                    adapter.patientDTOS.addAll(tempList);
                    adapter.notifyDataSetChanged();
                    start_default = end_default;
                    end_default += limit_default;
                }
            }
        }
        else {
            if (recent.size() > 0) {    // on scroll, new data loads issue fix.

            } else {
                if (patientDTOList != null && patientDTOList.size() == 0) {
                    isFullyLoaded = true;
                    return;
                }

                //   patientDTOList = PatientsDAO.getAllPatientsFromDB(limit, start);    // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
                List<PatientDTO> tempList = PatientsDAO.getAllPatientsFromDB(limit, start); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
                if (tempList.size() > 0) {
                    patientDTOList.addAll(tempList);
                    Log.d(TAG, "queryAllPatients: " + patientDTOList.size());
                    adapter.patientDTOS.addAll(tempList);
                    adapter.notifyDataSetChanged();
                    start = end;
                    end += limit;
                }
            }
        }

    }

    private void initLimits() {
        limit_default = 15;
        start_default = 0;
        end_default = start_default + limit_default;

        limit = 15;
        start = 0;
        end = start + limit;
    }

    private void resetData() {
        initLimits();
        recent_default.clear();
        recent.clear();
       /*
        recentList = recentVisits(recentLimit, recentStart);
        olderList = olderVisits(olderLimit, olderStart);

        recentStart = recentEnd;
        recentEnd += recentLimit;
        olderStart = olderEnd;
        olderEnd += olderLimit;

        //
        recent_older_visibility(recentList, olderList);
        Log.d("TAG", "resetData: " + recentList.size() + ", " + olderList.size());

        recent_adapter = new VisitAdapter(getActivity(), recentList, sessionManager.getAppLanguage());
        recycler_recent.setNestedScrollingEnabled(false);
        recycler_recent.setAdapter(recent_adapter);

        older_adapter = new VisitAdapter(getActivity(), olderList, sessionManager.getAppLanguage());
        recycler_older.setNestedScrollingEnabled(false);
        recycler_older.setAdapter(older_adapter);*/
    }



}
