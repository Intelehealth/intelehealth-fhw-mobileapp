package io.intelehealth.client.activities.login_activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.setting_activity.SettingsActivity;
import io.intelehealth.client.application.IntelehealthApplication;
import io.intelehealth.client.security.StringEncryption;

/**
 * Created by Dexter Barretto on 8/5/17.
 * Github : @dbarretto
 */

public class AdminPassword {

    private Context mContext;
    private SharedPreferences mSharedPreference;
    private static AdminPassword mAdminPassword;

    private AdminPassword(Context context) {
        mContext = context;
        mSharedPreference = mContext.getSharedPreferences(
                context.getString(R.string.admin_login_shared_preference_key), Context.MODE_PRIVATE);
    }

    public void setUp(String password) {
        StringEncryption stringEncryption = new StringEncryption();
        String random_salt = stringEncryption.getRandomSaltString();
        String hash = null;
        try {
            hash = stringEncryption.convertToSHA256(random_salt + password);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
                    hash = stringEncryption.convertToSHA256(stored_salt + password);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (hash != null && stored_password.equals(hash)) {
                    return true;
                } else {
                    Toast.makeText(mContext, "Admin Password Wrong", Toast.LENGTH_LONG).show();
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

    public static AdminPassword getAdminPassword() {
        if (mAdminPassword == null)
            mAdminPassword = new AdminPassword(IntelehealthApplication.getAppContext());
        return mAdminPassword;
    }

}
