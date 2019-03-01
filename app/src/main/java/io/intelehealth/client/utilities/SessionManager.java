package io.intelehealth.client.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
    // Shared preferences file name
    private static final String PREF_NAME = "Intelehealth";
    private static final String VISIT_ID="visitID";
    private static final String EMERGENCY="checked";
    // Shared Preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    // Shared pref mode
    private int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public String getVisitId() {
        return pref.getString(VISIT_ID, null);
    }

    public void setVisitId(String token) {
        editor.putString(VISIT_ID, token);
        editor.commit();
    }
    public Boolean isChecked () {
        return pref.getBoolean(EMERGENCY, false);
    }

    public void setChecked(Boolean checked) {
        editor.putBoolean(EMERGENCY, checked);
        editor.commit();
    }


}
