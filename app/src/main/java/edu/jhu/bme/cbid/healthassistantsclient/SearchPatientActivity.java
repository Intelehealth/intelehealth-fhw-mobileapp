package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchPatientActivity extends AppCompatActivity {

    LocalRecordsDatabaseHelper mDbHelper;
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        String[] columns = {"_id", "first_name", "middle_name", "last_name", "date_of_birth", "gender"};
        String selection = "patient MATCH ?";
        String[] args = new String[1];
        args[0] = String.format("%s", query);
        String order = "last_name ASC";
        Cursor searchCursor = db.query(table, columns, selection, args, null, null, order);
        // Find ListView to populate
        ListView lvItems = (ListView) findViewById(R.id.listview_search);

        try {
            // Setup cursor adapter and attach cursor adapter to the ListView
            SearchCursorAdapter searchAdapter = new SearchCursorAdapter(this, searchCursor, 0);
            if (searchAdapter.getCount() < 1) {
                noneFound(lvItems);
            } else {
                lvItems.setAdapter(searchAdapter);
            }
            lvItems.setAdapter(searchAdapter);
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
