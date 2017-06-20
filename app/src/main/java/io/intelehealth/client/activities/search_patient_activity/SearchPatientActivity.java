
package io.intelehealth.client.activities.search_patient_activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import io.intelehealth.client.activities.patient_detail_activity.PatientDetailActivity;
import io.intelehealth.client.R;
import io.intelehealth.client.database.LocalRecordsDatabaseHelper;

/**
 * This class helps to search for a patient from list of existing patients.
 */

public class SearchPatientActivity extends AppCompatActivity {

    final String LOG_TAG = "Search Patient Activity";

    LocalRecordsDatabaseHelper mDbHelper;
    SearchCursorAdapter mSearchAdapter;
    SearchView searchView;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDbHelper = new LocalRecordsDatabaseHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            doQuery(query);
        }

        // TODO: Clear Suggestions
        // SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
        // SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        // suggestions.clearHistory();

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
                doInstantSearch(newText);
                return true;
            }
        });


        return true;
    }

    /**
     * This method retrieves data from database and sends it via Intent to PatientDetailActivity.
     * @param query variable of type String
     * @return             void
     */

    public void doQuery(String query) { // called in onCreate()
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String table = "patient";
        String[] columns = {"_id", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "phone_number", "patient_photo"};

        //String selection = "patient like ?"; //TODO: try using like instead of match
        //String[] args = new String[1];
        //args[0] = "%"
        //String searchQuery = "%" + query + "%";

        String selection = "patient MATCH ?";
        String[] args = new String[1];
        args[0] = String.format("%s", query);

        String order = "last_name ASC";
        final Cursor searchCursor = db.query(table, columns, selection, args, null, null, order);
        // Find ListView to populate
        ListView lvItems = (ListView) findViewById(R.id.listview_search);


        try {
            // Setup cursor adapter and attach cursor adapter to the ListView
            mSearchAdapter = new SearchCursorAdapter(this, searchCursor, 0);
            if (mSearchAdapter.getCount() < 1) {
                noneFound(lvItems, query);
            } else if (searchCursor.moveToFirst()) {
                lvItems.setAdapter(mSearchAdapter);
                lvItems.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                        if (searchCursor.moveToPosition(position)) {
                            String patientID = searchCursor.getString(searchCursor.getColumnIndexOrThrow("_id"));
//                            Log.d(TAG, patientID);
                            String patientStatus = "returning";
                            Intent intent = new Intent(SearchPatientActivity.this, PatientDetailActivity.class);
                            intent.putExtra("patientID", patientID);
                            intent.putExtra("status", patientStatus);
                            intent.putExtra("tag", "");
                            startActivity(intent);

                        }
                    }
                });
            }


        } catch (Exception e) {
            noneFound(lvItems, query);
            Log.e("Search Activity", "Exception", e);

        }

    }

    //TODO: Hacked Code , needs cleaning.

    /**
     * This method is used to search for details with only a partial string.
     * @param searchTerm variable of type String
     * @return                  void
     */
    public void doInstantSearch(String searchTerm) { // called in onCreateOptionsMenu
        String search = searchTerm.trim();
        ListView lvItems = (ListView) findViewById(R.id.listview_search);
        if(TextUtils.isEmpty(search)) {
            lvItems.setAdapter(null);
        }
        else {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String table = "patient";

            final Cursor searchCursor = db.rawQuery("SELECT * FROM " + table + " WHERE first_name LIKE " +
                    "'" + search + "%' OR last_name LIKE '" + search + "%' OR middle_name LIKE '" + search + "%' " +
                    "ORDER BY last_name ASC", null);
            try {
                // Setup cursor adapter and attach cursor adapter to the ListView
                mSearchAdapter = new SearchCursorAdapter(this, searchCursor, 0);
                if (mSearchAdapter.getCount() < 1) {

                } else if (searchCursor.moveToFirst()) {
                    lvItems.setAdapter(mSearchAdapter);
                    lvItems.setOnItemClickListener(
                            new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                                    if (searchCursor.moveToPosition(position)) {
                                        String patientID = searchCursor.getString(searchCursor.getColumnIndexOrThrow("_id"));
//                            Log.d(LOG_TAG, patientID);
                                        String patientStatus = "returning";
                                        Intent intent = new Intent(SearchPatientActivity.this, PatientDetailActivity.class);
                                        intent.putExtra("patientID", patientID);
                                        intent.putExtra("status", patientStatus);
                                        intent.putExtra("tag", "");
                                        startActivity(intent);

                                    }
                                }
                            });
                }


            } catch (Exception e) {
                Log.d("Search Activity", "Exception", e);

            }
        }

    }

    /**
     * This method is called when no search result is found for patient.
     * @param lvItems variable of type ListView
     * @param query  variable of type String
     */
    public void noneFound(ListView lvItems, String query) {
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_search,
                R.id.list_item_head, new ArrayList<String>());
        String errorMessage = getString(R.string.alert_none_found).replace("_", query);
        searchAdapter.add(errorMessage);
        lvItems.setAdapter(searchAdapter);
    }

}

