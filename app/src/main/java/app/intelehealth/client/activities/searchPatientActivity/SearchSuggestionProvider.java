
package app.intelehealth.client.activities.searchPatientActivity;

import android.content.SearchRecentSuggestionsProvider;

/**
 * This class extends SearchRecentSuggestionsProvider
 * Created by tusharjois on 3/22/16.
 */

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

