package io.intelehealth.client.utilities;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.intelehealth.client.app.IntelehealthApplication;

public class ConfigUtils {
    public static String TAG = ConfigUtils.class.getSimpleName();
    SessionManager sessionManager = null;
    Context context;
    private String mFileName = "config.json";

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

    public boolean mFirstName() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mFirstName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mMiddleName() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mMiddleName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mLastName() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mLastName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mDOB() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mDOB");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mPhoneNum() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mPhoneNum");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mAge() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mAge");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mAddress1() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mAddress1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mAddress2() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mAddress2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mCity() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mCity");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean countryStateLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("countryStateLayout");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mPostal() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mPostal");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mGenderM() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mGenderM");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mGenderF() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mGenderF");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mRelationship() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mRelationship");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean mOccupation() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mOccupation");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean casteLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("casteLayout");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean educationLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("educationLayout");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean economicLayout() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("economicLayout");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean height() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mHeight");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean weight() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mWeight");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean pulse() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mPulse");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean bpSys() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mBpSys");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean bpDia() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mBpDia");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean spo2() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mSpo2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean bmi() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mBMI");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean resp() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mResp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean temperature() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mTemperature");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public boolean celsius() {
        boolean view = false;

        JSONObject object = jsonreader();
        try {
            view = object.getBoolean("mCelsius");
        } catch (JSONException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return view;
    }



}
