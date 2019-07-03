package io.intelehealth.client.utilities;

import android.content.Context;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import io.intelehealth.client.app.IntelehealthApplication;

import static io.intelehealth.client.app.AppConstants.CONFIG_FILE_NAME;

public class ConfigUtils {
    public static String TAG = ConfigUtils.class.getSimpleName();
    SessionManager sessionManager = null;
    Context context;

    public ConfigUtils(Context context) {
        this.context = context;
    }

    public JSONObject jsonreader() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        JSONObject obj = null;
        try {
            if (sessionManager.valueContains("licensekey")) {
                obj = new JSONObject(FileUtils.readFileRoot(CONFIG_FILE_NAME, context)); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(context, CONFIG_FILE_NAME)));

            }
        } catch (JSONException e) {
            Logger.logE(TAG, "Exception", e);
            Toast.makeText(context, "JsonException" + e, Toast.LENGTH_LONG).show();
        }
        return obj;
    }

    public boolean mFirstName() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mFirstName");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mMiddleName() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mMiddleName");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mLastName() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mLastName");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mDOB() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mDOB");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mPhoneNum() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mPhoneNum");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mAge() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mAge");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mAddress1() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mAddress1");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mAddress2() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mAddress2");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mCity() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mCity");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean countryStateLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("countryStateLayout");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mPostal() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mPostal");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mGenderM() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mGenderM");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mGenderF() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mGenderF");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mRelationship() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mRelationship");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean mOccupation() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mOccupation");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean casteLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("casteLayout");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean educationLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("educationLayout");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean economicLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("economicLayout");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean height() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mHeight");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean weight() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mWeight");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean pulse() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mPulse");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean bpSys() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mBpSys");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean bpDia() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mBpDia");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean spo2() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mSpo2");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean bmi() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mBMI");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean resp() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mResp");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean temperature() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mTemperature");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean celsius() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mCelsius");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean fahrenheit() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mFahrenheit");
            Logger.logD(TAG, String.valueOf(view));
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

    public boolean privacy_notice() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("privacy_boolean");
        } catch (JSONException e) {
            Crashlytics.getInstance().core.logException(e);
        }
        return view;
    }

}
