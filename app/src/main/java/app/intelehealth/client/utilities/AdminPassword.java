package app.intelehealth.client.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import app.intelehealth.client.R;
import app.intelehealth.client.app.IntelehealthApplication;

/**
 * Created by Dexter Barretto on 8/5/17.
 * Github : @dbarretto
 */

public class AdminPassword {

    private static AdminPassword mAdminPassword;
    private Context mContext;
    private SharedPreferences mSharedPreference;

    private AdminPassword(Context context) {
        mContext = context;
        mSharedPreference = mContext.getSharedPreferences(
                context.getString(R.string.admin_login_shared_preference_key), Context.MODE_PRIVATE);
    }

    public static AdminPassword getAdminPassword() {
        if (mAdminPassword == null)
            mAdminPassword = new AdminPassword(IntelehealthApplication.getAppContext());
        return mAdminPassword;
    }

    public void setUp(String password) {
        StringEncryption stringEncryption = new StringEncryption();
        String random_salt = stringEncryption.getRandomSaltString();
        String hash = null;
        try {
            hash = StringEncryption.convertToSHA256(random_salt + password);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        SharedPreferences.Editor editor = mSharedPreference.edit();

        if (random_salt != null && !TextUtils.isEmpty(random_salt)) editor.putString(
                mContext.getString(R.string.admin_login_salt_key), random_salt);

        if (hash != null && !TextUtils.isEmpty(hash)) editor.putString(
                mContext.getString(R.string.admin_login_password_key), hash);

        editor.commit();

    }

    public boolean login(String password) {
        if (mSharedPreference.contains(mContext.getString(R.string.admin_login_salt_key)) &&
                mSharedPreference.contains(mContext.getString(R.string.admin_login_password_key))) {

            String stored_password = mSharedPreference.getString(mContext.getString(R.string.admin_login_password_key), null);
            String stored_salt = mSharedPreference.getString(mContext.getString(R.string.admin_login_salt_key), null);

            if (stored_password != null && stored_salt != null) {
                StringEncryption stringEncryption = new StringEncryption();
                String hash = null;
                try {
                    hash = StringEncryption.convertToSHA256(stored_salt + password);
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (hash != null && stored_password.equals(hash)) {
                    return true;
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_admin_password), Toast.LENGTH_LONG).show();
                    return false;
                }

            } else {
                Toast.makeText(mContext, mContext.getString(R.string.admin_authentication_not_possible), Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.admin_authentication_not_possible), Toast.LENGTH_LONG).show();
            return false;
        }

    }

}
