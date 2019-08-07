package io.intelehealth.client.utilities;


import android.content.Context;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;
import io.intelehealth.client.application.IntelehealthApplication;


public class ConfigUtils {
    public static String TAG = ConfigUtils.class.getSimpleName();
    SessionManager sessionManager = null;
    Context context;
    public static String mFileName = "config.json";

    public ConfigUtils(Context context) {
        this.context = context;
    }

    public JSONObject jsonreader() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        JSONObject obj = null;
        try {
            if (sessionManager.valueContains("licensekey")) {
                obj = new JSONObject(FileUtils.readFileRoot(mFileName, context)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(context, mFileName)));

            }
        } catch (JSONException e) {
            Logger.logE(TAG, "Exception", e);
            Toast.makeText(context, "JsonException" + e, Toast.LENGTH_LONG).show();
        }
        return obj;
    }


    public boolean privacy_notice() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("privacyNotice");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }
}
