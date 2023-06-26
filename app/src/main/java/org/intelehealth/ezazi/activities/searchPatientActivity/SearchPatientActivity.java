package org.intelehealth.ezazi.activities.searchPatientActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ezazi.activities.privacyNoticeActivity.PrivacyNoticeActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.builder.PatientQueryBuilder;
import org.intelehealth.ezazi.builder.QueryBuilder;
import org.intelehealth.ezazi.database.dao.PatientsDAO;
import org.intelehealth.ezazi.database.dao.ProviderDAO;
import org.intelehealth.ezazi.executor.TaskCompleteListener;
import org.intelehealth.ezazi.executor.TaskExecutor;
import org.intelehealth.ezazi.models.dto.PatientAttributesDTO;
import org.intelehealth.ezazi.models.dto.PatientDTO;
import org.intelehealth.ezazi.ui.BaseActionBarActivity;
import org.intelehealth.ezazi.ui.patient.PatientDataBinder;
import org.intelehealth.ezazi.ui.patient.PatientStageBinder;
import org.intelehealth.ezazi.utilities.ConfigUtils;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringUtils;
import org.intelehealth.ezazi.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchPatientActivity extends BaseActionBarActivity implements SearchView.OnQueryTextListener {
    SearchView searchView;
    String query;
    private SearchPatientAdapter searchPatientAdapter;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;
    MaterialAlertDialogBuilder dialogBuilder;
    private String TAG = SearchPatientActivity.class.getSimpleName();
    private SQLiteDatabase db;
    ImageView new_patient;
    int limit = 10, offset = 0;
    boolean fullyLoaded = false;
    //    EditText toolbarET;
//    ImageView toolbarClear, toolbarSearch, toolbarFilter;
    LinearLayoutManager reLayoutManager;

    //    private PatientQueryBuilder queryBuilder;
    private PatientDataBinder dataBinder;

    private interface OnSearchCompleteListener {
        void onSearchCompleted(List<PatientDTO> results);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_search_patient_ezazi);
        super.onCreate(savedInstanceState);
        dataBinder = new PatientDataBinder();
        // Get the intent, verify the action and get the query


        //toolbar views
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);
//        toolbarET = findViewById(R.id.etvSearchVisit);
//        toolbarClear = findViewById(R.id.toolbar_clear);
//        toolbarSearch = findViewById(R.id.toolbar_search);
//        toolbarFilter = findViewById(R.id.toolbar_filter);
//        toolbarET.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
////                toolbarClear.setVisibility(View.VISIBLE);
////                toolbarSearch.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (toolbarET.getText().toString().isEmpty()) {
//                    toolbarET.clearFocus();
////                    toolbarClear.setVisibility(View.GONE);
////                    toolbarSearch.setVisibility(View.GONE);
//                    firstQuery();
//                }
//            }
//        });
//        toolbarClear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toolbarET.setText(null);
//                toolbarET.clearFocus();
//                toolbarClear.setVisibility(View.GONE);
//                toolbarSearch.setVisibility(View.GONE);
//                firstQuery();
//            }
//        });
//        toolbarSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toolbarET.clearFocus();
//                String text = toolbarET.getText().toString();
//                if (text != null || !text.isEmpty() || text.equalsIgnoreCase(" ")) {
//                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions
//                            (SearchPatientActivity.this,
//                                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
//                    suggestions.clearHistory();
//                    query = text;
//                    doQuery(text);
//                }
//            }
//        });

