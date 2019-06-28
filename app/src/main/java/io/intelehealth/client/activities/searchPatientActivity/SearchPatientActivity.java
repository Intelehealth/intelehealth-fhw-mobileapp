package io.intelehealth.client.activities.searchPatientActivity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.intelehealth.client.R;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.database.InteleHealthDatabaseHelper;
import io.intelehealth.client.models.dto.PatientDTO;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.SessionManager;

public class SearchPatientActivity extends AppCompatActivity {
    SearchView searchView;
    InteleHealthDatabaseHelper mDbHelper;
    String query;
    private SearchPatientAdapter recycler;
    RecyclerView recyclerView;
    SessionManager sessionManager = null;
    TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Get the intent, verify the action and get the query
        sessionManager = new SessionManager(this);
        mDbHelper = new InteleHealthDatabaseHelper(this);
        msg = findViewById(R.id.textviewmessage);
        recyclerView = findViewById(R.id.recycle);
        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (sessionManager.isSyncFinished()) {
                msg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                doQuery(query);
            }

        } else {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            if (sessionManager.isSyncFinished()) {
                msg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                firstQuery();
            }

        }


    }

    private void doQuery(String query) {
        try {
            recycler = new SearchPatientAdapter(getQueryPatients(query), SearchPatientActivity.this);

//            Log.i("db data", "" + getQueryPatients(query));
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(reLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(recycler);

        } catch (Exception e) {
            Logger.logE("doquery", "doquery", e);
        }
    }

    private void firstQuery() {
        try {
            getAllPatientsFromDB();

            recycler = new SearchPatientAdapter(getAllPatientsFromDB(), SearchPatientActivity.this);


//            Log.i("db data", "" + getAllPatientsFromDB());
            RecyclerView.LayoutManager reLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(reLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(recycler);

        } catch (Exception e) {
            Logger.logE("firstquery", "exception", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XMLz
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setFocusable(true);
        searchView.requestFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Hack", "in query text change");
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(SearchPatientActivity.this,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                doQuery(newText);
                return true;
            }
        });


        return true;
    }

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

    public List<PatientDTO> getAllPatientsFromDB() {
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String table = "tbl_patient";
        final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table +
                " ORDER BY first_name ASC", null);

        if (searchCursor.moveToFirst()) {
            do {
                PatientDTO model = new PatientDTO();
                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));
                modelList.add(model);
            } while (searchCursor.moveToNext());
        }


        //  Log.d("student data", modelList.toString());


        return modelList;

    }

    public List<PatientDTO> getQueryPatients(String query) {
        String search = query.trim();
        List<PatientDTO> modelList = new ArrayList<PatientDTO>();
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        String table = "tbl_patient";
        final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table +
                " WHERE first_name LIKE " + "'" + search +
                "%' OR last_name LIKE '" + search +
                "%' OR openmrs_id LIKE '" + search +
                "%' OR middle_name LIKE '" + search + "%' " +
                "ORDER BY first_name ASC", null);

        if (searchCursor.moveToFirst()) {
            do {
                PatientDTO model = new PatientDTO();
                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                model.setFirstname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name")));
                model.setLastname(searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name")));
                model.setOpenmrsId(searchCursor.getString(searchCursor.getColumnIndexOrThrow("openmrs_id")));
                model.setMiddlename(searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name")));
                model.setUuid(searchCursor.getString(searchCursor.getColumnIndexOrThrow("uuid")));

                modelList.add(model);
            } while (searchCursor.moveToNext());
        }


        //   Log.d("patients data", modelList.toString());


        return modelList;

    }
}




