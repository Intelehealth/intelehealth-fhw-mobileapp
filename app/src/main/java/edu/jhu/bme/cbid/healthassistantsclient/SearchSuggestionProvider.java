package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by tusharjois on 3/22/16.
 */
public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "edu.jhu.bme.cbid.healthassistantsclient.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}