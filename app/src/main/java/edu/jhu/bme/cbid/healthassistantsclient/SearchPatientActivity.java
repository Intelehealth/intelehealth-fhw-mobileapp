package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchPatientActivity extends AppCompatActivity {

    LocalRecordsDatabaseHelper mDbHelper;
    SearchCursorAdapter mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDbHelper = new LocalRecordsDatabaseHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            doQuery(query);
        }

        // TODO: Clear Suggestions
        // SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
        // SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
        // suggestions.clearHistory();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.generic_coming_soon, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    public void doQuery(String query) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String table = "patient";
        String[] columns = {"_id", "first_name", "middle_name", "last_name",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "phone_number", "patient_identifier1", "patient_identifier2, patient_identifier3"};
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
                noneFound(lvItems);
            } else if (searchCursor.moveToFirst()) {
                lvItems.setAdapter(mSearchAdapter);
                lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        // Toast
                        // Toast toast = Toast.makeText(getActivity(), mAdapter.getItem(position), Toast.LENGTH_SHORT);
                        // toast.show();

                        if (searchCursor.moveToPosition(position)) {
                            String fName = searchCursor.getString(searchCursor.getColumnIndexOrThrow("first_name"));
                            String mName = searchCursor.getString(searchCursor.getColumnIndexOrThrow("middle_name"));
                            char mInitial = '\0';
                            if (mName != null) mInitial = mName.charAt(0);
                            String lName = searchCursor.getString(searchCursor.getColumnIndexOrThrow("last_name"));
                            String dob = searchCursor.getString(searchCursor.getColumnIndexOrThrow("date_of_birth"));
                            int age = HelperMethods.getAge(dob);

                            String addr1 = searchCursor.getString(searchCursor.getColumnIndexOrThrow("address1"));
                            String addr2 = searchCursor.getString(searchCursor.getColumnIndexOrThrow("address2"));
                            if (addr2 != null) addr1 = addr1 + " " + addr2;
                            String cityVillage = searchCursor.getString(searchCursor.getColumnIndexOrThrow("city_village"));
                            String stateProvince = searchCursor.getString(searchCursor.getColumnIndexOrThrow("state_province"));
                            String postal = searchCursor.getString(searchCursor.getColumnIndexOrThrow("postal_code"));
                            String phoneNumber = searchCursor.getString(searchCursor.getColumnIndexOrThrow("phone_number"));

                            String sdw = searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_identifier1"));
                            String occupation = searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_identifier2"));

                            String patientIdCol = searchCursor.getString(searchCursor.getColumnIndexOrThrow("_id"));

                            String photoLoc = searchCursor.getString(searchCursor.getColumnIndexOrThrow("patient_identifier3"));


                            ArrayList<String> patientInfo = new ArrayList<String>();

                            patientInfo.add(lName);
                            patientInfo.add(fName);
                            patientInfo.add(mInitial + "");
                            patientInfo.add(dob);
                            patientInfo.add(age + "");
                            patientInfo.add(addr1);
                            patientInfo.add(cityVillage + ", " + stateProvince + " " + postal);
                            patientInfo.add(phoneNumber);
                            patientInfo.add(sdw);
                            patientInfo.add(occupation);
                            patientInfo.add(patientIdCol);
                            patientInfo.add(photoLoc);



                            Intent patientIntent = new Intent(SearchPatientActivity.this, PatientDetailActivity.class);
                            patientIntent.putStringArrayListExtra("patientInfo", patientInfo);
                            startActivity(patientIntent);
                        }
                    }
                });
            }


        } catch (Exception e) {
            noneFound(lvItems);
            Log.e("Search Activity", "Exception", e);

        }

    }

    public void noneFound(ListView lvItems)  {
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(this,
                R.layout.list_item_search,
                R.id.list_item_head,new ArrayList<String>());
        searchAdapter.add("No patients found for the given information.");
        lvItems.setAdapter(searchAdapter);
    }

}