//        toolbarFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                displaySingleSelectionDialog();
//            }
//        });

        // Get the intent, verify the action and get the query
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the unicef should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        sessionManager = new SessionManager(this);

        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.recycle);
        reLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(reLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (searchPatientAdapter.patients != null && searchPatientAdapter.patients.size() < limit) {
                    return;
                }
                if (!fullyLoaded && newState == RecyclerView.SCROLL_STATE_IDLE && reLayoutManager.findLastVisibleItemPosition() == searchPatientAdapter.getItemCount() - 1) {
                    Toast.makeText(SearchPatientActivity.this, R.string.loading_more, Toast.LENGTH_SHORT).show();
                    offset += limit;
                    getAllPatientsFromDB(offset, results -> {
                        if (results.size() < limit) {
                            fullyLoaded = true;
                        }
                        searchPatientAdapter.patients.addAll(results);
                        searchPatientAdapter.notifyDataSetChanged();
                    });

                }
            }
        });
        new_patient = findViewById(R.id.new_patient);


        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (sessionManager.isPullSyncFinished()) {
                msg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                doQuery(query);
            }

        } else {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (sessionManager.isPullSyncFinished()) {
                msg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                firstQuery();
            }

        }

        new_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Loads the config file values and check for the boolean value of privacy key.
                ConfigUtils configUtils = new ConfigUtils(SearchPatientActivity.this);
                if (configUtils.privacy_notice()) {
                    Intent intent = new Intent(SearchPatientActivity.this, PrivacyNoticeActivity.class);
                    startActivity(intent);
                } else {
                    //Clear HouseHold UUID from Session for new registration
                    //  sessionManager.setHouseholdUuid("");
                    Intent intent = new Intent(SearchPatientActivity.this, IdentificationActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected int getScreenTitle() {
        return R.string.title_search_patient;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.clearOnScrollListeners();
    }

    private void doQuery(String query) {
        try {
//            searchPatientAdapter = new SearchPatientAdapter(getQueryPatients(query), SearchPatientActivity.this);
            fullyLoaded = true;
//            recyclerView.setAdapter(searchPatientAdapter);
//            bindAdapter(getQueryPatients(query));
//            searchFromAttributes(query, results -> bindAdapter(results));
            searchPatient(query, results -> bindAdapter(results));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("doquery", "doquery", e);
        }
    }


    private void bindAdapter(List<PatientDTO> patients) {
        if (patients.size() == 0) {
            searchPatientAdapter = new SearchPatientAdapter(patients, SearchPatientActivity.this);
            recyclerView.setAdapter(searchPatientAdapter);
            return;
        }

        new PatientStageBinder().bindStage(patients, results -> runOnUiThread(() -> {
            searchPatientAdapter = new SearchPatientAdapter(results, SearchPatientActivity.this);
            recyclerView.setAdapter(searchPatientAdapter);
        }));
    }


    private void firstQuery() {
        try {
            offset = 0;
            fullyLoaded = false;
//            searchPatientAdapter = new SearchPatientAdapter(getAllPatientsFromDB(offset), SearchPatientActivity.this);
//            recyclerView.setAdapter(searchPatientAdapter);
            getAllPatientsFromDB(offset, results -> bindAdapter(results));

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (searchPatientAdapter.patients != null && searchPatientAdapter.patients.size() < limit) {
                        return;
                    }

                    if (!fullyLoaded && newState == RecyclerView.SCROLL_STATE_IDLE && reLayoutManager.findLastVisibleItemPosition() == searchPatientAdapter.getItemCount() - 1) {
                        Toast.makeText(SearchPatientActivity.this, R.string.loading_more, Toast.LENGTH_SHORT).show();
                        offset += limit;
                        getAllPatientsFromDB(offset, results -> {
                            if (results.size() < limit) {
                                fullyLoaded = true;
                            }

                            new PatientStageBinder().bindStage(results, patients -> runOnUiThread(() -> {
                                searchPatientAdapter.patients.addAll(patients);
                                searchPatientAdapter.notifyDataSetChanged();
                            }));
                        });

                    }
                }
            });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE("firstquery", "exception", e);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the options menu from XMLz
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_search, menu);
//        inflater.inflate(R.menu.today_filter, menu);
////        inflater.inflate(R.menu.today_filter, menu);
//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setInputType(InputType.TYPE_CLASS_TEXT |
//                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //to show numbers easily...
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Log.d("Hack", "in query text change");
//                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchPatientActivity.this,
//                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
//                suggestions.clearHistory();
//                query = newText;
//                doQuery(newText);
//                return true;
//            }
//        });
//
//
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.summary_endAllVisit:
//                endAllVisit();
//
//            case R.id.action_filter:
//                //alert box.
//                displaySingleSelectionDialog();    //function call
//            case R.id.action_search:
//
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    /**
     * This method is called when no search result is found for patient.
     *
     * @param lvItems variable of type ListView
     * @param query   variable of type String
     */
    public void noneFound(ListView lvItems, String query) {
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_search,
                R.id.list_item_head, new ArrayList<String>());
        String errorMessage = getString(R.string.alert_none_found).replace("_", query);
        searchAdapter.add(errorMessage);
        lvItems.setAdapter(searchAdapter);
    }

    public void getAllPatientsFromDB(int offset, OnSearchCompleteListener listener) {
        new TaskExecutor<List<PatientDTO>>().executeTask(new TaskCompleteListener<List<PatientDTO>>() {
            @Override
            public List<PatientDTO> call() throws Exception {
                List<PatientDTO> modelList = new ArrayList<PatientDTO>();
                SQLiteDatabase database = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
                String query = new PatientQueryBuilder().pagingQuery(offset, limit);
                final Cursor searchCursor = database.rawQuery(query, null);

                try {
                    modelList = dataBinder.retrieveDataFromCursor(searchCursor);
                } catch (DAOException e) {
                    e.printStackTrace();
                } finally {
                    if (!searchCursor.isClosed()) searchCursor.close();
                }

                return modelList;
            }

            @Override
            public void onComplete(List<PatientDTO> result) {
                TaskCompleteListener.super.onComplete(result);
                if (listener != null) listener.onSearchCompleted(result);
            }
        });


    }

    private void endAllVisit() {

        int failedUploads = 0;

        String query = "SELECT tbl_visit.patientuuid, tbl_visit.enddate, tbl_visit.uuid," +
                "tbl_patient.first_name, tbl_patient.middle_name, tbl_patient.last_name FROM tbl_visit, tbl_patient WHERE" +
                " tbl_visit.patientuid = tbl_patient.uuid AND tbl_visit.enddate IS NULL OR tbl_visit.enddate = ''";

        final Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    boolean result = endVisit(
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")) + " " +
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid"))
                    );
                    if (!result) failedUploads++;
                } while (cursor.moveToNext());
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        if (failedUploads == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage("Unable to end " + failedUploads +
                    " visits.Please upload visit before attempting to end the visit.");
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        }

    }

    private boolean endVisit(String patientUuid, String patientName, String visitUUID) {

        return visitUUID != null;

    }

    private void displaySingleSelectionDialog() {

        ProviderDAO providerDAO = new ProviderDAO();
        ArrayList selectedItems = new ArrayList<>();
        selectedItems.clear();
        String[] creator_names = null;
        String[] creator_uuid = null;
        try {
            creator_names = providerDAO.getProvidersList().toArray(new String[0]);
            creator_uuid = providerDAO.getProvidersUuidList().toArray(new String[0]);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        dialogBuilder = new MaterialAlertDialogBuilder(SearchPatientActivity.this);
        dialogBuilder.setTitle(getString(R.string.filter_by_creator));

        String[] finalCreator_names = creator_names;
        String[] finalCreator_uuid = creator_uuid;
        dialogBuilder.setMultiChoiceItems(creator_names, null, new DialogInterface.OnMultiChoiceClickListener() {


            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                Logger.logD(TAG, "multichoice" + which + isChecked);
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
//                    if (finalCreator_uuid != null) {
                    selectedItems.add(finalCreator_uuid[which]);
//                    }

                } else if (selectedItems.contains(finalCreator_uuid[which])) {
                    // Else, if the item is already in the array, remove it
                    selectedItems.remove(finalCreator_uuid[which]);


                }

            }
        });

        dialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //display filter query code on list menu
                Logger.logD(TAG, "onclick" + i);
//                doQueryWithProviders(query, selectedItems);
//            }
                if (selectedItems.isEmpty())
                    firstQuery();
                else
                    doQueryWithProviders(selectedItems);
            }
        });

//        dialogBuilder.setNegativeButton(R.string.generic_cancel, null);
        dialogBuilder.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firstQuery();
            }
        });
        //dialogBuilder.show();
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));

        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);

    }

    private void searchPatient(String keyword, OnSearchCompleteListener listener) {
        SQLiteDatabase database = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        String query = new PatientQueryBuilder().searchQuery(keyword);
        new TaskExecutor<List<PatientDTO>>().executeTask(new TaskCompleteListener<List<PatientDTO>>() {
            @Override
            public List<PatientDTO> call() throws Exception {
                List<PatientDTO> modelList = new ArrayList<>();
                try (Cursor searchCursor = database.rawQuery(query, null)) {
                    modelList = dataBinder.retrieveDataFromCursor(searchCursor);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                return modelList;
            }

            @Override
            public void onComplete(List<PatientDTO> result) {
                TaskCompleteListener.super.onComplete(result);
                runOnUiThread(() -> {
                    if (listener != null) listener.onSearchCompleted(result);
                });
            }
        });
    }

//    private void searchFromAttributes(String query, OnSearchCompleteListener listener) {
//        SQLiteDatabase database = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        TaskExecutor<List<String>> executor = new TaskExecutor<>();
//        executor.executeTask(new TaskCompleteListener<List<String>>() {
//            @Override
//            public List<String> call() throws Exception {
//                List<String> patientUUID_List = new ArrayList<>();
//                final Cursor search_mobile_cursor = database.rawQuery("SELECT DISTINCT patientuuid FROM tbl_patient_attribute WHERE value = ?",
//                        new String[]{query});
//                /* DISTINCT will get remove the duplicate values. The duplicate value will come when you have created
//                 * a patient with mobile no. 12345 and patient is pushed than later you edit the mobile no to
//                 * 12344 or something. In this case, the local db maintains two separate rows both with value: 12344 */
//                //if no data is present against that corresponding cursor than cursor count returns = 0 ... i.e cursor_count = 0 ...
//                try {
//                    if (search_mobile_cursor.moveToFirst()) {
//                        do {
//                            patientUUID_List.add(search_mobile_cursor.getString
//                                    (search_mobile_cursor.getColumnIndexOrThrow("patientuuid")));
//                        }
//                        while (search_mobile_cursor.moveToNext());
//                    }
//                } catch (Exception e) {
//                    FirebaseCrashlytics.getInstance().recordException(e);
//                } finally {
//                    search_mobile_cursor.close();
//                }
//                return patientUUID_List;
//            }
//
//            @Override
//            public void onComplete(List<String> result) {
//                TaskCompleteListener.super.onComplete(result);
//                if (result.size() > 0)
//                    searchPatientByAttributes(result, query, listener);
//                else searchPatientFromTable(query, listener);
//            }
//        });
//    }

//    private void searchPatientByAttributes(List<String> attributeIds, String search, OnSearchCompleteListener listener) {
//        SQLiteDatabase database = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        new TaskExecutor<List<PatientDTO>>().executeTask(new TaskCompleteListener<List<PatientDTO>>() {
//            @Override
//            public List<PatientDTO> call() throws Exception {
//                List<PatientDTO> modelList = new ArrayList<>();
//                String table = "tbl_patient";
//                String columns = "uuid, openmrs_id, first_name, last_name, middle_name, date_of_birth";
//                for (int i = 0; i < attributeIds.size(); i++) {
////                    final Cursor searchCursor = database.rawQuery("SELECT " + columns + " FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR uuid = ? OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC",
////                            new String[]{attributeIds.get(i)});
//                    final Cursor searchCursor = database.rawQuery("SELECT " + columns + " FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR uuid = ? OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' ORDER BY first_name ASC",
//                            new String[]{attributeIds.get(i)});
//                    //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
//                    try {
//                        if (searchCursor.moveToFirst()) {
//                            do {
//                                PatientDTO model = new PatientDTO();
//                                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                                model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
//                                model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
//                                model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
//                                model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
//                                model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
//                                model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(model.getUuid())));
//                                String bedNod = getPatientBedNot(model.getUuid());
//                                model.setBedNo(bedNod);
//                                modelList.add(model);
//                            } while (searchCursor.moveToNext());
//                        }
//                    } catch (DAOException e) {
//                        FirebaseCrashlytics.getInstance().recordException(e);
//                    } finally {
//                        searchCursor.close();
//                    }
//                }
//                return modelList;
//            }
//
//            @Override
//            public void onComplete(List<PatientDTO> result) {
//                TaskCompleteListener.super.onComplete(result);
//                runOnUiThread(() -> {
//                    if (listener != null) listener.onSearchCompleted(result);
//                });
//            }
//        });
//    }

//    private void searchPatientFromTable(String search, OnSearchCompleteListener listener) {
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        new TaskExecutor<List<PatientDTO>>().executeTask(new TaskCompleteListener<List<PatientDTO>>() {
//            @Override
//            public List<PatientDTO> call() throws Exception {
//                List<PatientDTO> modelList = new ArrayList<>();
//                String table = "tbl_patient";
//                String columns = "uuid, openmrs_id, first_name, last_name, middle_name, date_of_birth";
////                final Cursor searchCursor = db.rawQuery("SELECT " + columns + " FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC",
////                        null);
//                final Cursor searchCursor = db.rawQuery("SELECT " + columns + " FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' ORDER BY first_name ASC",
//                        null);
//                //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
//                try {
//                    if (searchCursor.moveToFirst()) {
//                        do {
//                            PatientDTO model = new PatientDTO();
//                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
//                            model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
//                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
//                            model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
//                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
//                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
//                            modelList.add(model);
//                        } while (searchCursor.moveToNext());
//                    }
//                } catch (DAOException e) {
//                    FirebaseCrashlytics.getInstance().recordException(e);
//                } finally {
//                    searchCursor.close();
//                }
//                return modelList;
//            }
//
//            @Override
//            public void onComplete(List<PatientDTO> result) {
//                TaskCompleteListener.super.onComplete(result);
//                runOnUiThread(() -> {
//                    if (listener != null) listener.onSearchCompleted(result);
//                });
//            }
//        });
//    }

//    public List<PatientDTO> getQueryPatients(String query) {
//        String search = query.trim().replaceAll("\\s", "");
//        // search = StringUtils.mobileNumberEmpty(phoneNumber());
//        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
//        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
//        String table = "tbl_patient";
//        List<String> patientUUID_List = new ArrayList<>();
//
//        final Cursor search_mobile_cursor = db.rawQuery("SELECT DISTINCT patientuuid FROM tbl_patient_attribute WHERE value = ?",
//                new String[]{search});
//        /* DISTINCT will get remove the duplicate values. The duplicate value will come when you have created
//         * a patient with mobile no. 12345 and patient is pushed than later you edit the mobile no to
//         * 12344 or something. In this case, the local db maintains two separate rows both with value: 12344 */
//        //if no data is present against that corresponding cursor than cursor count returns = 0 ... i.e cursor_count = 0 ...
//        try {
//            if (search_mobile_cursor.moveToFirst()) {
//                do {
//                    patientUUID_List.add(search_mobile_cursor.getString
//                            (search_mobile_cursor.getColumnIndexOrThrow("patientuuid")));
//                }
//                while (search_mobile_cursor.moveToNext());
//            }
//        } catch (Exception e) {
//            FirebaseCrashlytics.getInstance().recordException(e);
//        }
//        Log.d("patientUUID_list", "list: " + patientUUID_List.toString());
//        if (patientUUID_List.size() != 0) {
//            for (int i = 0; i < patientUUID_List.size(); i++) {
//                final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR uuid = ? OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC",
//                        new String[]{patientUUID_List.get(i)});
//                //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
//                try {
//                    if (searchCursor.moveToFirst()) {
//                        do {
//                            PatientDTO model = new PatientDTO();
//                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
//                            model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
//                            model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
//                            model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
//                            model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
//                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(model.getUuid())));
//                            String bedNod = getPatientBedNot(model.getUuid());
//                            model.setBedNo(bedNod);
////                            model.setStage(getStage(model.getUuid()));
//
//                            modelList.add(model);
//                        } while (searchCursor.moveToNext());
//                    }
//                } catch (DAOException e) {
//                    FirebaseCrashlytics.getInstance().recordException(e);
//                }
//            }
//        } else {
//            final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " + "'%" + search + "%' OR middle_name LIKE '%" + search + "%' OR last_name LIKE '%" + search + "%' OR (first_name || middle_name) LIKE '%" + search + "%' OR (middle_name || last_name) LIKE '%" + search + "%' OR (first_name || last_name) LIKE '%" + search + "%' OR openmrs_id LIKE '%" + search + "%' " + "ORDER BY first_name ASC",
//                    null);
//            //  if(searchCursor.getCount() != -1) { //all values are present as per the search text entered...
//            try {
//                if (searchCursor.moveToFirst()) {
//                    do {
//                        PatientDTO model = new PatientDTO();
//                        model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                        model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
//                        model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
//                        model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
//                        model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
//                        model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
//                        model.setDateofbirth(searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth")));
//                        model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")))));
//                        modelList.add(model);
//                    } while (searchCursor.moveToNext());
//                }
//            } catch (DAOException e) {
//                FirebaseCrashlytics.getInstance().recordException(e);
//            }
//        }
//        return modelList;
//    }

    private String getPatientBedNot(String patientUuid) {
        PatientsDAO patientsDAO = new PatientsDAO();
        return patientsDAO.getPatientAttributeValue(patientUuid, PatientAttributesDTO.Columns.BED_NUMBER);
    }

    //    private void doQueryWithProviders(String querytext, List<String> providersuuids) {
    private List<PatientDTO> doQueryWithProviders(List<String> providersuuids) {
        List<PatientDTO> modelListwihtoutQuery = new ArrayList<PatientDTO>();
//        if (querytext == null) {
//            List<PatientDTO> modelListwihtoutQuery = new ArrayList<PatientDTO>();
        String query =
                "select b.openmrs_id,b.first_name,b.last_name,b.middle_name,b.uuid,b.date_of_birth from tbl_visit a, tbl_patient b, tbl_encounter c WHERE a.patientuuid = b.uuid  AND c.visituuid=a.uuid and c.provider_uuid in " +
                        "('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
                        "group by a.uuid order by b.uuid ASC";
        Logger.logD(TAG, query);
        final Cursor cursor = db.rawQuery(query, null);
        Logger.logD(TAG, "Cursour count" + cursor.getCount());

        try {
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        PatientDTO model = new PatientDTO();
                        model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                        model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                        model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                        model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                        model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                        model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                        model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
                        model.setBedNo(getPatientBedNot(model.getUuid()));
//                        model.setStage(getStage(model.getUuid()));
                        modelListwihtoutQuery.add(model);

                    } while (cursor.moveToNext());
                }
            }
            if (cursor != null) {
                cursor.close();
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }

        try {
//            searchPatientAdapter = new SearchPatientAdapter(modelListwihtoutQuery, SearchPatientActivity.this);
////            Log.i("db data", "" + getQueryPatients(query));
//            /*    recyclerView.addItemDecoration(new
//                        DividerItemDecoration(this,
//                        DividerItemDecoration.VERTICAL));*/
//            recyclerView.setAdapter(searchPatientAdapter);
            bindAdapter(modelListwihtoutQuery);

        } catch (Exception e) {
            Logger.logE("doquery", "doquery", e);
        }
//        }else {
//            String search = querytext.trim().replaceAll("\\s", "");
//            List<PatientDTO> modelList = new ArrayList<PatientDTO>();
//            String query =
//                    "select   b.openmrs_id,b.firstname,b.last_name,b.middle_name,b.uuid,b.date_of_birth  from tbl_visit a, tbl_patient b, tbl_encounter c WHERE" +
//                            "first_name LIKE " + "'%" + search +
//                            "%' OR middle_name LIKE '%" + search +
//                            "%' OR last_name LIKE '%" + search +
//                            "%' OR openmrs_id LIKE '%" + search +
//                            "%' " +
//                            "AND a.provider_uuid in ('" + StringUtils.convertUsingStringBuilder(providersuuids) + "')  " +
//                            "AND  a.patientuuid = b.uuid  AND c.visituuid=a.uuid " +
//                            "group by a.uuid order by b.uuid ASC";
//            Logger.logD(TAG, query);
//            final Cursor cursor = db.rawQuery(query, null);
//            Logger.logD(TAG, "Cursour count" + cursor.getCount());
//            try {
//                if (cursor != null) {
//                    if (cursor.moveToFirst()) {
//                        do {
//                            PatientDTO model = new PatientDTO();
//                            model.setOpenmrsId(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
//                            model.setFirstname(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
//                            model.setLastname(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
//                            model.setMiddlename(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
//                            model.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
//                            model.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
//                            model.setPhonenumber(StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))));
//                            modelList.add(model);
//
//                        } while (cursor.moveToNext());
//                    }
//                }
//                cursor.close();
//            } catch (DAOException sql) {
//                FirebaseCrashlytics.getInstance().recordException(sql);
//            }
//
//
//            try {
//                recycler = new SearchPatientAdapter(modelList, SearchPatientActivity.this);
//                RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
//                recyclerView.setLayoutManager(reLayoutManager);
//           /*     recyclerView.addItemDecoration(new
//                        DividerItemDecoration(this,
//                        DividerItemDecoration.HORIZONTAL));*/
//                recyclerView.setAdapter(recycler);
//
//            } catch (Exception e) {
//                FirebaseCrashlytics.getInstance().recordException(e);
//                Logger.logE("doquery", "doquery", e);
//            }
//        }
        return modelListwihtoutQuery;

    }

    private String phoneNumber(String patientuuid) throws DAOException {
        String phone = null;
        Cursor idCursor = db.rawQuery("SELECT value  FROM tbl_patient_attribute where patientuuid = ? AND person_attribute_type_uuid='14d4f066-15f5-102d-96e4-000c29c2a5d7' ", new String[]{patientuuid});
        try {
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    phone = idCursor.getString(idCursor.getColumnIndexOrThrow("value"));

                }
            }
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
        }
        idCursor.close();

        return phone;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (s.isEmpty()) firstQuery();
        else {
//            SearchRecentSuggestions suggestions = new SearchRecentSuggestions
//                    (SearchPatientActivity.this,
//                            SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
//            suggestions.clearHistory();
            query = s.trim();
            doQuery(s.trim());
        }
        return false;
    }
}



